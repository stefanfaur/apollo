#include "MqttClient.h"
#include <ArduinoJson.h>

extern const char* MQTT_UNLOCK_TOPIC;
extern const char* HARDWARE_ID;
extern const char* DEVICE_TYPE;

MqttClient::MqttClient(const char* mqttBroker, int mqttPort) : client(wifiClient) {
  this->mqttBroker = String(mqttBroker);
  this->mqttPort = mqttPort;
  this->connected = false;
  this->lastReconnectAttempt = 0;
  this->lastConnectionAttempt = 0;
  this->reconnectAttempts = 0;
  this->_topicsSubscribed = false;
}

bool MqttClient::begin(const char* clientId) {
  debugPrint("MqttClient: Initializing...");
  
  this->clientId = String(clientId);
  
  // !!!!! Configure WiFiClient for non-blocking mode with timeout
  wifiClient.setNonBlockingMode();
  wifiClient.setRecvTimeout(CONNECT_TIMEOUT_MS);
  
  debugPrint("MqttClient: WiFiClient configured for non-blocking mode");
  
  // Configure MQTT client
  client.setServer(mqttBroker.c_str(), mqttPort);
  client.setKeepAlive(30);
  client.setSocketTimeout((CONNECT_TIMEOUT_MS + 999) / 1000);
  
  // AMB82-specific: Disable waiting for ACK to prevent blocking
  #ifdef MQTT_PCN006_SUPPORT_WAIT_FOR_ACK
  client.waitForAck(0);
  debugPrint("MqttClient: Disabled waitForAck for non-blocking operation");
  #endif
  
  debugPrint("MqttClient: MQTT server configured");
  debugPrintStatus();
  
  // No background thread – updates will occur from main loop
  return true;
}

void MqttClient::setCallback(void (*callback)(char* topic, uint8_t* payload, unsigned int length)) {
  client.setCallback(callback);
  debugPrint("MqttClient: Callback function set");
}

bool MqttClient::connectToMqttBroker() {
  debugPrint("MqttClient: Attempting to connect to MQTT broker...");
  
  // Check WiFi connection first
  if (WiFi.status() != WL_CONNECTED) {
    debugPrint("MqttClient: ERROR - WiFi not connected, skipping MQTT connection");
    return false;
  }
  
  // Ensure WiFiClient is properly configured for non-blocking operation
  wifiClient.setNonBlockingMode();
  wifiClient.setRecvTimeout(CONNECT_TIMEOUT_MS);
  
  char buffer[128];
  snprintf(buffer, sizeof(buffer), "MqttClient: Connecting to %s:%d with client ID: %s", 
           mqttBroker.c_str(), mqttPort, clientId.c_str());
  debugPrint(buffer);
  
  debugPrint("MqttClient: Starting non-blocking connection attempt...");
  
  // AMB82-specific: Make sure waitForAck is disabled before connect
  #ifdef MQTT_PCN006_SUPPORT_WAIT_FOR_ACK
  client.waitForAck(0);
  #endif
  
  // Use simple connect call
  // TODO: should make this non-blocking
  bool result = client.connect(clientId.c_str());
  
  if (result) {
    debugPrint("MqttClient: Successfully connected to MQTT broker");
    connected = true;
    reconnectAttempts = 0;
    
    // Re-enable waitForAck for normal operations after connection
    #ifdef MQTT_PCN006_SUPPORT_WAIT_FOR_ACK
    client.waitForAck(1);
    #endif
    
    // Send a minimal hello message (hardwareId, deviceType)
    debugPrint("MqttClient: Sending hello message...");
    JsonDocument helloDoc;
    helloDoc["hardwareId"] = HARDWARE_ID;
    helloDoc["deviceType"] = DEVICE_TYPE;
    String helloPayload;
    serializeJson(helloDoc, helloPayload);
    bool helloResult = client.publish("devices/hello", helloPayload.c_str());
    if (helloResult) {
      debugPrint("MqttClient: Hello message sent successfully");
    } else {
      debugPrint("MqttClient: WARNING - Failed to send hello message (non-critical)");
    }
    
    return true;
  } else {
    int state = client.state();
    snprintf(buffer, sizeof(buffer), "MqttClient: Connection failed with state: %d", state);
    debugPrint(buffer);
    
    // Decode MQTT error states
    switch (state) {
      case -4:
        debugPrint("MqttClient: Error - Connection timeout");
        break;
      case -3:
        debugPrint("MqttClient: Error - Connection lost");
        break;
      case -2:
        debugPrint("MqttClient: Error - Connect failed");
        break;
      case -1:
        debugPrint("MqttClient: Error - Disconnected");
        break;
      case 1:
        debugPrint("MqttClient: Error - Bad protocol version");
        break;
      case 2:
        debugPrint("MqttClient: Error - Bad client ID");
        break;
      case 3:
        debugPrint("MqttClient: Error - Server unavailable");
        break;
      case 4:
        debugPrint("MqttClient: Error - Bad username/password");
        break;
      case 5:
        debugPrint("MqttClient: Error - Not authorized");
        break;
      default:
        debugPrint("MqttClient: Error - Unknown error");
        break;
    }
    
    connected = false;
    debugPrint("MqttClient: Connection attempt completed (failed) - system remains responsive");
    return false;
  }
}

