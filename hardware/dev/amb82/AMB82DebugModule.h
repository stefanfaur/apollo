#ifndef AMB82_DEBUG_MODULE_H
#define AMB82_DEBUG_MODULE_H

#include <Arduino.h>
#include "DebugModule.h"
#include "VideoHandler.h"
#include "InternalHttpClient.h"
#include "MqttClient.h"
#include "WiFiManager.h"
#include <WiFi.h>

class AMB82DebugModule : public DebugModule {
public:
  AMB82DebugModule(VideoHandler& videoHandler, InternalHttpClient& httpClient, MqttClient& mqttClient, WiFiManager& wifiManager);
  
  // Implementation of base class virtual methods
  bool begin() override;
  void update() override;
  void respond(const char* message) override;
  void setEnabled(bool enabled) override;
  bool isEnabled() override;
  void log(const char* message) override;
  bool isReady() override;
  void printHelp() override;

private:
  // Custom command handler type for this class
  using AMB82CommandHandler = CommandResult (AMB82DebugModule::*)(const char* args);
  
  // Custom command structure
  struct Command {
    const char* command;
    const char* description;
    AMB82CommandHandler handler;
  };
  
  // Module references
  VideoHandler& _videoHandler;
  InternalHttpClient& _httpClient;
  MqttClient& _mqttClient;
  WiFiManager& _wifiManager;
  
  // Command buffer
  static const int MAX_COMMAND_LENGTH = 128;
  char commandBuffer[MAX_COMMAND_LENGTH];
  int commandLength;
  
  // Non-blocking WiFiClient for debug operations
  WiFiClient debugClient;
  
  // Record and upload state tracking
  bool _recordAndUpload = false;
  unsigned long _recordStartTime = 0;
  unsigned long _recordDuration = 0;
  
  // Serial command parsing
  void processCommand(const char* command);
  CommandResult processCommandInternal(const char* command);
  
  // Common command handlers
  // Implementing locally since we have a custom Command struct
  CommandResult cmdHelp(const char* args);
  CommandResult cmdVersion(const char* args);
  CommandResult cmdStatus(const char* args);
  
  // AMB82-specific command handlers
  CommandResult cmdRecordVideo(const char* args);
  CommandResult cmdStopVideo(const char* args);
  CommandResult cmdRecordSendVideo(const char* args);
  CommandResult cmdReset(const char* args);
  CommandResult cmdWifiTest(const char* args);
  CommandResult cmdMqttTest(const char* args);
  CommandResult cmdMinioTest(const char* args);
  CommandResult cmdSendMessage(const char* args);
  CommandResult cmdCurlTest(const char* args);
  CommandResult cmdSystemInfo(const char* args);
  CommandResult cmdFileSystemTest(const char* args);
  CommandResult cmdClearWiFiCredentials(const char* args);
  CommandResult cmdFingerprintEnroll(const char* args);
  
  // Command definitions table
  static const Command COMMANDS[];
  static const int COMMAND_COUNT;
};

#endif // AMB82_DEBUG_MODULE_H 