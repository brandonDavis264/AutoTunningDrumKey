#include "arduinoFFT.h"

#define SAMPLES 64              // This value MUST ALWAYS be a power of 2
#define SAMPLING_FREQUENCY 5000 // Sampling frequency in Hz
#define SIGNAL_FREQUENCY 1000  // Signal frequency in Hz
#define AMPLITUDE 100           // Amplitude of the signal

#define SCL_INDEX 0x00
#define SCL_TIME 0x01
#define SCL_FREQUENCY 0x02
#define SCL_PLOT 0x03

double vReal[SAMPLES];  // Real part of the signal
double vImag[SAMPLES];  // Imaginary part of the signal

// Create FFT object
ArduinoFFT<double> FFT = ArduinoFFT<double>(vReal, vImag, SAMPLES, SAMPLING_FREQUENCY);

// Create a lookup table with simulated sine wave data
double lookupTable[SAMPLES] = {
  0,    25,    50,    74,    98,   120,   142,
   162,   180,   197,   212,   225,   236,   244,
   250,   254,   255,   254,   250,   244,   236,
   225,   212,   197,   180,   162,   142,   120,
    98,    74,    50,    25,     0,   -25,   -50,
   -74,   -98,  -120,  -142,  -162,  -180,  -197,
  -212,  -225,  -236,  -244,  -250,  -254,  -255,
  -254,  -250,  -244,  -236,  -225,  -212,  -197,
  -180,  -162,  -142,  -120,   -98,   -74,   -50,
   -25
};

void setup() {
  Serial.begin(115200);  // Start serial communication
  while (!Serial);
  Serial.println("Ready");
}

void loop() {
  /* Build raw data using lookup table */
  double ratio = twoPi * SIGNAL_FREQUENCY / SAMPLING_FREQUENCY; // Fraction of a complete cycle stored at each sample (in radians)

  while(1) {
    for (uint16_t i = 0; i < SAMPLES; i++) {
      vReal[i] = lookupTable[i];  // Use the lookup table as the signal input
      vImag[i] = 0.0;             // Imaginary part must be zeroed for real signals
    }

    /* Print the results of the simulated signal in time domain */
    Serial.println("Data:");
    PrintVector(vReal, SAMPLES, SCL_TIME);
    
    /* Apply windowing function */
    FFT.windowing(FFTWindow::Hamming, FFTDirection::Forward);  // Apply Hamming window to the data
    Serial.println("Weighed data:");
    PrintVector(vReal, SAMPLES, SCL_TIME);

    /* Compute FFT */
    FFT.compute(FFTDirection::Forward);
    Serial.println("Computed Real values:");
    PrintVector(vReal, SAMPLES, SCL_INDEX);
    
    Serial.println("Computed Imaginary values:");
    PrintVector(vImag, SAMPLES, SCL_INDEX);

    /* Compute magnitudes */
    FFT.complexToMagnitude();
    Serial.println("Computed magnitudes:");
    PrintVector(vReal, (SAMPLES >> 1), SCL_FREQUENCY); // Only print half (positive frequencies)

    /* Identify the major peak */
    double x = FFT.majorPeak();
    Serial.print("Major peak at frequency: ");
    Serial.println(x, 6);

  }
}

void PrintVector(double *vData, uint16_t bufferSize, uint8_t scaleType) {
  for (uint16_t i = 0; i < bufferSize; i++) {
    double abscissa;
    /* Print abscissa value */
    switch (scaleType) {
      case SCL_INDEX:
        abscissa = (i * 1.0);
        break;
      case SCL_TIME:
        abscissa = ((i * 1.0) / SAMPLING_FREQUENCY);
        break;
      case SCL_FREQUENCY:
        abscissa = ((i * 1.0 * SAMPLING_FREQUENCY) / SAMPLES);
        break;
    }
    Serial.print(abscissa, 6);
    if (scaleType == SCL_FREQUENCY)
      Serial.print("Hz");
    Serial.print(" ");
    Serial.println(vData[i], 4);
  }
  Serial.println();
}
