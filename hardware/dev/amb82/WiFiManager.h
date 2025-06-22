#ifndef WIFI_MANAGER_H
#define WIFI_MANAGER_H

#include <Arduino.h>
#include <WiFi.h>
#include "AmebaFatFS.h"

class WiFiManager {
public:
  WiFiManager();
  
  // Initialize the WiFi manager
  bool begin();
  
  // Check if WiFi is connected
  bool isConnected();
  
  // Check if configuration portal is active
  bool isConfigPortalActive();
  
  // Update WiFi manager (call in loop)
  void update();
  
  // Clear stored credentials
  void clearCredentials();
  
  // Public WiFi connection method
  bool connectToWiFi(const char* ssid, const char* password);
  
  // Compatibility methods for old API
  bool hasStoredCredentials();
  bool startConfigurationMode();
  void handleConfigurationRequests();
  bool isConfigurationComplete();
  bool storeCredentials(const char* ssid, const char* password);

private:
  AmebaFatFS fs;
  WiFiServer server;
  bool apActive;
  
  // File paths for storing credentials
  static const char* CREDENTIALS_FILE;
  static const char* AP_SSID;
  static const char* AP_PASSWORD;
  
  // Core functionality
  bool startConfigPortal();
  void handleClient();
  bool connectToWiFi();
  
  // Credential management
  bool saveCredentials(const String& ssid, const String& password);
  bool loadCredentials();
  
  // HTTP response methods (new improved versions)
  void sendSetupPageWithHeaders(WiFiClient& client);
  void sendSuccessPageWithHeaders(WiFiClient& client);
  void sendErrorPageWithHeaders(WiFiClient& client, const String& errorMessage);
  
  // HTML generation methods
  String generateSetupPageHTML();
  String generateSuccessPageHTML();
  String generateErrorPageHTML(const String& errorMessage);
  
  // Legacy HTTP response methods (deprecated but kept for compatibility)
  void sendSetupPage(WiFiClient& client);
  void sendSuccessPage(WiFiClient& client);
  void sendErrorPage(WiFiClient& client, const String& errorMessage);
  void sendHTTPHeader(WiFiClient& client);
  
  // Helper methods
  String extractValue(const String& data, const String& key);
  String urlDecode(const String& str);
  unsigned char h2int(char c);
  void debugPrint(const String& message);
};

#endif // WIFI_MANAGER_H 