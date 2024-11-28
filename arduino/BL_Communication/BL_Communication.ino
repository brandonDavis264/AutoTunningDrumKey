#include "BluetoothSerial.h"

// Check if Bluetooth is available
#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

// Check Serial Port Profile
#if !defined(CONFIG_BT_SPP_ENABLED)
#error Serial Port Profile for Bluetooth is not available or not enabled. It is only available for the ESP32 chip.
#endif

BluetoothSerial SerialBT;

int BLUE_LED = 2;
bool hasSentMessage = false;

void setup() {
  pinMode(BLUE_LED, OUTPUT);
  Serial.begin(115200);
  SerialBT.begin("ESP32_SERVER");  // Bluetooth device name
  Serial.println("Bluetooth Server started, waiting for connection...");
}

void loop() {
  if (SerialBT.hasClient()) {

    // Toggle blue LED to verify connection
    digitalWrite(BLUE_LED, HIGH);
    delay(1000);
    digitalWrite(BLUE_LED, LOW);
    delay(1000);

    // Check if data is available from the app
    if (SerialBT.available()) {
      String receivedMessage = SerialBT.readString();  // Read the incoming data
      Serial.println(receivedMessage);  // Print to Serial Monitor

      // Respond to the app
      String response = "Sent from ESP (Server) to App (Client)";
      SerialBT.write((uint8_t*)response.c_str(), response.length());
    }
  }
}
