import java.io.*;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat;

public class Microphone implements Runnable {
    private Thread t;
    private AudioFormat format;
    private TargetDataLine microphone;

    public Microphone() {
        this.format = new AudioFormat(8000.0f,16,1,true,true);

        this.microphone = AudioSystem.getTargetDataLine(this.format);

    }
}
