import java.net.*;
import java.io.*;
import java.util.*;

public class VoipApp {
    public static String RECEIVER = "receiver";
    public static String SENDER   = "sender";
    public ArrayList<String> receiverAddresses;
    public Socket tcpSocket;
    public VoipSender sender;
    public VoipReceiver receiver;
    public VoipRunner runner;

    public VoipApp(String receiver,String myAddress,int port,String role) {
        this.receiver = new VoipReceiver(port);
        this.sender = new VoipSender(receiver,port);
        try { 
        switch(role) {
            case "receiver":
                ServerSocket tcpServerSocket = new ServerSocket(port+10);
                TCPServer tcp = new TCPServer(this,tcpServerSocket); 
                tcp.start();
                break;
            case "sender":
            case "other":
                TCPClient client = new TCPClient(this,myAddress,receiver,port+10);
                client.start();
                break;
        }
        }
        catch(IOException io) {
            System.out.println(io);
            io.printStackTrace();
        }
        runner = new VoipRunner(sender,this.receiver,role);
        System.out.println(" running!!! ");
        runner.start();

    }

    public void addAddress(String address) {
        this.sender.addDestination(address);
    }
   
    private class VoipRunner implements Runnable {
        private Thread t;
        private String role;
        VoipSender sender;
        VoipReceiver receiver;

        public VoipRunner(VoipSender sender,VoipReceiver receiver,String role) {
            this.sender = sender;
            this.receiver = receiver;
            this.role = role;
        }

        public void run() {
            switch(this.role) {
                case "receiver":
                    this.receiver.start();
                    while (!this.sender.inCall) {
                        //keep on trying to send.
                        this.sender.start();
                    }
                    break;
                case "other":
                case "sender":
                    this.sender.start();
                    this.receiver.start();
                    break;
            }
        }

        public void start() {
            if (t == null) {
                t = new Thread(this,this.role);
                t.start();
            }
        }
    }

    public static void main(String[] args) {
        String receiverAddr = args[0];
        String address = args[1];
        int port       = Integer.parseInt(args[2]);
        String role    = args[3];
        VoipApp app = new  VoipApp(receiverAddr, address, port,role);       
    }
}
