#include <driver/i2s.h>
#include <arduinoFFT.h>
#include <ESP32Servo.h>
#include "BluetoothSerial.h"

#define I2S_WS 4
#define I2S_SD 15
#define I2S_SCK 23
#define PWM_PIN 18
#define I2S_PORT I2S_NUM_0

BluetoothSerial SerialBT; 
int BLUE_LED = 2;

//Signal Proccesing
int16_t* sBuffer; // Dynamic Buffers
double* vReal;
double* vImag;
int bufferLen = 1024; // will record for 23.22ms     1024/44100 = 0.023219
//Desired frequency
double targetFreak = 0;
// FFT Object
ArduinoFFT<double> FFT = ArduinoFFT<double>(NULL, NULL, bufferLen, 44100); // 44100 is the sampling rate used for fft

// Servo Proccessing Variables 
Servo servoFS5;
int servPos = 0;
const int stopPulse = 90;
int maxTurnAngle = 90; // limit on how much we can turn the motor at a time

// Envelope Follower variables
float envelope = 0.0f;
const float alpha = 0.05f;  // Low-pass filter smoothing factor
const float envelopeThreshold = 3000.0f;  // Envelope threshold for calculation

int enable = 0;

#pragma region i2s Set Up
void i2s_install() {
  const i2s_config_t i2s_config = {
    .mode = i2s_mode_t(I2S_MODE_MASTER | I2S_MODE_RX),
    .sample_rate = 44100,
    .bits_per_sample = i2s_bits_per_sample_t(16),
    .channel_format = I2S_CHANNEL_FMT_ONLY_LEFT,
    .communication_format = i2s_comm_format_t(I2S_COMM_FORMAT_STAND_I2S),
    .intr_alloc_flags = 0,
    .dma_buf_count = 8,
    .dma_buf_len = 512,
    .use_apll = false
  };
  i2s_driver_install(I2S_PORT, &i2s_config, 0, NULL);
}

void i2s_setpin() {
  const i2s_pin_config_t pin_config = {
    .bck_io_num = I2S_SCK,
    .ws_io_num = I2S_WS,
    .data_out_num = -1,
    .data_in_num = I2S_SD
  };
  i2s_set_pin(I2S_PORT, &pin_config);
}
#pragma endregion

#pragma region Signal Proccessing
bool allocateBuffers(int length) {
  if (sBuffer) free(sBuffer);
  if (vReal) free(vReal);
  if (vImag) free(vImag);

  sBuffer = (int16_t*)malloc(length * sizeof(int16_t));
  vReal = (double*)malloc(length * sizeof(double));
  vImag = (double*)malloc(length * sizeof(double));

  if (!sBuffer || !vReal || !vImag) {
    Serial.println("Memory allocation failed!");
    return false;
  }
  FFT = ArduinoFFT<double>(vReal, vImag, length, 44100);
  return true;
}

// Envelope Follower Function
void processEnvelope(int16_t* sBuffer, size_t data_size) {
  for (size_t i = 0; i < data_size; i++) {
    // Rectify the signal (absolute value)
    float rectified = abs(sBuffer[i]);
    
    // Apply low-pass filter to smooth the envelope
    envelope = alpha * rectified + (1 - alpha) * envelope;
  }

  Serial.print("Target frequency:");
  Serial.println(targetFreak);
  Serial.print("Reached Threshold:");
  Serial.println(envelopeThreshold);
  // Print envelope value for plotting (Serial Plotter compatible)
  Serial.print("envelope:");
  Serial.println(envelope);
}

// moving average logic used to smooth out the samples obtained from the mic
#define AVG_COUNT 5
double freqBuffer[AVG_COUNT] = {0};
int bufferIndex = 0;

double rollingAverage(double newFreq) {
  freqBuffer[bufferIndex++] = newFreq;
  if (bufferIndex >= AVG_COUNT) bufferIndex = 0;

  double sum = 0;
  for (int i = 0; i < AVG_COUNT; i++) {
    sum += freqBuffer[i];
  }
  return sum / AVG_COUNT;
}


// The Harmonic Product Spectrum (HPS) is a signal processing technique used to estimate the 
// fundamental frequency (pitch) of a signal, especially those with harmonic content like musical instruments
double findHPS(double* spectrum, int len, int sampleRate, int fftSize) {
  int hpsLen = len / 3;
  double* hps = (double*)malloc(hpsLen * sizeof(double));
  if (!hps) return 0;

  for (int i = 0; i < hpsLen; i++) {
    hps[i] = spectrum[i] * spectrum[2 * i] * spectrum[3 * i];
  }

  double maxVal = 0;
  int maxIndex = 0;
  for (int i = 2; i < hpsLen; i++) {
    if (hps[i] > maxVal) {
      maxVal = hps[i];
      maxIndex = i;
    }
  }

  free(hps);
  return (maxIndex * (double)sampleRate) / fftSize;
}


