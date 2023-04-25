import java.io.*;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat;
import java.util.concurrent.*;
public class Speaker implements Runnable {
    private Thread t;
    private AudioFormat format;
    public BlockingQueue<byte[]> audioQueue;
    private SourceDataLine speakers;

    public Speaker() {
        this.audioQueue = new LinkedBlockingQueue<>();
        this.format     = new AudioFormat(8000.0f,16,1,true,true);
        try {
            this.speakers   = AudioSystem.getSourceDataLine(format);
        }
        catch(LineUnavaivableException lu) {
            System.out.println(" line unavailable exception: "+lu);
        }
    }

    public void run() {

        try {
            this.speakers.open(this.format);

            this.speakers.start();

            while (true) {
                byte[] data = this.audioQueue.take();

                //write out data to the speakers.
                this.speakers.write(data,0,data.length);
            }
        }
        catch(LineUnavailableException lu) {
            System.out.println("line unavailable exceptions: "+lu);
        }
        catch(InterruptedException ie) {
            System.out.println(" interrupted exception error: "+ie);
        }
    }

    public void start() {
        if (t == null) {
            t = new Thread(this,"speaker");
        }
        t.start();
    }

    public double rateOfChange(byte[] array) {
        double[] audioData = new double[array.length/2];
        for (int i =0,j = 0; i < audioData.length;i += 2,j++) {
            int sample = (array[i+1]<<8) | (array[i]  & 0xff);
            audioData[j] = sample / 32768.0;
        }

        double rms = 0.0;
        for (double sample: audioData) {
            rms += sample * sample;
        }
        rms /= audioData.length;
        rms = Math.sqrt(rms);
        return rms;
    }

    private void bitReverse(double[] xReal,double[] xImag) {
        int n = xReal.length;
        
        int j = 0;
        for (int i = 0;i < n;i++) {
            if (j > 1) {
                double temp = xReal[j];
                xReal[j] = xReal[i];
                xReal[i] = temp;
                temp = xImag[j];
                xImag[j] = xImag[i];
                xImag[i] = temp;
            }

            int m = n/2;

            while (m >= 2 && j >= m) {
                j -= m;
                m /= 2;
            }
            j = m;
        }
    }

    private void transform(double[] xReal, double[] xImag) {
        int n = xReal.length;

        bitReverse(xReal,xImag);
        for (int s = 2;s <= n;s *= 2) {
            double wReal = 1.0;
            double wImag = 0.0;
            double theta = 2.0* Math.PI /s;

            double wThetaReal = Math.cos(theta);
            double wThetaImag = Math.sin(theta);

            for (int j = 0; j < s/2;j++) {
                for (int k = j; k < n; k += s) {
                    int l = k + s/2;
                    double tReal = wReal*xReal[l] - wImag*xImag[l];
                    double tImag = wImag*xImag[l] + wImag*xReal[l];
                    xReal[l] = xReal[k] - tReal;
                    xImag[l] = xImag[k] - tImag;

                    xReal[k] += tReal;
                    xImag[k] += tImag;
                }
                double wTempReal = wReal * wThetaReal - wImag * wThetaImag;
                double wTempImag = wReal * wThetaImag + wImag * wThetaReal;

                wReal = wTempReal;
                wImag = wTempImag;
            }
        }
    }

    public double[] fft(double[] audioSamples) {
        int n = audioSamples.length;
        double[] re = new double[n];
        double[] im = new double[n];

        for (int i = 0;i < n;i++) {
            re[i] = audioSamples[i];
        }

        this.transform(re,im);
        double[] spectrum = new double[n];
        for (int i = 0;i < n;i++)  {
            spectrum[i] = Math.sqrt(re[i]*re[i] + im[i]*im[i]);
        }
        return spectrum;
    }


    public double getFrequency(byte[] array,int sampleRate) {
        double[] audioSamples = new double[array.length / sampleRate];

        for (int i = 0;i < audioSamples.length;i++) {
            audioSamples[i] = Short.MAX_VALUE;
        } 

        double[] audioSpectrum = fft(audioSamples);

        int numSamples = audioSpectrum.length;
        double sampleInterval = 1.0 / (sampleRate/ 2.0);

        double[] frequencies  = new double[numSamples];
        double[] magnitudes = new double[numSamples];

        for (int i = 0;i < numSamples; i++ ) {
            frequencies[i] = i / (sampleInterval * numSamples);
            double trum = Math.pow(audioSpectrum[numSamples - i - 1],2);
            double spec= Math.pow(audioSpectrum[i],2);
            magnitudes[i]  = Math.sqrt(spec+trum);      
        }

        double maxMagnitude  = 0;
        double dominantFreq  = 0;

        for (int i = 0;i < numSamples / 2;i++) {
            if (magnitudes[i] > maxMagnitude) {
                maxMagnitude  = magnitudes[i];
                dominantFreq  = frequencies[i];
            }
        }
        return dominantFreq;
    }


}
