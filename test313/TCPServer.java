import java.io.*;
import java.util.*;
import java.net.*;

class TCPServer implements Runnable {
    private ServerSocket tcpServerSocket;
    private ArrayList<TCPThread> threads;
    private VoipApp voip;
    public int nc;
    private Thread th;
    public TCPServer(VoipApp voip,ServerSocket tcpServerSocket) {
        this.tcpServerSocket = tcpServerSocket;
        this.voip            = voip;
        this.threads         = new ArrayList<TCPThread>();
        this.nc              = 0;
    }

    public void run() {
        try {
            while (true) {
                Socket clientSocket = this.tcpServerSocket.accept();
                System.out.println("connected!");
                TCPThread tcpThread = new TCPThread(this,this.voip,clientSocket);
                this.threads.add(tcpThread);
                tcpThread.start();
                nc++;
            }
        }
        catch(IOException io) {
            System.out.println(io);
            io.printStackTrace();
        }
    }

    public void start() {
        if (th == null) {
            th = new Thread(this,"server");
            th.start();
        }
    }

    public void newClient(String address,int port) {
        for (TCPThread th: this.threads) {
            th.addDestination(address);
            th.newReceiver(port);
        }
    }



    private class TCPThread implements Runnable {
        private Socket clientSocket;
        private ObjectInputStream input;
        private ObjectOutputStream output;
        private VoipApp voip;
        private TCPServer tcpServer;
        private String myAddress;
        private Thread th;

        public TCPThread(TCPServer server,VoipApp voip,Socket clientSocket) {
            try {
                this.tcpServer    = server;
                this.voip         = voip;
                this.clientSocket = clientSocket;
                this.output       = new ObjectOutputStream(clientSocket.getOutputStream());
                this.input        = new ObjectInputStream(clientSocket.getInputStream());
            }
            catch(IOException io) {
                System.out.println("error: "+io);
                io.printStackTrace();
            }
        }

        public void newReceiver(String address,int port) {
            this.voip.newReceiver(address,port);
        }

        public void addDestination(String dest) { 
            if (!this.myAddress.equals(dest)) {
                this.voip.addAddress(dest);
            } 
        }
        
        public void run() {
            while (true) {
                try {
                    String in = this.input.readUTF();
                    switch(in) {
                        case "new":
                            //read in the clients new address.
                            String address = this.input.readUTF();
                            System.out.println("address: "+address);
                           
                            int n = this.tcpServer.threads.size();
        
                            //write out the number of callers on the call
                            this.output.writeInt(n);
                            this.output.flush();

                            //read in the port number
                            int port = this.input.readInt();

                            //new receiver.
                            this.tcpServer.newReceiver(address,port);
                            break;
                        case "":
                            break;

                    }
                }
                catch(IOException io) {
                    System.out.println("error 2: "+io);
                    io.printStackTrace();
                }
            }
        }

        public void start() {
            if (th == null) {
                th = new Thread(this,"thread-"+nc);
                th.start();
            }
        }
    }

}
