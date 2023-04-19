import java.io.*;
import java.util.*;
import java.net.*;


class TCPClient implements Runnable {
    private Socket clientSocket;
    private String myAddress;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private VoipApp voip;
    private Thread th;

    public TCPClient(VoipApp voip,String myAddress,String serverAddress,int port) {
        try {
            this.voip         = voip;
            this.clientSocket = new Socket(serverAddress,port);
            this.myAddress    = myAddress;
            this.input  = new ObjectInputStream(this.clientSocket.getInputStream());
            this.output = new ObjectOutputStream(this.clientSocket.getOutputStream());
        }
        catch(IOException io) {
            System.out.println(io);
            io.printStackTrace();
        }
    }

    public void run() {
        try {
            this.output.writeUTF("new");
            this.output.flush();
            this.output.writeUTF(this.myAddress);
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
