# AutoTunningDrum
Milestone Overview
This milestone demonstrates the initial integration of hardware and software components for the audio frequency analyzer project. It includes functional implementations of key systems, focusing on the connection between the ESP32 microcontroller and the Android application via Bluetooth.

Work Completed in This Milestone

Hardware Development:
I2S Microphone Integration:
Configured the ESP32 to read audio input from the I2S microphone.
Established a sample rate for audio acquisition.
FFT Implementation:
Developed FFT processing using the ArduinoFFT library.
Bluetooth Communication:
Enabled ESP32 Bluetooth to send frequency data to the Android app.
LED Feedback:
Added LED indicator to show active Bluetooth connections.

Android Application Development:
Designed and implemented an intuitive user interface with dropdown menus for drum configuration and a record button for initiating analysis.
Added Bluetooth connectivity to communicate with the ESP32 using hardcoded device addresses.
Implemented permission handling for modern Android versions.
Configured navigation between the main screen and subsequent activities.

Integration and Communication:
Established reliable two-way communication between the ESP32 and Android application using Bluetooth.
ESP32 successfully receives commands and sends processed frequency data back to the Android app.

Known Bugs and Issues
Hardware Bugs:
FFT Accuracy:
Occasional noise in audio data results in inaccurate frequency peaks.
Memory Constraints:
Buffer allocation for large FFT sizes can fail for longer recordings, must stick to a short period of time.

Software Bugs:
Hardcoded Bluetooth Address:
The Android app only connects to a predefined ESP32 device address, limiting flexibility for multiple devices.
Peak detection:
Cant properly detect peak
Issues in noisy environment where the mic would be triggered for other noise that is not the drum.
UI/UX Issues:
Dropdown options are not dynamically linked to functionality.
After drum specifications, the socket connection to the ESP fails at times yet the page still goes to the recording page which it shouldn't do since recording will be
impossible without an active connection.

Future Plans
Possibly use a directional mic to avoid undesired noise or apply filters
Implement a dynamic Bluetooth device discovery feature.
Add filtering mechanisms for audio input to enhance FFT accuracy.

Refine user interface for better real-time feedback and error handling.
