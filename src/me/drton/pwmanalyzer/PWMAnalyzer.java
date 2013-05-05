package me.drton.pwmanalyzer;

/*
 * Copyright (c) 2013 Anton Babushkin. All rights reserved.
 */

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

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
    private XYSeries pulseIntervals = new XYSeries("intervals", false);
    private XYSeries pulseWidths = new XYSeries("widths", false);

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
            pulseStart = t;
        } else if (v < -threshold && boolValue) {
            boolValue = false;
            pulseWidths.add(t, (t - pulseStart) * 1000000);
        }
    }

    public void processFile(String somePathName) throws IOException, UnsupportedAudioFileException {
        File fileIn = new File(somePathName);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(fileIn);
        AudioFormat audioFormat = audioInputStream.getFormat();
        int bytesPerFrame = audioFormat.getFrameSize();
        double sampleFreq = audioFormat.getFrameRate();
        boolean bigEndian = audioFormat.isBigEndian();
        int bits = audioFormat.getSampleSizeInBits();
        if (bits != 16) {
            throw new RuntimeException("Only 16-bit PCM is supported");
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
                double v = -readInt(audioBytes, i * 2 * channels, bigEndian) / (double) 0x8000;
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

    public XYSeries getPulseIntervals() {
        return pulseIntervals;
    }

    public XYSeries getPulseWidths() {
        return pulseWidths;
    }

    public static void plot(XYSeries series, String fileName, String titlePlot, String titleX, String titleY,
                            int[] size) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        // Generate the graph
        JFreeChart chart = ChartFactory.createXYLineChart(titlePlot, titleX, titleY, dataset, PlotOrientation.VERTICAL,
                false, false, false);
        NumberAxis numberAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
        numberAxis.setAutoRangeIncludesZero(false);
        try {
            ChartUtilities.saveChartAsJPEG(new File(fileName), chart, size[0], size[1]);
        } catch (IOException e) {
            System.err.println("Problem occurred creating chart.");
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar pwmanalyzer.jar <file.wav> [-s <width>x<height>]");
    }

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException {
        if (args.length < 1) {
            printUsage();
            return;
        }
        String opt = null;
        int[] size = new int[]{1024, 768};
        String inFile = null;
        for (String arg : args) {
            if (arg.startsWith("-")) {
                opt = arg;
                continue;
            }
            if (opt == null) {
                if (inFile == null) {
                    inFile = arg;
                } else {
                    printUsage();
                    return;
                }
            } else {
                if ("-s".equals(opt)) {
                    String[] p = arg.split("x");
                    try {
                        size = new int[]{Integer.parseInt(p[0]), Integer.parseInt(p[1])};
                    } catch (Exception e) {
                        printUsage();
                        return;
                    }
                }
                opt = null;
            }
        }
        PWMAnalyzer analyzer = new PWMAnalyzer();
        analyzer.processFile(args[0]);
        plot(analyzer.getPulseWidths(), "pwm_width.png", "Pulse Width", "Time, s", "Width, us", size);
        plot(analyzer.getPulseIntervals(), "pwm_interval.png", "Pulse Interval", "Time, s", "Interval, us", size);
    }
}
