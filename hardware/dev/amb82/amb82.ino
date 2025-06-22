#include <Arduino.h>
#include "MessageProtocol.h"
#include "VideoHandler.h"
#include "HttpClient.h"
#include "MqttClient.h"
#include "EventLogger.h"
#include "AMB82DebugModule.h"
#include "EventType.h"
#include "EventHandler.h"
#include "WiFiManager.h"

// Default fallback WiFi credentials
const char* DEFAULT_WIFI_SSID = "FRAME";
const char* DEFAULT_WIFI_PASSWORD = "HY04IOABBA8GI";

// MinIO configuration
const char* MINIO_HOST = "192.168.2.125";
const int MINIO_PORT = 9000;
const char* MINIO_BUCKET = "apollo-bucket/uploads/";

// MQTT configuration
const char* MQTT_BROKER = "192.168.2.125";
const int MQTT_PORT = 1883;
const char* MQTT_NOTIFICATION_TOPIC = "devices/notifications";
const char* MQTT_UNLOCK_TOPIC = "devices/commands/unlock";
const char* HARDWARE_ID = "AMB82_001";
const char* MQTT_CLIENT_ID = HARDWARE_ID;

// State variables
bool videoCompletedUploading = false;
String pendingNotificationMessage = "";
bool wifiConfigMode = false;
bool shouldRestart = false;

// Module instances
WiFiManager wifiManager;
VideoHandler videoHandler;
InternalHttpClient httpClient(MINIO_HOST, MINIO_PORT, MINIO_BUCKET);
MqttClient mqttClient(MQTT_BROKER, MQTT_PORT);
EventLogger eventLogger;
AMB82DebugModule debugModule(videoHandler, httpClient, mqttClient, wifiManager);
EventHandler eventHandler(videoHandler, httpClient, mqttClient, eventLogger);

// MQTT callback function for message handling
void mqttCallback(char* topic, uint8_t* payload, unsigned int length) {
  Serial.print("MQTT message received on topic: ");
  Serial.println(topic);
  
  // Convert payload to string for easier processing
  String message = "";
  for (unsigned int i = 0; i < length; i++) {
    message += (char)payload[i];
  }
  
  Serial.print("Message content: ");
  Serial.println(message);

  // Log the MQTT message
  char eventDesc[48];
  snprintf(eventDesc, sizeof(eventDesc), "MQTT: %s - %s", topic, message.c_str());
  eventLogger.logEvent(0, eventDesc);
  
  // Handle unlock command
  if (String(topic) == MQTT_UNLOCK_TOPIC) {
    Serial.println("Unlock command received, forwarding to STM32");
    
    // Send the unlock command to the STM32 via Serial2
    uint8_t emptyPayload[1] = {0};
    sendMessage(Serial2, CMD_UNLOCK, emptyPayload, 0);
  }
}

bool initializeWiFiCredentials() {
  // Initialize WiFi manager
  if (!wifiManager.begin()) {
    Serial.println("ERROR: Failed to initialize WiFi manager");
    return false;
  }
  
  // Check if we're in AP configuration mode
  if (wifiManager.isConfigPortalActive()) {
    Serial.println("WiFi configuration mode active");
    Serial.println("Connect to 'SmartDoorLock_Setup' AP (password: 'configure')");
    Serial.println("Then visit http://192.168.1.1 to configure WiFi");
    wifiConfigMode = true;
    return false; // Don't proceed with normal operations
  }
  
  // If we reach here, WiFi is connected successfully
  if (wifiManager.isConnected()) {
    Serial.println("WiFi connection established successfully");
    return true;
  }
  
  // This shouldn't happen with the new WiFiManager logic, but handle as fallback
  Serial.println("ERROR: Unexpected WiFiManager state");
  return false;
}

void setup() {
  // Initialize Serial for debugging
  Serial.begin(115200);
  delay(1000);
  Serial.println("AMB82 board initializing...");

  // Initialize Serial2 to communicate with the stm32 board
  Serial2.begin(9600);
  delay(1000);
  Serial.println("Serial2 initialized for communication with STM32");
  
  // Initialize Debug Module
  debugModule.begin();
  
  // Initialize EventLogger - AFTER Serial initialization
  eventLogger.begin(true); // Enable serial monitoring
  
  // Initialize VideoHandler
  if (!videoHandler.begin()) {
    Serial.println("WARNING: VideoHandler initialization failed");
    debugModule.log("VideoHandler initialization failed");
  }
  
  // Initialize WiFi credentials
  if (initializeWiFiCredentials()) {
    // WiFi is now connected, initialize modules that depend on it
    if (httpClient.begin()) {
      Serial.println("HTTP client initialized successfully");
      
      // Set up MQTT client with callback first
      mqttClient.setCallback(mqttCallback);
      
      // Initialize client configuration
      if (mqttClient.begin(MQTT_CLIENT_ID)) {
        Serial.println("MQTT client configured successfully");
        debugModule.log("MQTT client configured successfully");
      } else {
        Serial.println("Failed to configure MQTT client");
        debugModule.log("Failed to configure MQTT client");
      }
    } else {
      Serial.println("ERROR: Failed to initialize HTTP client");
      debugModule.log("Failed to initialize HTTP client");
    }
  }
  
  // Initialize EventHandler (AFTER dependencies are initialized)
  eventHandler.begin();
  
  if (wifiConfigMode) {
    Serial.println("AMB82 board initialized in WiFi configuration mode");
  } else {
    Serial.println("AMB82 board initialized. Starting message reception...");
  }
}

