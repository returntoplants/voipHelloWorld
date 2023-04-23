import java.net.*;
import java.io.*;
import java.util.*;

public class VoipApp {
    public static String RECEIVER = "receiver";
    public static String SENDER   = "sender";


    public Socket tcpSocket;
    public VoipSender sender;
    public VoipReceiver receiver;
    public VoipRunner runner;

    public VoipApp(String receiver,String myAddress,int port,String role,String call) {
        this.receiver = new VoipReceiver(port,call,myAddress);
        if (call.equals("group")) {
            this.sender = new VoipSender("228.0.0.0",port,call);
        }
        else {
            this.sender = new VoipSender(receiver,port,call);
        }

        runner = new VoipRunner(sender,this.receiver,role);
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
        String myAddress    = args[1];
        int port       = Integer.parseInt(args[2]);
        String role    = args[3];
        String call    = args[4];
        VoipApp app = new  VoipApp(receiverAddr,myAddress,port,role,call);       
    }
}
