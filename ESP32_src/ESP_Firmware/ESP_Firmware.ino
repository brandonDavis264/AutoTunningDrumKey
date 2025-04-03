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
int bufferLen = 4096; //Sample Size
//Desired frequency
double targetFreak = 0;
// FFT Object
ArduinoFFT<double> FFT = ArduinoFFT<double>(NULL, NULL, bufferLen, 44100);

// Servo Proccessing Variables 
Servo servoFS5;
int servPos = 0;
const int stopPulse = 90;
int maxTurnAngle = 90; // limit on how much we can turn the motor at a time

// Envelope Follower variables
float envelope = 0.0f;
const float alpha = 0.05f;  // Low-pass filter smoothing factor
const float envelopeThreshold = 700.0f;  // Envelope threshold for calculation


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

  Serial.print("Target frequency: ");
  Serial.println(targetFreak);
  // Serial.print("Reached Threshold: ");
  // Serial.println(envelopeThreshold: );
  // // Print envelope value for plotting (Serial Plotter compatible)
  // Serial.print("envelope:");
  // Serial.println(envelope);
}

double recordAndCalculateAverage() {
  unsigned long startTime = millis();
  double totalFrequency = 0;
  int numReadings = 0;
  //Polling Makes The frequncy show up after
  while (millis() - startTime < 1) {
    size_t bytesIn = 0;
    esp_err_t result = i2s_read(I2S_PORT, sBuffer, bufferLen * sizeof(int16_t), &bytesIn, portMAX_DELAY);

    if (result == ESP_OK && bytesIn > 0) {
      int samples_read = bytesIn / sizeof(int16_t);

      // Process the envelope on the audio data
      processEnvelope(sBuffer, samples_read);

      // Only proceed with FFT and frequency calculation if envelope exceeds threshold
      if (envelope > envelopeThreshold) {
        // FFT computation
        for (int i = 0; i < samples_read; i++) {
          vReal[i] = sBuffer[i];
          vImag[i] = 0;
        }

        FFT.windowing(FFT_WIN_TYP_HAMMING, FFT_FORWARD);
        FFT.compute(FFT_FORWARD);
        FFT.complexToMagnitude();
        totalFrequency += FFT.majorPeak();

        numReadings++;
        break;
      }
    }
  }

  // Calculate and display the average frequency if envelope was above threshold
  if (numReadings > 0) {
    double averageFrequency = totalFrequency / numReadings;
    
    // Print average frequency value for plotting (Serial Plotter compatible)
    // Serial.print("averageFrequency:");
    // SerialBT.println(averageFrequency);
    // Serial.println(averageFrequency);
  

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
  int scale = 1;
  
  int angle = scale * error;
  int constrain = constrain(angle, -90, 90);
  
  if (abs(angle) > 3) {
      rotate(constrain);
      
      // Serial.println(String("Current Freq: ") + freak + " | Target Freq: "
      //  + targetFreak + " | Error: " + error + " | Angle: " + constrain);
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
    digitalWrite(BLUE_LED, HIGH);
    double freak = recordAndCalculateAverage();
    //double newTargetFreak = 0; // Declare outside the if block

    if (SerialBT.available()) {
      String receivedData = SerialBT.readStringUntil('\n');
      targetFreak = receivedData.toDouble();
      Serial.print("New target frequency received: ");
      Serial.println(targetFreak);
    }

    turnMotor(freak, targetFreak);
    delay(200); // Allow servo time to move
  }else {
    digitalWrite(BLUE_LED, LOW);
  }
}