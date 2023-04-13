import java.net.*;
import java.io.*;

public class VoipApp {
    public static String RECEIVER = "receiver";
    public static String SENDER   = "sender";
    public VoipSender sender;
    public VoipReceiver receiver;
    public VoipRunner runner;

    public VoipApp(VoipSender sender,VoipReceiver receiver,String address,int port,String role) {
        this.receiver = new VoipReceiver(port);
        this.sender = new VoipSender(address,port);
        
        runner = new VoipRunner(sender,receiver,role);
        runner.start();
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
        String address = args[0];
        int port       = Integer.parseInt(args[1]);
        String role    = args[2];

        VoipSender sender = new VoipSender(address, port);
        VoipReceiver receiver = new VoipReceiver(port);
        VoipApp app = new  VoipApp(sender, receiver, address, port,role);       
    }
}
