import java.io.*;
import java.nio.DoubleBuffer;
import java.nio.ByteBuffer;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.AudioFormat;
import java.util.concurrent.*;
public class VoipMicrophone implements Runnable {
    private Thread t;
    private AudioFormat format;
    private TargetDataLine microphone;
    public BlockingQueue<byte[]> audioQueue;

    public VoipMicrophone() {
        this.format = new AudioFormat(8000.0f,16,1,true,false);

        try {
            this.microphone = AudioSystem.getTargetDataLine(this.format);
        }
        catch(LineUnavailableException lu) {
            System.out.println("line unavailable exception: "+lu);
        }
        this.audioQueue = new LinkedBlockingQueue<>();
    }

    public void run() {
        try {
            this.microphone.open(format);
            this.microphone.start();

            int CHUNK_SIZE = 1024;
            while (true) {
                byte[] buffer = new byte[CHUNK_SIZE];
                int count = microphone.read(buffer,0,CHUNK_SIZE);
            
                //double freq = this.getFrequency(buffer,16,8000.0f);
                //System.out.println(" current frequency: "+freq);
                //if (freq > -1) {
                this.audioQueue.put(buffer);
                //}
            }
        }
        catch(LineUnavailableException lu) {
            System.out.println("line unavailable exception: "+lu);
        }
        catch(InterruptedException ie) {
            System.out.println("interrupt exception: "+ie);
        }

    }

    public void start() {
        if (t == null) {
            t = new Thread(this,"microphone");
            t.start();
        }
    }

    /**
     * AUDIO - PROCESSING functions.
     * */
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
            j += m;
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
                    double tImag = wReal*xImag[l] + wImag*xReal[l];
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


    public double[] byteArrayToDoubleArray(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        DoubleBuffer doubleBuffer = byteBuffer.asDoubleBuffer();

        double[] doubleArray = new double[doubleBuffer.remaining()];
        doubleBuffer.get(doubleArray);
        return doubleArray;
    }


    public double getFrequency(byte[] array,int sampleSize,double sampleRate) {
        double[] audioSamples = byteArrayToDoubleArray(array);

        //for (int i = 0;i < audioSamples.length;i++) {
        //    audioSamples[i] /= Short.MAX_VALUE;
        //} 
        double[] audioSpectrum = fft(audioSamples);

        int numSamples = audioSpectrum.length;
        double sampleInterval = 1.0 / (sampleSize/ 2.0); 

        double[] frequencies  = new double[numSamples];
        double[] magnitudes = new double[numSamples];

        for (int i = 0;i < numSamples; i++ ) {
            frequencies[i] = i*sampleRate / (double)(numSamples);
            double trum = Math.pow(audioSpectrum[numSamples - i - 1],2);
            double spec= Math.pow(audioSpectrum[i],2);
            magnitudes[i]  = audioSpectrum[i];     
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
