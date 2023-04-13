import java.net.*;
import java.io.*;

public class VoipApp {
    public VoipSender sender;
    public VoipReceiver receiver;

    public VoipApp(VoipSender sender,VoipReceiver receiver,String addresss,int port) {
        this.sender = new VoipSender(receiverAddress,port);
        this.receiver = new VoipReceiver(port);
        
        this.sender.start();
        this.receiver.start();
    }

    public static void main(String[] args) {
        String address = args[0];
        int port       = Integer.parseInt(args[1]);
        VoipSender sender = new VoipSender(address, port);
        VoipReceiver receiver = new VoipReceiver(port);
        VoipApp app = new  VoipApp(sender, receiver, addresss, port);       
    }
}
