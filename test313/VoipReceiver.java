import java.net.*;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat;
import org.webrtc.AudioProcessing;
import org.webrtc.AudioProcessingFactory;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.PeerConnectionFactory.InitializationOptions;

import java.io.*;

public class VoipReceiver implements Runnable {
    private Thread t;
	private DatagramSocket socket; 
    private AudioFormat format;
    private AudioProcessingFactory audioProcessingFactory;
    private AudioProcessing audioProcessing;
    public VoipReceiver(int port) {
        try {
            format = new AudioFormat(8000.0f,16,1,true,true);

             InitializationOptions initializationOptions =
                InitializationOptions.builder(format).createInitializationOptions();
            PeerConnectionFactory.initialize(initializationOptions);
            // Create an instance of AudioProcessing
            AudioProcessingFactory audioProcessingFactory = new AudioProcessingFactory();
             audioProcessing = audioProcessingFactory.createAudioProcessing();
            // Enable Echo Cancellation
            audioProcessing.setEchoCancellation(true);
            

            socket = new DatagramSocket(port); 

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
                byte[] processedBuffer = new byte[packet.getLength()];
                audioProcessing.process(packet.getData(), packet.getLength(), format.getSampleRate(), processedBuffer);
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