bool MqttClient::subscribe(const char* topic) {
  debugPrint("MqttClient: Attempting to subscribe...");
  if (!client.connected()) {
    debugPrint("MqttClient: Cannot subscribe - not connected to broker");
    return false;
  }

  bool result = client.subscribe(topic);
  if (result) {
    // Process outgoing subscription packet immediately
    client.loop();
    delay(50);
  }

  char buffer[128];
  snprintf(buffer, sizeof(buffer), "MqttClient: %s to subscribe to topic: %s", result ? "Successfully" : "Failed", topic);
  debugPrint(buffer);
  return result;
}

bool MqttClient::publishNotification(const char* topic, const char* hardwareId, const char* eventType, 
                                    const char* description, const char* mediaUrl, const char* timestamp) {
  if (!client.connected()) {
    debugPrint("MqttClient: Cannot publish - not connected to broker");
    return false;
  }
  
  JsonDocument doc;
  doc["hardwareId"] = hardwareId;
  doc["eventType"] = eventType;
  doc["description"] = description;
  doc["mediaUrl"] = mediaUrl;
  doc["timestamp"] = timestamp;
  
  String message;
  serializeJson(doc, message);
  
  char buffer[256];
  snprintf(buffer, sizeof(buffer), "MqttClient: Publishing to %s: %s", topic, message.c_str());
  debugPrint(buffer);
  
  bool result = client.publish(topic, message.c_str());
  if (result) {
    // Ensure message is actually placed on the wire before leaving critical section
    client.loop();
    delay(50);
  } else {
    debugPrint("MqttClient: client.publish() returned false immediately.");
    debugPrintStatus();
  }
  
  if (!result) {
    debugPrint("MqttClient: Failed to publish message");
    debugPrintStatus();
  }
  
  return result;
}

void MqttClient::update() {
  unsigned long currentTime = millis();
  
  // Check connection status
  if (!client.connected()) {
    connected = false;
    
    // Only attempt reconnection if WiFi is connected
    if (WiFi.status() == WL_CONNECTED) {
      // Don't attempt reconnection too frequently
      if (currentTime - lastReconnectAttempt >= RECONNECT_INTERVAL) {
        lastReconnectAttempt = currentTime;
        
        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
          reconnectAttempts++;
          char buffer[128];
          snprintf(buffer, sizeof(buffer), "MqttClient: Reconnection attempt %d/%d", 
                   reconnectAttempts, MAX_RECONNECT_ATTEMPTS);
          debugPrint(buffer);
          
          // Ensure non-blocking mode before reconnection
          wifiClient.setNonBlockingMode();
          wifiClient.setRecvTimeout(CONNECT_TIMEOUT_MS);
          
          debugPrint("MqttClient: Starting non-blocking reconnection...");
          connectToMqttBroker();
          debugPrint("MqttClient: Reconnection attempt completed - system responsive");
          
        } else {
          // Reset reconnect attempts after a longer delay
          if (currentTime - lastConnectionAttempt >= (RECONNECT_INTERVAL * 4)) {
            debugPrint("MqttClient: Resetting reconnection attempts");
            reconnectAttempts = 0;
            lastConnectionAttempt = currentTime;
          }
        }
      }
    } else {
      debugPrint("MqttClient: WiFi not connected, skipping MQTT reconnection");
    }
  } else {
    // Process MQTT messages only if connected
    client.loop();
    connected = true;

    // Ensure we have subscribed to required topics once connected
    if (!_topicsSubscribed) {
      debugPrint("MqttClient: Attempting initial topic subscription...");
      if (client.subscribe(MQTT_UNLOCK_TOPIC)) {
        debugPrint("MqttClient: Subscribed to unlock topic");
        _topicsSubscribed = true;
      } else {
        debugPrint("MqttClient: Failed to subscribe (will retry)");
      }
    }
  }
}

bool MqttClient::isConnected() {
  return client.connected();
}

int MqttClient::getState() {
  return client.state();
}

void MqttClient::debugPrint(const char* message) {
  Serial.println(message);
}

void MqttClient::debugPrintStatus() {
  char buffer[256];
  snprintf(buffer, sizeof(buffer), "MqttClient Status - Connected: %s, State: %d, WiFi: %s", 
           isConnected() ? "YES" : "NO", 
           getState(),
           WiFi.status() == WL_CONNECTED ? "CONNECTED" : "DISCONNECTED");
  debugPrint(buffer);
  
  if (WiFi.status() == WL_CONNECTED) {
    // Use individual octets instead of toString() for AMB82 compatibility
    IPAddress ip = WiFi.localIP();
    snprintf(buffer, sizeof(buffer), "MqttClient: WiFi IP: %d.%d.%d.%d, RSSI: %ld dBm", 
             ip[0], ip[1], ip[2], ip[3], WiFi.RSSI());
    debugPrint(buffer);
  }
} 