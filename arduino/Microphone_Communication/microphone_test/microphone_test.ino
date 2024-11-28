#include <driver/i2s.h>
#include <arduinoFFT.h>
#include "BluetoothSerial.h"

#define I2S_WS 4
#define I2S_SD 15
#define I2S_SCK 23
#define I2S_PORT I2S_NUM_0

BluetoothSerial SerialBT;
int bufferLen = 4096; // Reduced buffer size
int BLUE_LED = 2;

// Dynamic Buffers
int16_t* sBuffer;
double* vReal;
double* vImag;

// FFT Object
ArduinoFFT<double> FFT = ArduinoFFT<double>(NULL, NULL, bufferLen, 44100);

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

void recordAndCalculateAverage() {
  Serial.println("Recording for 1 second...");
  unsigned long startTime = millis();
  double totalFrequency = 0;
  int numReadings = 0;

  while (millis() - startTime < 1000) {
    size_t bytesIn = 0;
    esp_err_t result = i2s_read(I2S_PORT, sBuffer, bufferLen * sizeof(int16_t), &bytesIn, portMAX_DELAY);

    if (result == ESP_OK && bytesIn > 0) {
      int samples_read = bytesIn / sizeof(int16_t);
      for (int i = 0; i < samples_read; i++) {
        vReal[i] = sBuffer[i];
        vImag[i] = 0;
      }
      FFT.windowing(FFT_WIN_TYP_HAMMING, FFT_FORWARD);
      FFT.compute(FFT_FORWARD);
      FFT.complexToMagnitude();
      totalFrequency += FFT.majorPeak();
      numReadings++;
    }
  }

  if (numReadings > 0) {
    double averageFrequency = totalFrequency / numReadings;
    Serial.println("Average Frequency: " + String(averageFrequency) + " Hz");
    SerialBT.println("Average Frequency: " + String(averageFrequency) + " Hz");
  }
}

void setup() {
  Serial.begin(115200);
  SerialBT.begin("ESP32_SERVER");
  pinMode(BLUE_LED, OUTPUT);
  allocateBuffers(bufferLen);
  i2s_install();
  i2s_setpin();
}

void loop() {
  if (SerialBT.hasClient()) {
    digitalWrite(BLUE_LED, HIGH);
    delay(500);
    digitalWrite(BLUE_LED, LOW);
    delay(500);

    if (SerialBT.available()) {
      char command = SerialBT.read();
      if (command == 's') {
        recordAndCalculateAverage();
      }
    }
  }
}
