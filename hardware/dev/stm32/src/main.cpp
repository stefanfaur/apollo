#include <Arduino.h>
#include "MessageProtocol.h"
#include "STM32DebugModule.h"
#include "LockController.h"
#include "SensorManager.h"

// Define Serial2 for UART2 communication
HardwareSerial Serial2(PA3, PA2);

// Pin definitions
#define LED_PIN           PC13  // Built-in LED on board
#define LOCK_PIN          PB12
#define MOTION_SENSOR_PIN PA0
#define DOOR_SENSOR_PIN   PA1

// Module instances
STM32DebugModule debugModule;
LockController lockController(LOCK_PIN);
SensorManager sensorManager;

// Timestamp tracking
unsigned long lastMotionCheck = 0;
unsigned long lastStatusSend = 0;
const unsigned long MOTION_CHECK_INTERVAL = 1000;  // Check motion sensor every 1 second
const unsigned long STATUS_SEND_INTERVAL = 5000;   // Send status update every 5 seconds

// Function declarations
void handleUnlockCommand();
void checkSensors();
void sendSensorData(int sensorId, int value);

void setup() {
  // Initialize Serial for debugging
  Serial.begin(115200);
  delay(100);
  Serial.println("STM32 board initializing...");

  // Initialize Serial2 to communicate with the AMB82 board
  Serial2.begin(9600);
  delay(100);
  Serial.println("Serial2 initialized for communication with AMB82");
  
  // Initialize built-in LED
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, HIGH);  // Turn off LED (active-low)
  
  // Initialize Debug Module
  debugModule.begin();
  
  // Initialize other components
  lockController.begin();
  sensorManager.begin();
  
  // Add sensors to manager
  sensorManager.addSensor(0, MOTION_SENSOR_PIN, INPUT);      // Motion sensor
  sensorManager.addSensor(1, DOOR_SENSOR_PIN, INPUT_PULLUP); // Door sensor with pullup
  
  Serial.println("STM32 board initialized. Starting main loop...");
  debugModule.log("System initialized and ready");
}

void loop() {
  // Update modules
  debugModule.update();
  lockController.update();
  
  // Check sensors periodically
  unsigned long currentMillis = millis();
  if (currentMillis - lastMotionCheck >= MOTION_CHECK_INTERVAL) {
    lastMotionCheck = currentMillis;
    checkSensors();
  }
  
  // Check for incoming messages from AMB82
  if (Serial2.available() > 0) {
    Message incoming;
    if (readMessage(Serial2, incoming)) {
      // Notify the debug module about the received message
      debugModule.notifyMessageReceived(incoming.command, incoming.length, incoming.payload);
      
      // Process based on command type
      switch (incoming.command) {
        case CMD_UNLOCK:
          debugModule.log("Unlock command received from AMB82");
          handleUnlockCommand();
          break;
          
        case CMD_ACK:
          debugModule.log("Acknowledgment received from AMB82");
          break;
          
        default:
          Serial.print("Received unhandled command: 0x");
          Serial.println(incoming.command, HEX);
          break;
      }
    }
  }
  
  // Small delay to avoid hogging the CPU
  delay(10);
}

void handleUnlockCommand() {
  Serial.println("Unlock command received, activating lock");
  
  // Activate the door lock
  lockController.unlock(3000);  // Unlock for 3 seconds
  
  // Turn on LED to indicate unlock
  digitalWrite(LED_PIN, LOW);  // LED on (active LOW)
  
  // Send acknowledgment back to AMB82
  uint8_t emptyPayload[1] = {0};
  sendMessage(Serial2, CMD_ACK, emptyPayload, 0);
}

void checkSensors() {
  // Check motion sensor
  int motionValue = sensorManager.readSensor(0);
  if (motionValue > 500) {  // Threshold for motion detection
    debugModule.log("Motion detected");
    
    // Send motion event to AMB82
    uint8_t payload[3] = {
      0x01,  // Event type: motion detected
      (uint8_t)(motionValue >> 8),   // High byte
      (uint8_t)(motionValue & 0xFF)  // Low byte
    };
    sendMessage(Serial2, CMD_SENSOR_EVENT, payload, 3);
    
    // Request video recording start
    uint8_t emptyPayload[1] = {0};
    sendMessage(Serial2, CMD_START_VIDEO, emptyPayload, 0);
  }
  
  // Check door sensor
  int doorValue = sensorManager.readSensor(1);
  if (doorValue == LOW) {  // Door open (LOW when using INPUT_PULLUP)
    debugModule.log("Door open detected");
    
    // Send door event to AMB82
    uint8_t payload[3] = {
      0x02,  // Event type: door opened
      0x00,  // High byte
      0x01   // Low byte
    };
    sendMessage(Serial2, CMD_SENSOR_EVENT, payload, 3);
    
    // Request video recording start
    uint8_t emptyPayload[1] = {0};
    sendMessage(Serial2, CMD_START_VIDEO, emptyPayload, 0);
  }
}

void sendSensorData(int sensorId, int value) {
  uint8_t payload[3] = {
    (uint8_t)sensorId,
    (uint8_t)(value >> 8),   // High byte
    (uint8_t)(value & 0xFF)  // Low byte
  };
  sendMessage(Serial2, CMD_SENSOR_DATA, payload, 3);
}
