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
    private AudioFormat format;
    private InetAddress rcvAddr;
    private InetAddress myAddr;
    private InetSocketAddress groupAddr;
    private Microphone microphone;
    private int destPort;
    private Thread t;
    public boolean inCall;
    public String call;

    public VoipSender(String receiverAddress,String myAddress,int port,String call) {
        try {
            //the datagram socket.
            this.myAddr = InetAddress.getByName(myAddress);
            socket = new DatagramSocket();
            socket.setOption(StandardSocketOptions.IP_MULTICAST_LOOP,false);
            this.call = call;
            this.rcvAddr = InetAddress.getByName(receiverAddress);
            this.microphone = new Microphone();
            switch(call) {    
                case "group":
                    this.groupAddr = new InetSocketAddress(this.rcvAddr,port);
                    break;
            }
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
        System.out.println("awe");
        this.inCall = true;
        // the microphone input stream.
        this.microphone.start();

        // Continuouslty read audio data from the microphone and send it over UDP
        while (true) {
            //create a buffer to hold the audio data.
            try {
                byte[] buffer = this.microphone.audioQueue.take();
                // the datagram packet.
                switch(this.call) {
                    case "private":
                        DatagramPacket packet = new DatagramPacket(buffer,buffer.length,this.rcvAddr,destPort);
                        socket.send(packet);
                        break;
                    case "group":
                        DatagramPacket gpack = new DatagramPacket(buffer, buffer.length,this.groupAddr);
                        socket.send(gpack);
                        break;
                }
            }
            catch(InterruptedException  e) {
                System.out.println("interrupt exception.");
            }
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
        //VoipSender send = new VoipSender(receiverAddr, port);
        //try {
        //    send.call();    // start the call.
        //}
        //catch(IOException io) {
         //   System.out.println(io);
         //   io.printStackTrace();
        //}
    }
}
