# AutoTunningDrum
Milestone Overview - Production release

SEE "Design.pdf" FOR INTERNAL, EXTERNAL, AND PERSISTENT STATE DESIGNS

This milestone represents a build with all features complete, with no bugs. It shows a finilized, working product.

# Project Work Space:
- 3D_Files
- ESP32_Src:
  - Test Files:
      - Peripherals 
          - Microphone I2S Communication
          - Classic Bluetooth Communication
          - Servo Motor Pulse Width Modulation
      - Algorithms
          - Fast Fourier Transform (FFT)
  - ESP32 Project Firmware
      - ESP32 Code for Project
- Software_Src:
  - Bluetooth Communication
      - Classic Bluetooth test for writing/reading data from the ESP32
  - Companion App Src
      - App Code for Project
-----------------------------------------------------------------------------------------------------------------------------------------------------

# Hardware Development:
  - Linear Controller loop (PID - Proportional-Integral-Derivative)
  - Mic proper functioning
  - Soldering of all parts into one
  - Peak detection (Envelope follower)
  - Encasing (3D printed encasing)
  - Drill bit to connect to the lug and be able to twist it.
  - Moving average filter used to smooth out the samples obtained from the mic.
  - Harmonic Product Spectrum (HPS) is a signal processing technique used to estimate the fundamental frequency (pitch), especially those with harmonic content like musical instruments, helpful in our case sice drums have a lot of overtones.

# Android Application Development:
  - Button gradients for note accuracy from red to green.
  - Device management for bluetooth connectivity.
  - Spinner drum image for user UX/UI with a slice highlighted.
  - Current note box showing detected note.
  - Variable lug count.
  - Drop down menu that selects target note.
  - Open mic button - Turns on the mic -.
  - Frequency shows on the bottom of the screen.
  
--------------------------------------------------------------------------------------------------------------------------------------------------------
# Known Bugs and Issues

Hardware Bugs:

Software Bugs:
  
---------------------------------------------------------------------------------------------------------------------------------------------------------
# Future Plans
  - Cater to other drums with different tuning ranges.
  - Refining of the App and Firmware.
