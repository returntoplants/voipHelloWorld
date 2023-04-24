import java.io.*;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat;

public class Speaker implements Runnable {
    private Thread t;
    private AudioFormat format;
    public BlockingQueue<byte[]> audioQueue;
    private SourceDataLine speakers;

    public Speaker() {
        this.audioQueue = new LinkedBlockingQueue<>();
        this.format     = new AudioFormat(8000.0f,16,1,true,true);
        this.speakers   = new AudioSystem.getSourceDataLine(format);
    }

}
