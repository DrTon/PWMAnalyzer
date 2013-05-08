package me.drton.pwmanalyzer;

/*
 * Copyright (c) 2013 Anton Babushkin. All rights reserved.
 */

import org.jfree.data.xy.XYSeries;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class PWMAnalyzer {
    private boolean boolValue = false;
    private double pulseStart = 0.0;
    private double maxValue = 1.0;
    private double maxValueW = 1.0;
    private double inputSign = 1.0;
    private XYSeries pulseIntervals = new XYSeries("Interval", false);
    private XYSeries pulseWidths = new XYSeries("Width", false);
    private XYSeries pulseSeries = new XYSeries("Pulse", false);

    private void reset() {
        boolValue = false;
        pulseStart = 0.0;
        maxValue = 1.0;
        maxValueW = 1.0;
        pulseWidths.clear();
        pulseIntervals.clear();
        pulseSeries.clear();
    }

    public void newValue(double t, double v) {
        double absV = Math.abs(v);
        if (absV > maxValue)
            maxValue = absV;
        maxValue *= maxValueW;
        double threshold = maxValue * 0.3;
        if (v > threshold && !boolValue) {
            boolValue = true;
            if (pulseStart != 0)
                pulseIntervals.add(t, (t - pulseStart) * 1000000);
            pulseSeries.add(t, 0);
            pulseSeries.add(t, 1);
            pulseStart = t;
        } else if (v < -threshold && boolValue) {
            boolValue = false;
            pulseWidths.add(t, (t - pulseStart) * 1000000);
            pulseSeries.add(t, 1);
            pulseSeries.add(t, 0);
        }
    }

    public void processFile(String somePathName) throws IOException, UnsupportedAudioFileException {
        reset();
        File fileIn = new File(somePathName);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(fileIn);
        AudioFormat audioFormat = audioInputStream.getFormat();
        int bytesPerFrame = audioFormat.getFrameSize();
        double sampleFreq = audioFormat.getFrameRate();
        boolean bigEndian = audioFormat.isBigEndian();
        int bits = audioFormat.getSampleSizeInBits();
        if (bits != 16) {
            throw new UnsupportedAudioFileException("Only 16-bit PCM is supported");
        }
        int channels = audioFormat.getChannels();
        maxValueW = 1.0 - 1.0 / sampleFreq;
        int numBytes = 1024 * bytesPerFrame;
        byte[] audioBytes = new byte[numBytes];
        long n = 0;
        double t = 0.0;
        int numBytesRead;
        int numFramesRead;
        while ((numBytesRead = audioInputStream.read(audioBytes)) != -1) {
            numFramesRead = numBytesRead / bytesPerFrame;
            for (int i = 0; i < numFramesRead; i++) {
                double v = inputSign * readInt(audioBytes, i * 2 * channels, bigEndian) / (double) 0x8000;
                newValue(t, v);
                n++;
                t = n / sampleFreq;
            }
        }
    }

    private static int readInt(byte[] arr, int offs, boolean bigEndian) {
        int v;
        if (bigEndian)
            v = (arr[offs + 1] & 0xff) + (arr[offs] & 0xff) * 0x100;
        else
            v = (arr[offs + 1] & 0xff) * 0x100 + (arr[offs] & 0xff);
        if ((v & 0x8000) != 0)
            v -= 0x10000;
        return v;
    }

    public void setInputSign(double inputSign) {
        this.inputSign = inputSign;
    }

    public XYSeries getPulseIntervals() {
        return pulseIntervals;
    }

    public XYSeries getPulseWidths() {
        return pulseWidths;
    }

    public XYSeries getPulseSeries() {
        return pulseSeries;
    }
}
