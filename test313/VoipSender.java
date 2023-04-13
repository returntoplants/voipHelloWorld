import java.net.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import java.io.*;

// the voip sender
public class VoipSender implements Runnable {
    private DatagramSocket socket;
    private InetAddress dest;
    private AudioFormat format;
    private int destPort;
    private Thread t;
    public boolean inCall;

    public VoipSender(String receiverAddress,int port) {
        try {
            //the datagram socket.
            socket = new DatagramSocket();

            // destination addresss.
            dest   = InetAddress.getByName(receiverAddress); 

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

            //create a buffer to hold the audio data.
            byte[] buffer = new byte[1024];

            // Continuouslty read audio data from the microphone and send it over UDP
            while (true) {
                int count = microphone.read(buffer,0,buffer.length);

                // the datagram packet.
                DatagramPacket packet = new DatagramPacket(buffer,count,dest,destPort);

                socket.send(packet);   // send the data packet over the socket.
            }
        }
        catch (LineUnavailableException lu) {
            System.out.println(lu);
            lu.printStackTrace();
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
