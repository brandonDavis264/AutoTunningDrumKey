# AutoTunningDrum
Milestone Overview - Release candidate

SEE "Design.pdf" FOR INTERNAL, EXTERNAL, AND PERSISTENT STATE DESIGNS

This milestone represents a build with all features complete, with minor bugs. It shows an almost finilized, working product.

Project Work Space:
- ESP32_Src:
  - Test Files:
      - Peripherals 
          - Microphone I2S Communication
          - Classic Bluetooth Communication
          - Servo Moror Pulse With Modulation
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

Hardware Development:
  - PID loop
  - Mic proper functioning
  - Soldering of all parts into one
  - Peak detection
  - Encasing
  - Drill bit to connect to the lug and be able to twist it.

Android Application Development:
  - Button gradients for note accuracy from red to green.
  - Device management for bluetooth connectivity.
  - Spinner drum image for user UX/UI with a slice highlighted.
  - Current note box showing detected note.
  - Variable lug count.
  - Drop down menu that selects target note. (only for the beta build version and production release)
  - Open mic button - Turns on the mic -.
  - Frequency shows on the bottom of the screen.
  
--------------------------------------------------------------------------------------------------------------------------------------------------------
Known Bugs and Issues

Hardware Bugs:
  - Actual tuning of the drum faces issues with overtones and natural errors when struck at different pressures.

Software Bugs:
  - This build for the app does not have the ability to send data to the ESP; however, the code for it is in the Beta build and can easily be ported over for Production release

Future Plans
  - Finilize encasing for the hardware to better fit the device and avoid the motor from slipping. Mic, power switch, charge port holes positioning.
  - Include tutorial/walkthrough on welcome page.
  - Testing of the device and app with the drum.
