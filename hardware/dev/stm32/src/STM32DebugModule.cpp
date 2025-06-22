#include "STM32DebugModule.h"
#include "DebugModule.h"
#include "MessageProtocol.h"
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include "debug_serial.h"

// Define min function if not available
#ifndef min
#define min(a,b) ((a)<(b)?(a):(b))
#endif

// Define the commands table
const STM32DebugModule::Command STM32DebugModule::COMMANDS[] = {
  {"help", "Show available commands", &STM32DebugModule::cmdHelp},
  {"version", "Show debug module version", &STM32DebugModule::cmdVersion},
  {"status", "Show system status", &STM32DebugModule::cmdStatus},
  {"send", "Send message to AMB82 (usage: send [cmd] [data])", &STM32DebugModule::cmdSendMessage},
  {"sensor", "Test sensor readings (usage: sensor [id])", &STM32DebugModule::cmdSensorTest},
  {"event", "Trigger a sensor event (usage: event [type] [value])", &STM32DebugModule::cmdTriggerEvent},
  {"lock", "Lock/unlock door (usage: lock [0=unlock, 1=lock])", &STM32DebugModule::cmdLockUnlock},
  {"fake", "Generate fake sensor data (usage: fake [id] [value])", &STM32DebugModule::cmdFakeSensor},
  {"info", "Show system information", &STM32DebugModule::cmdSystemInfo},
  {"gpio", "Test GPIO pin (usage: gpio [pin] [state])", &STM32DebugModule::cmdGPIOTest}
};

const int STM32DebugModule::COMMAND_COUNT = sizeof(COMMANDS) / sizeof(COMMANDS[0]);

STM32DebugModule::STM32DebugModule() {
  commandLength = 0;
  memset(commandBuffer, 0, MAX_COMMAND_LENGTH);
}

bool STM32DebugModule::begin() {
  debug_println("STM32 Debug Module initialized");
  debug_println("Type 'help' for available commands");
  enabled = true;
  return true;
}

void STM32DebugModule::update() {
  if (!enabled) {
    return;
  }
  
  // Process any pending serial data
  while (get_debug_serial().available() > 0) {
    char c = get_debug_serial().read();
    
    // Handle backspace/delete
    if (c == '\b' || c == 127) {
      if (commandLength > 0) {
        commandLength--;
        get_debug_serial().print("\b \b"); // Erase the character on the console
      }
      continue;
    }
    
    // Echo character back to console
    get_debug_serial().write(c);
    
    // Process on newline
    if (c == '\n' || c == '\r') {
      get_debug_serial().println();
      
      // Null terminate the command and process it
      if (commandLength > 0) {
        commandBuffer[commandLength] = '\0';
        processCommand(commandBuffer);
      }
      
      // Reset the buffer
      commandLength = 0;
      get_debug_serial().print("> "); // Print prompt
      continue;
    }
    
    // Add to buffer if there's space
    if (commandLength < MAX_COMMAND_LENGTH - 1) {
      commandBuffer[commandLength++] = c;
    }
  }
  
}

void STM32DebugModule::respond(const char* message) {
  debug_println(message);
}

void STM32DebugModule::setEnabled(bool isEnabled) {
  enabled = isEnabled;
  if (enabled) {
    debug_println("Debug mode enabled");
    debug_print("> ");
  } else {
    debug_println("Debug mode disabled");
  }
}

bool STM32DebugModule::isEnabled() {
  return enabled;
}

void STM32DebugModule::log(const char* message) {
  if (enabled) {
    debug_print("[DEBUG] ");
    debug_println(message);
  }
}

bool STM32DebugModule::isReady() {
  return enabled;
}

void STM32DebugModule::printHelp() {
  respond("Available commands:");
  for (int i = 0; i < COMMAND_COUNT; i++) {
    char buffer[128];
    snprintf(buffer, sizeof(buffer), "  %-10s - %s", COMMANDS[i].command, COMMANDS[i].description);
    respond(buffer);
  }
}

void STM32DebugModule::processCommand(const char* command) {
  // Find the first space to separate command from args
  const char* space = strchr(command, ' ');
  char cmdName[32] = {0};
  const char* args = "";
  
  if (space) {
    // Copy command name up to space
    strncpy(cmdName, command, space - command);
    cmdName[space - command] = '\0';
    args = space + 1; // Skip the space
  } else {
    // No arguments, just copy the command
    strncpy(cmdName, command, sizeof(cmdName) - 1);
  }
  
  // Process the command
  CommandResult result = processCommandInternal(command);
  
  // Display result
  switch (result) {
    case CMD_SUCCESS:
      respond("Command completed successfully");
      break;
    case CMD_UNKNOWN:
      respond("Unknown command. Type 'help' for available commands");
      break;
    case CMD_ERROR:
      respond("Error executing command");
      break;
    case CMD_INVALID_ARGS:
      respond("Invalid arguments. Check command syntax");
      break;
  }
}

