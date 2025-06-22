#include <Arduino.h>
#include "MessageProtocol.h"
#include "STM32DebugModule.h"
#include "debug_serial.h"
#include "fingerprint_sensor.h"
#include "LockController.h"
#include "SensorManager.h"
#include "Buzzer.h"

// Define Serial2 for UART2 communication to AMB82
HardwareSerial Serial2(PA3, PA2);

// Forward declaration for status helper
void sendStatusMessage(uint8_t command, uint8_t* payload, uint8_t length);

// Pin definitions
#define LED_PIN           PC13  // Built-in LED on board
#define LOCK_PIN          PB12
#define MOTION_SENSOR_PIN  PA0
#define DOOR_SENSOR1_PIN   PA1  // Frame reed switch
#define DOOR_SENSOR2_PIN   PA4  // Handle reed switch

// Module instances
STM32DebugModule debugModule;
LockController lockController(LOCK_PIN);
SensorManager sensorManager;
FingerprintSensor fingerprintSensor(&Serial); // Use default Serial (USART1)
Buzzer buzzer(PB10); // Passive buzzer

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
  // Initialize Software Debug Serial first
  debug_serial_init();
  debug_println("STM32 board initializing...");

  // Initialise hardware Serial used for fingerprint sensor
  Serial.begin(57600);
  delay(100);
  debug_println("Serial (Fingerprint) initialized");

  // Initialize Serial2 to communicate with the AMB82 board
  Serial2.begin(9600);
  delay(100);
  debug_println("Serial2 initialized for communication with AMB82");
  
  // Initialize built-in LED
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, HIGH);  // Turn off LED (active-low)
  
  // Initialize Debug Module
  debugModule.begin();
  
  // Initialize Buzzer
  buzzer.begin();
  
  // Initialize fingerprint sensor
  if (fingerprintSensor.begin()) {
    debug_println("Fingerprint sensor detected.");
  } else {
    debug_println("ERROR: No fingerprint sensor found!");
  }
  
  // Initialize other components
  lockController.begin();
  sensorManager.begin();
  
  // Add sensors to manager
  sensorManager.addSensor(0, MOTION_SENSOR_PIN, INPUT);        // PIR motion sensor (analog output)
  sensorManager.addSensor(1, DOOR_SENSOR1_PIN, INPUT_PULLUP);  // Door sensor #1 (reed switch)
  sensorManager.addSensor(2, DOOR_SENSOR2_PIN, INPUT_PULLUP);  // Door sensor #2 (reed switch)
  
  debug_println("STM32 board initialized. Starting main loop...");
  debugModule.log("System initialized and ready");
}

void loop() {
  // Update modules
  debugModule.update();
  lockController.update();
  fingerprintSensor.update();
  
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
          
        case CMD_ENROLL_START:
          if (incoming.length == 1) {
            uint8_t eid = incoming.payload[0];
            debugModule.log("Enroll start received");
            fingerprintSensor.startEnrollment(eid);
          }
          break;
          
        default:
          get_debug_serial().print("Received unhandled command: 0x");
          get_debug_serial().println(incoming.command, HEX);
          break;
      }
    }
  }
  
  // Small delay to avoid hogging the CPU
  delay(10);
}

void handleUnlockCommand() {
  debug_println("Unlock command received, activating lock");
  
  // Activate the door lock
  lockController.unlock(3000);  // Unlock for 3 seconds
  
  // Turn on LED to indicate unlock
  digitalWrite(LED_PIN, HIGH);  // LED on 
  
  // Send acknowledgment back to AMB82
  uint8_t emptyPayload[1] = {0};
  sendMessage(Serial2, CMD_ACK, emptyPayload, 0);
}

void checkSensors() {
  /********************
   * Motion sensor
   *******************/
  static bool prevMotionActive = false;
  int motionValue = sensorManager.readSensor(0);
  bool motionActive = motionValue > 500;  // Threshold for motion detection (tune as required)

  if (motionActive && !prevMotionActive) {
    debugModule.log("Motion detected (PIR)");

    // Send motion event to AMB82
    uint8_t payload[3] = {
      0x01,  // Event type: motion detected
      (uint8_t)(motionValue >> 8),   // High byte (raw value for debugging)
      (uint8_t)(motionValue & 0xFF)  // Low byte
    };
    sendMessage(Serial2, CMD_SENSOR_EVENT, payload, 3);

    // Request video recording start
    uint8_t emptyPayload[1] = {0};
    sendMessage(Serial2, CMD_START_VIDEO, emptyPayload, 0);
  }
  prevMotionActive = motionActive;

  /********************
   * Door sensors (reed switches)
   *******************/
  static int prevDoor1State = HIGH;
  static int prevDoor2State = HIGH;

  int door1State = sensorManager.readSensor(1);
  int door2State = sensorManager.readSensor(2);

  // Door 1
  if (door1State == LOW && prevDoor1State == HIGH) { // transition: closed -> open
    debugModule.log("Door 1 opened");

    uint8_t payload[3] = {
      0x02,  // Event type: door1 opened
      0x00,
      0x01
    };
    sendMessage(Serial2, CMD_SENSOR_EVENT, payload, 3);

    uint8_t emptyPayload[1] = {0};
    sendMessage(Serial2, CMD_START_VIDEO, emptyPayload, 0);
  }
  prevDoor1State = door1State;

  // Door 2
  if (door2State == LOW && prevDoor2State == HIGH) { // transition: closed -> open
    debugModule.log("Door 2 opened");

    uint8_t payload[3] = {
      0x03,  // Event type: door2 opened
      0x00,
      0x01
    };
    sendMessage(Serial2, CMD_SENSOR_EVENT, payload, 3);

    uint8_t emptyPayload[1] = {0};
    sendMessage(Serial2, CMD_START_VIDEO, emptyPayload, 0);
  }
  prevDoor2State = door2State;
}

void sendSensorData(int sensorId, int value) {
  uint8_t payload[3] = {
    (uint8_t)sensorId,
    (uint8_t)(value >> 8),   // High byte
    (uint8_t)(value & 0xFF)  // Low byte
  };
  sendMessage(Serial2, CMD_SENSOR_DATA, payload, 3);
}

// Helper to send status back to AMB82
void sendStatusMessage(uint8_t command, uint8_t* payload, uint8_t length) {
  sendMessage(Serial2, command, payload, length);
}
