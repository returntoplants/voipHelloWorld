import java.net.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import java.io.*;
import java.util.*;

// the voip sender
public class VoipSender implements Runnable {
    private DatagramSocket socket;
    private ArrayList<InetAddress> destinations;
    private AudioFormat format;
    private int destPort;
    private Thread t;
    public boolean inCall;

    public VoipSender(String receiverAddress,int port) {
        try {
            //the datagram socket.
            socket = new DatagramSocket();

            this.destinations = new ArrayList<InetAddress>();
            // destination addresss.
            this.destinations.add(InetAddress.getByName(receiverAddress)); 

            //the audio format for the audio data.
            format = new AudioFormat(8000.0f, 16, 1, true, true);
            
            destPort = port;  // the port.
            inCall   = false;
        }
        catch (IOException io) {
            System.out.println(io);
            io.printStackTrace();
        }
    }

    public void call() throws IOException {
        try {
            System.out.println("awe");
            this.inCall = true;
            // the microphone input stream.
            TargetDataLine microphone = AudioSystem.getTargetDataLine(format);

            //the microphone.
            microphone.open(format);
            microphone.start();


            // Continuouslty read audio data from the microphone and send it over UDP
            while (true) {
                //create a buffer to hold the audio data.
                byte[] buffer = new byte[8*1024];

                microphone.read(buffer,0,buffer.length);
                // the datagram packet.
                if (this.getDestinations().size() > 1) {
                    Arrays.stream(this.getDestinations().toArray())
                        .parallel()
                        .forEach(
                                dest -> {
                                    try {
                                        InetAddress d = (InetAddress)dest;
                                        DatagramPacket packet = new DatagramPacket(buffer,buffer.length,d,destPort);
                                        socket.send(packet);
                                    }
                                    catch(IOException io) {
                                        System.out.println("errs : "+io);
                                    }
                            }
                        );
                }
                else {
                    InetAddress dest = this.getDestinations().get(0);
                    DatagramPacket packet = new DatagramPacket(buffer,buffer.length,dest,destPort);
                    socket.send(packet);
                }
            }
        }
        catch (LineUnavailableException lu) {
            System.out.println(lu);
            lu.printStackTrace();
        }
    }


    public ArrayList<InetAddress> getDestinations() {
        return this.destinations;
    }

    public void addDestination(String dest) {
        try {
            this.destinations.add(InetAddress.getByName(dest));
        }
        catch(UnknownHostException noKnown) {
            System.out.println(noKnown);
            noKnown.printStackTrace();
        }
    }

    public void run() {
        try {
            this.call();
        }
        catch (IOException io) {
            System.out.println(" io exceptions "+io);
            io.printStackTrace();
        }
    }
    
    public void start() {
        if (t == null) {
            t = new Thread(this,"sender");
            t.start();
        }
    }
    

    public static void main(String[] args) {
        String receiverAddr = args[0];
        int port = Integer.parseInt(args[1]);
        VoipSender send = new VoipSender(receiverAddr, port);
        try {
            send.call();    // start the call.
        }
        catch(IOException io) {
            System.out.println(io);
            io.printStackTrace();
        }
    }
}
