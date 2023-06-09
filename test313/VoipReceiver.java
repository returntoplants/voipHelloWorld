import java.net.*;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat;

import java.io.*;

public class VoipReceiver implements Runnable {
    private Thread t;
	private DatagramSocket socket;
    private String call;
    private MulticastSocket mSocket;
    private AudioFormat format;
    private String myAddress;
    private InetSocketAddress group;
    private VoipSpeaker speakers;
    private SourceDataLine speak;

    public VoipReceiver(MulticastSocket mSocket,int port,String call,String myAddress) {
        String multicastAddr = "228.0.0.0";
        try {
            format = new AudioFormat(8000.0f,16,1,true,true);
            this.myAddress = myAddress;
            this.call = call;
            this.speakers = new VoipSpeaker();
            this.mSocket = mSocket;

            try {
                this.speak = AudioSystem.getSourceDataLine(format);
            }
            catch(LineUnavailableException lu) {
                System.out.println("interrupted exception error: "+lu);
            }
            switch(call) {
                case "private":
                    socket = new DatagramSocket(port);
                    break;
                case "group":
                    System.out.println("created datagram socket.");
                    break;
            }


        }
        catch(IOException io) {
            System.out.println(io);
            io.printStackTrace();
        }
    }


    public double rateOfChange(byte[] array) {
        double[] audioData = new double[array.length/2];
        for (int i = 0,j = 0; i< audioData.length;i += 2,j++) {
            int sample = (array[i+1] << 8) | (array[i] & 0xff);
            audioData[j] = sample / 32768.0;
        }

        double rms = 0.0;
        for (double sample: audioData) {
            rms += sample * sample;
        }

        rms /= audioData.length;
        rms = Math.sqrt(rms);
        System.out.println("standard deviation of sound: "+rms);
        return rms;
    } 

    public void receive() throws IOException {
        try {
            
            //this.speakers.start();
            this.speak.open(this.format);
            this.speak.start();
            byte[] buffer = new byte[1024];
            // Continuously receive audio data over UDP and play it back on the
            // speakers.
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
                InetAddress source = packet.getAddress();
                if (source != null) {
                    System.out.println("source: "+ source.getHostAddress());
                }
                switch(this.call) {
                    case "group":
                        mSocket.receive(packet);
                        break;
                    default:
                        mSocket.receive(packet);
                        break;
                }
                //socket.receive(packet);
                //double rateMs = this.rateOfChange(packet.getData());
         
                //put the data onto the audio queue.
                //this.speakers.audioQueue.put(packet.getData());
                this.speak.write(packet.getData(),0,packet.getData().length);    
            }
        }
        catch (LineUnavailableException lu) {
            System.out.println(lu);
            lu.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
		
        // receiver the file file data
        //VoipReceiver receiver = new VoipReceiver(port);
    }
	public void run () {
		try {
		    this.receive();
		} catch(IOException e) {
		    System.out.println("awe");
		    e.printStackTrace();
		}
	}
	public void start() {
		if (t == null) {
			t = new Thread(this, "receiver");
			t.start();
		} 
		
	}

}
