package me.drton.pwmanalyzer;

/*
 * Copyright (c) 2013 Anton Babushkin. All rights reserved.
 */

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeriesCollection;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class PWMAnalyzerMain extends JFrame {
    public final static String appName = "PWMAnalyzer";
    private JLabel statusLabel;
    private ChartPanel chartPanel;
    private JFreeChart jFreeChart;
    private JCheckBox inputInvert;
    private JRadioButton showPulse;
    private JRadioButton showWidth;
    private JRadioButton showIntervals;
    private PWMAnalyzer analyzer = new PWMAnalyzer();
    private XYSeriesCollection dataset = new XYSeriesCollection();
    private String fileName = null;

    public PWMAnalyzerMain() throws IOException, UnsupportedAudioFileException {
        super(appName);
        setSize(800, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        initComponents();
        jFreeChart.getXYPlot().setDataset(dataset);
        updateChart();
    }

    private void initComponents() {
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setVgap(5);
        setLayout(borderLayout);
        Container container = getContentPane();
        // Control panel
        Box vBoxLeft = Box.createVerticalBox();
        // Panel "Input"
        JPanel panelInput = new JPanel();
        panelInput.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5),
                BorderFactory.createTitledBorder("Input")));
        panelInput.setLayout(new BoxLayout(panelInput, BoxLayout.PAGE_AXIS));
        JButton openButton = new JButton("Open");
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showOpenFileDialog();
            }
        });
        panelInput.add(openButton);
        inputInvert = new JCheckBox("Invert");
        ActionListener inputActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processFile();
            }
        };
        inputInvert.addActionListener(inputActionListener);
        panelInput.add(inputInvert);
        vBoxLeft.add(panelInput);
        // Panel "Show"
        JPanel panelShow = new JPanel();
        panelShow.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5),
                BorderFactory.createTitledBorder("Show")));
        panelShow.setLayout(new BoxLayout(panelShow, BoxLayout.PAGE_AXIS));
        showPulse = new JRadioButton("Pulses");
        showWidth = new JRadioButton("Width");
        showIntervals = new JRadioButton("Interval");
        ActionListener showActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateChart();
            }
        };
        showPulse.addActionListener(showActionListener);
        showWidth.addActionListener(showActionListener);
        showIntervals.addActionListener(showActionListener);
        showWidth.setSelected(true);
        ButtonGroup group = new ButtonGroup();
        group.add(showPulse);
        group.add(showWidth);
        group.add(showIntervals);
        panelShow.add(showPulse);
        panelShow.add(showWidth);
        panelShow.add(showIntervals);
        vBoxLeft.add(panelShow);
        container.add(vBoxLeft, BorderLayout.LINE_START);
        // Chart panel
        jFreeChart = ChartFactory.createXYLineChart("", "", "", null, PlotOrientation.VERTICAL, false, true, false);
        jFreeChart.setBackgroundPaint(Color.WHITE);
        XYPlot plot = jFreeChart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        NumberAxis numberAxis = (NumberAxis) plot.getRangeAxis();
        numberAxis.setAutoRangeIncludesZero(false);
        chartPanel = new ChartPanel(jFreeChart);
        container.add(chartPanel, BorderLayout.CENTER);
        // Status bar
        statusLabel = new JLabel(" ");
        container.add(statusLabel, BorderLayout.PAGE_END);
    }

    private void updateChart() {
        dataset.removeAllSeries();
        if (showPulse.isSelected())
            dataset.addSeries(analyzer.getPulseSeries());
        else if (showWidth.isSelected())
            dataset.addSeries(analyzer.getPulseWidths());
        else if (showIntervals.isSelected())
            dataset.addSeries(analyzer.getPulseIntervals());
        jFreeChart.getXYPlot().getRangeAxis().setAutoRange(true);
    }

    public void setStatus(String status) {
        statusLabel.setText(" " + status);
    }

    public void showOpenFileDialog() {
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showDialog(this, "Attach");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            fileName = file.getPath();
            setTitle(appName + " - " + fileName);
            setStatus("Processing...");
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    processFile();
                }
            });
        }
    }

    private void processFile() {
        if (fileName != null) {
            analyzer.setInputSign(inputInvert.isSelected() ? -1.0 : 1.0);
            try {
                analyzer.processFile(fileName);
                jFreeChart.getXYPlot().getDomainAxis().setAutoRange(true);
                updateChart();
                setStatus("");
            } catch (IOException e) {
                setStatus("Error: " + e);
            } catch (UnsupportedAudioFileException e) {
                setStatus("Unsupported audio file");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    PWMAnalyzerMain pwmAnalyzerMain = new PWMAnalyzerMain();
                    pwmAnalyzerMain.setVisible(true);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UnsupportedAudioFileException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
