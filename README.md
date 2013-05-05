PWMAnalyzer
===========

PWMAnalyzer is simple command-line tool for analyzing PWM signal captured by sound card. PWMAnalyzer reconstructs pulses
after high-pass filter in sound card and creates interval and width plots.

Building
--------
Simply run:

ant

Resulting jar will be placed to out/production/ directory.

Usage
--------
java -jar pwmanalyzer.jar <file.wav> [-s <width>x<height>]

Output files "pwm_interval.png" and "pwm_width.png" will be placed to current directory. Plot size can be adjusted
with "-s" option, defaults to 1024x768.
