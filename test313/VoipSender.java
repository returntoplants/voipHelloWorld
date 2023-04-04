import java.net.*;
import java.io.*;
import javax.sound.sampled.*;

public class VoipSender  {
    public static void main(String[] args){
        try {
            // Create a socket and connect to the server
            Socket socket = new Socket("127.0.0.1", 4999);

            // Get the output stream from the socket
            OutputStream outputStream = socket.getOutputStream();

            // Set up the audio format
            AudioFormat format = new AudioFormat(8000, 16, 1, true, true);

            // Get a target data line for capturing audio
            TargetDataLine microphone = AudioSystem.getTargetDataLine(format);
            microphone.open(format);
            microphone.start();

            // Create a buffer for reading audio data
            byte[] buffer = new byte[1024];

            // Read audio data from the microphone and send it to the server
            while (true) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                outputStream.write(buffer, 0, bytesRead);
                outputStream.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}