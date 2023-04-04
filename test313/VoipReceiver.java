import java.net.*;
import java.io.*;
import javax.sound.sampled.*;

public class VoipReceiver {
    public static void main(String[] args) {
        try {
            // Create a server socket and wait for a client to connect
            ServerSocket serverSocket = new ServerSocket(4999);
            Socket socket = serverSocket.accept();

            // Get the input stream from the socket
            InputStream inputStream = socket.getInputStream();

            // Set up the audio format
            AudioFormat format = new AudioFormat(8000, 16, 1, true, true);

            // Get a source data line for playing audio
            SourceDataLine speakers = AudioSystem.getSourceDataLine(format);
            speakers.open(format);
            speakers.start();

            // Create a buffer for reading audio data
            byte[] buffer = new byte[1024];

            // Read audio data from the server and play it on the speakers
            while (true) {
                int bytesRead = inputStream.read(buffer, 0, buffer.length);
                speakers.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