STM32DebugModule::CommandResult STM32DebugModule::processCommandInternal(const char* command) {
  // Find the command in the command table
  for (int i = 0; i < COMMAND_COUNT; i++) {
    // Check if command starts with the command name
    if (strncmp(command, COMMANDS[i].command, strlen(COMMANDS[i].command)) == 0) {
      // Check if it's an exact command or has args
      if (command[strlen(COMMANDS[i].command)] == '\0' || 
          command[strlen(COMMANDS[i].command)] == ' ') {
        // Extract arguments
        const char* args = command + strlen(COMMANDS[i].command);
        if (*args == ' ') args++; // Skip space
        
        // Call the handler
        return (this->*COMMANDS[i].handler)(args);
      }
    }
  }
  
  return CMD_UNKNOWN;
}

// Common command implementations
STM32DebugModule::CommandResult STM32DebugModule::cmdHelp(const char* args) {
  printHelp();
  return CMD_SUCCESS;
}

STM32DebugModule::CommandResult STM32DebugModule::cmdVersion(const char* args) {
  respond("Debug Module v1.0");
  respond("Common framework for AMB82 and STM32 debugging");
  return CMD_SUCCESS;
}

STM32DebugModule::CommandResult STM32DebugModule::cmdStatus(const char* args) {
  char buffer[128];
  
  snprintf(buffer, sizeof(buffer), "Debug mode: %s", enabled ? "ENABLED" : "DISABLED");
  respond(buffer);
  
  snprintf(buffer, sizeof(buffer), "System status: %s", isReady() ? "READY" : "NOT READY");
  respond(buffer);
  
  return CMD_SUCCESS;
}

// STM32-specific command implementations
DebugModule::CommandResult STM32DebugModule::cmdSendMessage(const char* args) {
  char argBuffer[64];
  char* cmdStr = nullptr;
  char* dataStr = nullptr;
  
  if (args && *args) {
    if (parseArgs(args, argBuffer, sizeof(argBuffer), 2, &cmdStr, &dataStr)) {
      uint8_t cmd = (uint8_t)strtol(cmdStr, nullptr, 16);
      uint8_t data[8] = {0};
      int dataLen = 0;
      
      // Parse data bytes (hex format)
      char* token = strtok(dataStr, ",");
      while (token && dataLen < 8) {
        data[dataLen++] = (uint8_t)strtol(token, nullptr, 16);
        token = strtok(nullptr, ",");
      }
      
      // Send the message
      sendMessage(Serial2, cmd, data, dataLen);
      
      respond("Message sent to AMB82");
      return CMD_SUCCESS;
    } else {
      respond("Invalid arguments. Usage: send [cmd_hex] [data_hex,data_hex,...]");
      return CMD_INVALID_ARGS;
    }
  } else {
    respond("Missing arguments. Usage: send [cmd_hex] [data_hex,data_hex,...]");
    return CMD_INVALID_ARGS;
  }
}

DebugModule::CommandResult STM32DebugModule::cmdSensorTest(const char* args) {
  char argBuffer[32];
  char* sensorId = nullptr;
  int sensorNum = 0;
  
  if (args && *args) {
    if (parseArgs(args, argBuffer, sizeof(argBuffer), 1, &sensorId)) {
      sensorNum = atoi(sensorId);
    } else {
      return CMD_INVALID_ARGS;
    }
  }
  
  // Sensor simulation for testing
  int sensorValue = 0;
  
  switch (sensorNum) {
    case 0: // Motion sensor
      sensorValue = random(0, 1000); // Random value
      break;
    case 1: // Temperature sensor
      sensorValue = 20 + random(0, 15); // 20-35°C
      break;
    case 2: // Light sensor
      sensorValue = random(0, 1023); // 0-1023 light level
      break;
    default:
      respond("Unknown sensor ID");
      return CMD_INVALID_ARGS;
  }
  
  char buffer[64];
  snprintf(buffer, sizeof(buffer), "Sensor %d reading: %d", sensorNum, sensorValue);
  respond(buffer);
  
  // Send sensor data to AMB82
  uint8_t payload[2] = {
    (uint8_t)(sensorValue >> 8),   // High byte
    (uint8_t)(sensorValue & 0xFF)  // Low byte
  };
  sendMessage(Serial2, CMD_SENSOR_DATA, payload, 2);
  
  return CMD_SUCCESS;
}

DebugModule::CommandResult STM32DebugModule::cmdTriggerEvent(const char* args) {
  char argBuffer[32];
  char* eventType = nullptr;
  char* valueStr = nullptr;
  
  if (args && *args) {
    if (parseArgs(args, argBuffer, sizeof(argBuffer), 2, &eventType, &valueStr)) {
      uint8_t type = (uint8_t)atoi(eventType);
      int value = atoi(valueStr);
      
      char buffer[64];
      snprintf(buffer, sizeof(buffer), "Triggering event type: %d with value: %d", type, value);
      respond(buffer);
      
      // Prepare event payload
      // Format: [event_type][value_high][value_low]
      uint8_t payload[3] = {
        type,
        (uint8_t)(value >> 8),   // High byte
        (uint8_t)(value & 0xFF)  // Low byte
      };
      
      // Send event to AMB82
      sendMessage(Serial2, CMD_SENSOR_EVENT, payload, 3);
      
      return CMD_SUCCESS;
    } else {
      respond("Invalid arguments. Usage: event [type] [value]");
      return CMD_INVALID_ARGS;
    }
  } else {
    respond("Missing arguments. Usage: event [type] [value]");
    return CMD_INVALID_ARGS;
  }
}