double recordAndCalculateAverage() {
  unsigned long startTime = millis();
  double totalFrequency = 0;
  int numReadings = 0;
  //Polling Makes The frequncy show up after
  while (millis() - startTime < 1) {
    //Stops the loop if target freq
    double tempFreak = targetFreak;
    if (SerialBT.available()) {
      String receivedData = SerialBT.readStringUntil('\n');
      tempFreak = receivedData.toDouble();
      if(tempFreak != targetFreak){
        targetFreak = tempFreak;
        return tempFreak;
      }
    }

    size_t bytesIn = 0;
    esp_err_t result = i2s_read(I2S_PORT, sBuffer, bufferLen * sizeof(int16_t), &bytesIn, portMAX_DELAY);

    if (result == ESP_OK && bytesIn > 0) {
      int samples_read = bytesIn / sizeof(int16_t);

      // Process the envelope on the audio data
      processEnvelope(sBuffer, samples_read);

      // Only proceed with FFT and frequency calculation if envelope exceeds threshold
      if (envelope > envelopeThreshold) {
        // FFT computation
        // result = i2s_read(I2S_PORT, sBuffer, bufferLen * sizeof(int16_t), &bytesIn, portMAX_DELAY);
        // samples_read = bytesIn / sizeof(int16_t);
        for (int i = 0; i < samples_read; i++) {
          vReal[i] = sBuffer[i];
          vImag[i] = 0;
        }

        FFT.windowing(FFT_WIN_TYP_HAMMING, FFT_FORWARD);
        FFT.compute(FFT_FORWARD);
        FFT.complexToMagnitude();

        // Use HPS instead of just FFT.majorPeak();
        double hpsFreq = findHPS(vReal, bufferLen / 2, 44100, bufferLen);

        // Filter out of range values or outliers
        if (hpsFreq > 90 && hpsFreq < 500) {
          double smoothedFreq = rollingAverage(hpsFreq);
          totalFrequency += smoothedFreq;
          numReadings++;

          Serial.print("HPS Frequency (raw): ");
          Serial.println(hpsFreq);
          Serial.print("Smoothed Frequency: ");
          Serial.println(smoothedFreq);
        }

        break;
      }
    }
  }

  // Calculate and display the average frequency if envelope was above threshold
  if (numReadings > 0) {
    double averageFrequency = totalFrequency / numReadings;
    if((averageFrequency < 445 && averageFrequency > 90)){
      // Print average frequency value for plotting (Serial Plotter compatible)
      Serial.print("averageFrequency:");
      SerialBT.println((int)averageFrequency);
      Serial.println(averageFrequency);
    }else
      averageFrequency = 0;

    return averageFrequency;
  }
}
#pragma endregion

#pragma region Motor Functionality 
void rotate(int angle) {
  // motor controls:
  //   90 = neutral (no motion)
  // < 90 = turns one direction at a given speed
  // > 90 = turns in the opposite direction at a given speed
  int direction = (angle > 0) ? 135 : 45;

  // how much we want to turn scaled at a range from 0 to 1000
  int turningFactor = map(abs(angle), 0, 360, 100, 1000);
  
  servoFS5.write(direction);
  delay(turningFactor);
  servoFS5.write(90); // set to neutral or stop
  delay(1000);
}

void turnMotor(float freak, float targetFreak){
  int error = targetFreak - freak;
  int scale = 5;
  
  int angle = scale * error;
  int constrain = constrain(angle, -180, 180);
  
  if (abs(angle) > 3) {
      rotate(constrain);
      
      Serial.println(String("Current Freq: ") + freak + " | Target Freq: "
       + targetFreak + " | Error: " + error + " | Angle: " + constrain);
  } else{
      // Serial.println("Adjustment too small - holding position");
      servoFS5.write(90);
  }
}
#pragma endregion

void setup() {
  Serial.begin(115200);
  SerialBT.begin("AutoTuningDrumKey");
  pinMode(BLUE_LED, OUTPUT);
  allocateBuffers(bufferLen);
  i2s_install();
  i2s_setpin();

  servoFS5.attach(PWM_PIN);
}
void loop() {
  if (SerialBT.hasClient()) {
    //Debug Mic On and off:
    if(targetFreak > 0){
      Serial.println("Mic ON!");
    }else
      Serial.println("Mic OFF!");

    // Check for new Bluetooth data
    if (SerialBT.available()) {
      String receivedData = SerialBT.readStringUntil('\n');
      targetFreak = receivedData.toDouble();

      Serial.println("Received Target Frequency: " + receivedData);
    }

    // If frequency is 0 or lower, don't run mic or motor logic
    if (targetFreak <= 0) {
      digitalWrite(BLUE_LED, LOW);
      servoFS5.write(90); // Stop motor
      return;
    }

    // Run Mic and Motor Logic
    digitalWrite(BLUE_LED, HIGH);
    double freak = recordAndCalculateAverage();
    if(freak > 0)
      turnMotor(freak, targetFreak);

    delay(1000);

  } else {
    digitalWrite(BLUE_LED, LOW);
    servoFS5.write(90); 
  }
}