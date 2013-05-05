PWMAnalyzer
===========

PWMAnalyzer is simple command-line tool for analyzing PWM signal captured by sound card. Signal should be captured by
external recorder (e.g. Audacity) and stored to file in WAV or AIFF format. PWMAnalyzer reconstructs pulses after high-pass
filter in sound card and creates interval and width plots.

Building
--------
To build jar run:

    ant

Resulting jar will be placed to out/production/ directory.

Usage
--------
To process some file run:

    java -jar pwmanalyzer.jar <file.wav> [-s <width>x<height>]

Output files "pwm_interval.png" and "pwm_width.png" will be placed to current directory. Plot size can be adjusted
with "-s" option, defaults to 1024x768.
