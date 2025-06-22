#ifndef STM32_DEBUG_MODULE_H
#define STM32_DEBUG_MODULE_H

#include "DebugModule.h"
#include <cstdint> 

class STM32DebugModule : public DebugModule {
public:
  STM32DebugModule();
  
  // Implementation of base class virtual methods
  bool begin() override;
  void update() override;
  void respond(const char* message) override;
  void setEnabled(bool enabled) override;
  bool isEnabled() override;
  void log(const char* message) override;
  bool isReady() override;
  void printHelp() override;
  
  // Method for main code to notify debug module about received messages
  void notifyMessageReceived(uint8_t command, uint8_t length, uint8_t* payload);

private:
  // Custom command handler type for this class
  using STM32CommandHandler = CommandResult (STM32DebugModule::*)(const char* args);
  
  // Custom command structure
  struct Command {
    const char* command;
    const char* description;
    STM32CommandHandler handler;
  };
  
  // Command buffer
  static const int MAX_COMMAND_LENGTH = 128;
  char commandBuffer[MAX_COMMAND_LENGTH];
  int commandLength;
  
  // Message buffer
  static const int MAX_MESSAGE_SIZE = 64;
  uint8_t messageBuffer[MAX_MESSAGE_SIZE];
  
  // Serial command parsing
  void processCommand(const char* command);
  CommandResult processCommandInternal(const char* command);
  
  // Common command handlers - implementing them locally since we have a custom Command struct
  CommandResult cmdHelp(const char* args);
  CommandResult cmdVersion(const char* args);
  CommandResult cmdStatus(const char* args);
  
  // STM32-specific command handlers
  CommandResult cmdSendMessage(const char* args);
  CommandResult cmdSensorTest(const char* args);
  CommandResult cmdTriggerEvent(const char* args);
  CommandResult cmdLockUnlock(const char* args);
  CommandResult cmdFakeSensor(const char* args);
  CommandResult cmdSystemInfo(const char* args);
  CommandResult cmdGPIOTest(const char* args);
  
  // Command definitions table
  static const Command COMMANDS[];
  static const int COMMAND_COUNT;
};

#endif // STM32_DEBUG_MODULE_H 