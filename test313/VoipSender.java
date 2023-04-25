import java.net.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import java.io.*;
import java.util.*;
import java.nio.*;


// the voip sender
public class VoipSender implements Runnable {
    private DatagramSocket socket;
    private AudioFormat format;
    private InetAddress rcvAddr;
    private InetAddress myAddr;
    private InetSocketAddress groupAddr;
    private VoipMicrophone microphone;
    private TargetDataLine mphone;
    private int destPort;
    private Thread t;
    public boolean inCall;
    public String call;

    public VoipSender(String receiverAddress,String myAddress,int port,String call) {
        try {
            //the datagram socket.
            this.myAddr = InetAddress.getByName(myAddress);
            socket = new DatagramSocket();
            //socket.setOption(StandardSocketOptions.IP_MULTICAST_LOOP,false);
            this.call = call;
            this.rcvAddr = InetAddress.getByName(receiverAddress);
            //this.microphone = new VoipMicrophone();
            switch(call) {    
                case "group":
                    this.groupAddr = new InetSocketAddress(this.rcvAddr,port);
                    break;
            }
            //the audio format for the audio data.
            format = new AudioFormat(8000.0f, 16, 1, true, true);
             
            this.mphone = AudioSystem.getTargetDataLine(format);
            destPort = port;  // the port.
            inCall   = false;
        }
        catch (IOException io) {
            System.out.println(io);
            io.printStackTrace();
        }
        catch(LineUnavailableException lu) {
            System.out.println("line unavailable exception: "+lu);
        }
    }

    public void call() throws IOException {
        System.out.println("awe");
        this.inCall = true;
        try {
        // the microphone input stream.
        this.mphone.open(format);
        this.mphone.start();

        int CHUNK_SIZE = 1024;
        // Continuouslty read audio data from the microphone and send it over UDP
        while (true) {
            //create a buffer to hold the audio data.
            //try {
                //byte[] buffer = this.microphone.audioQueue.take();
                byte[] buffer = new byte[CHUNK_SIZE];
                int count = this.mphone.read(buffer,0,CHUNK_SIZE);
                // the datagram packet.
                double frequency = this.getFrequency(buffer, 16, 8000.0f);
                System.out.println("current frequency: "+frequency);
                switch(this.call) {
                    case "private":
                        DatagramPacket packet = new DatagramPacket(buffer,buffer.length,this.rcvAddr,destPort);
                        socket.send(packet);
                        break;
                    case "group":
                        DatagramPacket gpack = new DatagramPacket(buffer, buffer.length,this.groupAddr);
                        socket.send(gpack);
                        break;
                }
            //}
            //catch(InterruptedException  e) {
            //    System.out.println("interrupt exception.");
            //}
        }
        }
        catch(LineUnavailableException lu) {
            System.out.println("line unavailable exception "+lu);
        }
        
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

        for (int i = 0;i < audioSamples.length;i++) {
            audioSamples[i] /= Short.MAX_VALUE;
        } 
        double[] audioSpectrum = fft(audioSamples);

        int numSamples = audioSpectrum.length;

        double[] frequencies  = new double[numSamples];
        double[] magnitudes = new double[numSamples];

        for (int i = 0;i < numSamples; i++ ) {
            frequencies[i] = i*sampleRate / (double)(numSamples);
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
    public void run() {
        try {
            this.call();
        }
        catch (IOException io) {
            System.out.println(" io exceptions "+io);
            io.printStackTrace();
        }
    }
    
    public void start() {
        if (t == null) {
            t = new Thread(this,"sender");
            t.start();
        }
    }
    

    public static void main(String[] args) {
        String receiverAddr = args[0];
        int port = Integer.parseInt(args[1]);
        //VoipSender send = new VoipSender(receiverAddr, port);
        //try {
        //    send.call();    // start the call.
        //}
        //catch(IOException io) {
         //   System.out.println(io);
         //   io.printStackTrace();
        //}
    }
}