DebugModule::CommandResult STM32DebugModule::cmdLockUnlock(const char* args) {
  char argBuffer[16];
  char* stateStr = nullptr;
  
  if (args && *args) {
    if (parseArgs(args, argBuffer, sizeof(argBuffer), 1, &stateStr)) {
      int state = atoi(stateStr);
      
      if (state == 0) {
        respond("Unlocking door");
        // Simulate door unlock
        digitalWrite(LED_BUILTIN, HIGH); // Use LED to indicate unlock
      } else {
        respond("Locking door");
        // Simulate door lock
        digitalWrite(LED_BUILTIN, LOW); // Use LED to indicate lock
      }
      
      return CMD_SUCCESS;
    } else {
      return CMD_INVALID_ARGS;
    }
  } else {
    respond("Missing arguments. Usage: lock [0=unlock, 1=lock]");
    return CMD_INVALID_ARGS;
  }
}

DebugModule::CommandResult STM32DebugModule::cmdFakeSensor(const char* args) {
  char argBuffer[32];
  char* sensorId = nullptr;
  char* valueStr = nullptr;
  
  if (args && *args) {
    if (parseArgs(args, argBuffer, sizeof(argBuffer), 2, &sensorId, &valueStr)) {
      int sensorNum = atoi(sensorId);
      int value = atoi(valueStr);
      
      char buffer[64];
      snprintf(buffer, sizeof(buffer), "Sending fake sensor %d data with value: %d", sensorNum, value);
      respond(buffer);
      
      // Send custom sensor data to AMB82
      uint8_t payload[3] = {
        (uint8_t)sensorNum,
        (uint8_t)(value >> 8),   // High byte
        (uint8_t)(value & 0xFF)  // Low byte
      };
      sendMessage(Serial2, CMD_SENSOR_DATA, payload, 3);
      
      return CMD_SUCCESS;
    } else {
      return CMD_INVALID_ARGS;
    }
  } else {
    respond("Missing arguments. Usage: fake [sensor_id] [value]");
    return CMD_INVALID_ARGS;
  }
}

DebugModule::CommandResult STM32DebugModule::cmdSystemInfo(const char* args) {
  respond("===== STM32 System Information =====");
  
  // MCU information (hardcoded values)
  respond("-- MCU Information --");
  respond("Device: STM32F411CE");
  respond("Clock: 100 MHz");
  respond("Flash: 512 KB");
  respond("RAM: 128 KB");
  
  // GPIO status
  respond("-- GPIO Status --");
  respond("LED: On");
  respond("Door lock: Locked");
  respond("Motion sensor: Active");
  
  // Communication status
  respond("-- Communication Status --");
  respond("AMB82 link: Active");
  
  return CMD_SUCCESS;
}

DebugModule::CommandResult STM32DebugModule::cmdGPIOTest(const char* args) {
  char argBuffer[32];
  char* pinStr = nullptr;
  char* stateStr = nullptr;
  
  if (args && *args) {
    if (parseArgs(args, argBuffer, sizeof(argBuffer), 2, &pinStr, &stateStr)) {
      int pin = atoi(pinStr);
      int state = atoi(stateStr);
      
      char buffer[64];
      snprintf(buffer, sizeof(buffer), "Setting GPIO pin %d to %s", pin, state ? "HIGH" : "LOW");
      respond(buffer);
      
      pinMode(pin, OUTPUT);
      digitalWrite(pin, state ? HIGH : LOW);
      
      return CMD_SUCCESS;
    } else {
      return CMD_INVALID_ARGS;
    }
  } else {
    respond("Missing arguments. Usage: gpio [pin] [state]");
    return CMD_INVALID_ARGS;
  }
}

// New method to notify the debug module about a received message
void STM32DebugModule::notifyMessageReceived(uint8_t command, uint8_t length, uint8_t* payload) {
  if (!enabled) {
    return;
  }
  
  char logBuffer[128];
  snprintf(logBuffer, sizeof(logBuffer), "Message received from AMB82. Command: 0x%02X, Length: %d",
           command, length);
  log(logBuffer);
  
  if (length > 0) {
    get_debug_serial().print("[DEBUG] Data: ");
    for (int i = 0; i < min(length, 8); i++) {  // Show up to 8 bytes
      get_debug_serial().print(payload[i], HEX);
      get_debug_serial().print(" ");
    }
    if (length > 8) {
      get_debug_serial().print("...");
    }
    get_debug_serial().println();
  }
} 