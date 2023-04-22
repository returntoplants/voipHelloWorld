import java.io.*;
import java.util.*;
import java.net.*;


class TCPClient implements Runnable {
    private Socket clientSocket;
    private String myAddress;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private VoipApp voip;
    private int basePort;
    private Thread th;

    public TCPClient(VoipApp voip,String myAddress,String serverAddress,int port) {
        try {
            this.voip         = voip;
            this.clientSocket = new Socket(serverAddress,port);
            this.basePort     = port;
            this.myAddress    = myAddress;
            this.output = new ObjectOutputStream(this.clientSocket.getOutputStream());
            this.input  = new ObjectInputStream(this.clientSocket.getInputStream());
            System.out.println("client has been constructed!!");
        }
        catch(IOException io) {
            System.out.println(io);
            io.printStackTrace();
        }
    }

    //once the port for the receiver to run on is determined then we
    //run the receiver.
    public void startReceiver(int port) {
        
    }

    public void run() {
        try {
            System.out.println("start of run.");
            this.output.writeUTF("new");
            this.output.flush();
            
            this.output.writeUTF(this.myAddress);
            this.output.flush();

            //read in the number of active callers.
            int n = this.input.readInt();
            int port = this.basePort + n*1000;

            
            System.out.println("running client.");
        }
        catch(IOException io) {
            System.out.println(io);
            io.printStackTrace();
        }
    }
    public void start() {
        if (this.th == null) {
            th = new Thread(this,"client-"+this.myAddress);
            th.start();
        }
    }
}
