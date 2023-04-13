import java.net.*;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat;

import java.io.*;

public class VoipReceiver {
    private DatagramSocket socket; 
    private AudioFormat format;

    public VoipReceiver(int port) {
        try {
            format = new AudioFormat(8000.0f,16,1,true,true);

            socket = new DatagramSocket(port); 

            this.receive();
        }
        catch(IOException io) {
            System.out.println(io);
            io.printStackTrace();
        }
    }

    public void receive() throws IOException {
        try {
            SourceDataLine speakers = AudioSystem.getSourceDataLine(format);

            speakers.open(format);
            speakers.start();

            byte[] buffer = new byte[1024];

            // Continuously receive audio data over UDP and play it back on the
            // speakers.
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
                socket.receive(packet);
                speakers.write(packet.getData(),0,packet.getLength());
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
        VoipReceiver receiver = new VoipReceiver(port);
    }
}