// Clean shutdown when needed
void cleanup() {
  // Ensure video components are properly released
  videoHandler.end();
  
  // Any other cleanup tasks
  Serial.println("AMB82 board resources released.");
}

void handleStartVideo() {
  Serial.println("Start video command received");
  debugModule.log("Start video command received from STM32");
  
  // Clear any previous events
  eventLogger.clear();
  
  // Start a 10-second video recording - enable upload for STM32-triggered recordings
  if (videoHandler.startRecording(10000, true)) {
    // Send acknowledgement back to STM32
    uint8_t emptyPayload[1] = {0};
    sendMessage(Serial2, CMD_ACK, emptyPayload, 0);
  }
}

void handleSensorData(const Message &msg) {
  if (msg.length == 2) {
    int sensorValue = (msg.payload[0] << 8) | msg.payload[1];
    Serial.print("Sensor Data - Value: ");
    Serial.println(sensorValue);
    
    // Log this event
    eventLogger.logEvent(sensorValue, "Motion sensor triggered");
  } else {
    Serial.println("Sensor Data - Invalid length");
  }
}

void handleSensorEvent(const Message &msg) {
  // Process sensor events
  if (msg.length >= 1) {
    // Cast the first byte to our EventType enum
    EventType eventType = static_cast<EventType>(msg.payload[0]);
    
    int eventValue = 0;
    // If there's a value included, parse it (starting from the second byte)
    if (msg.length >= 3) {
      eventValue = (msg.payload[1] << 8) | msg.payload[2];
    }
    
    // Delegate event handling to the EventHandler module
    eventHandler.handleEvent(eventType, eventValue);

  } else {
    Serial.println("Sensor Event - Invalid length (must be at least 1 byte for type)");
  }
}

void loop() {
  // Handle WiFi configuration mode
  if (wifiConfigMode) {
    wifiManager.handleConfigurationRequests();
    
    // Check if configuration is complete
    if (wifiManager.isConfigurationComplete()) {
      Serial.println("WiFi configuration complete - restarting in 3 seconds...");
      delay(3000);
      // Restart the device to apply new configuration
      Serial.println("Restarting...");
      delay(500);
      // On AMB82, we can use NVIC_SystemReset() for reset
      NVIC_SystemReset();
    }
    
    // Still allow debug module to work in configuration mode
    debugModule.update();
    
    delay(10);
    return; // Skip normal loop operations while in config mode
  }
  
  // Normal operation mode
  // MQTT updates are now handled in a dedicated background thread
  // Otherwise we have problems because the PubSubClient is inherently blocking
  
  // Topic subscription is now handled internally by the MQTT client thread
  
  // Update other modules
  videoHandler.update();
  debugModule.update();  // Process debug commands
  eventHandler.update(); // Update the event handler state machine

  // currently disabled reading from Serial2
  eventLogger.update();
  
  // Check if there is data available on Serial2
  if (Serial2.available() > 0) {
    Message incoming;
    // Use the MessageProtocol lib to decode a message
    if (readMessage(Serial2, incoming)) {
      // Process based on command type
      switch (incoming.command) {
        case CMD_START_VIDEO:
          handleStartVideo();
          break;
          
        case CMD_SENSOR_DATA:
          handleSensorData(incoming);
          break;
          
        case CMD_SENSOR_EVENT:
          handleSensorEvent(incoming);
          break;
          
        default:
          Serial.print("Received unhandled command: 0x");
          Serial.println(incoming.command, HEX);
          break;
      }
    } else {
      // If it's not a proper Message protocol message, we can try to read it as plain text
      // for logging purposes only (after main code has processed it)
      char message[31];  // 30 chars + null terminator
      int bytesRead = 0;
      
      while (Serial2.available() > 0 && bytesRead < 30) {
        char c = Serial2.read();
        if (c == '\n') break;
        message[bytesRead++] = c;
      }
      
      if (bytesRead > 0) {
        message[bytesRead] = '\0';
        // Pass the message to the EventLogger
        eventLogger.processSerialMessage(message);
      }
    }
  }
  
  // Small delay to avoid hogging the CPU
  delay(10);
}
