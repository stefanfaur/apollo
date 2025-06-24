#ifndef MQTT_CLIENT_H
#define MQTT_CLIENT_H

#include <Arduino.h>
#include <WiFi.h>
#include <PubSubClient.h>

class MqttClient {
public:
  MqttClient(const char* mqttBroker, int mqttPort);
  
  // Initialize MQTT client with client ID (no background thread)
  bool begin(const char* clientId);
  
  // Set the callback function for incoming messages
  void setCallback(void (*callback)(char* topic, uint8_t* payload, unsigned int length));
  
  // Subscribe to a topic
  bool subscribe(const char* topic);
  
  // Publish a notification message
  bool publishNotification(const char* topic, const char* hardwareId, const char* eventType, 
                          const char* title, const char* description, const char* mediaUrl, const char* timestamp);
  
  // Update the MQTT client (call in loop)
  void update();
  
  // Check if client is connected
  bool isConnected();
  
  // Get last error state
  int getState();

  void debugPrint(const char* message);
  void debugPrintStatus();

private:
  bool _topicsSubscribed;
  // MQTT configuration
  String mqttBroker;
  int mqttPort;
  String clientId;
  
  // WiFi and MQTT clients
  WiFiClient wifiClient;
  PubSubClient client;
  
  // Connection management
  bool connected;
  unsigned long lastReconnectAttempt;
  unsigned long lastConnectionAttempt;
  int reconnectAttempts;
  static const unsigned long RECONNECT_INTERVAL = 15000; // 15 seconds
  static const int MAX_RECONNECT_ATTEMPTS = 3;
  static const unsigned long CONNECT_TIMEOUT_MS = 1000; // Max time (ms) we allow connect() to block
  
  // Internal methods
  bool connectToMqttBroker();
};

#endif // MQTT_CLIENT_H 