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
    ChartPanel chartPanel;
    JFreeChart jFreeChart;
    JRadioButton showWidth;
    JRadioButton showIntervals;
    PWMAnalyzer analyzer = new PWMAnalyzer();
    XYSeriesCollection dataset = new XYSeriesCollection();

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
        JButton openButton = new JButton("Open");
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showOpenFileDialog();
            }
        });
        vBoxLeft.add(openButton);
        showWidth = new JRadioButton("Width");
        showIntervals = new JRadioButton("Intervals");
        ActionListener selectPlotActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateChart();
            }
        };
        JPanel panelShow = new JPanel();
        panelShow.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5),
                BorderFactory.createTitledBorder("Show")));
        panelShow.setLayout(new BoxLayout(panelShow, BoxLayout.PAGE_AXIS));
        showWidth.addActionListener(selectPlotActionListener);
        showIntervals.addActionListener(selectPlotActionListener);
        showWidth.setSelected(true);
        ButtonGroup group = new ButtonGroup();
        group.add(showWidth);
        group.add(showIntervals);
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
        if (showWidth.isSelected())
            dataset.addSeries(analyzer.getPulseWidths());
        if (showIntervals.isSelected())
            dataset.addSeries(analyzer.getPulseIntervals());
        jFreeChart.getXYPlot().getRangeAxis().setAutoRange(true);
    }

    public void setStatus(String status) {
        System.out.printf("Set status: %s\n", status);
        statusLabel.setText(" " + status);
    }

    public void showOpenFileDialog() {
        JFileChooser fc = new JFileChooser();
        int returnVal = fc.showDialog(this, "Attach");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            final String fileName = file.getPath();
            setStatus("Processing...");
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        analyzer.processFile(fileName);
                        setStatus("");
                    } catch (IOException e) {
                        setStatus("Error: " + e);
                    } catch (UnsupportedAudioFileException e) {
                        setStatus("Unsupported audio file");
                    }
                }
            });
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
