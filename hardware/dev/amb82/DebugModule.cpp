#include "DebugModule.h"
#include <stdarg.h>
#include <string.h>
#include <stdio.h>

// Helper to parse command arguments
bool DebugModule::parseArgs(const char* args, char* buffer, int bufferSize, int argc, ...) {
  if (!args || !buffer || bufferSize <= 0) {
    return false;
  }
  
  // Copy args to our working buffer to avoid modifying the original
  strncpy(buffer, args, bufferSize - 1);
  buffer[bufferSize - 1] = '\0';
  
  // Setup va_list for pointers to return values
  va_list ap;
  va_start(ap, argc);
  
  char* token = strtok(buffer, " ");
  int count = 0;
  
  // Process each token and store in provided variables
  while (token != NULL && count < argc) {
    char** arg = va_arg(ap, char**);
    *arg = token;
    token = strtok(NULL, " ");
    count++;
  }
  
  va_end(ap);
  
  // Return true if we parsed exactly the number of arguments requested
  return (count == argc);
}

// Common command: Help
DebugModule::CommandResult DebugModule::cmdHelp(const char* args) {
  printHelp();
  return CMD_SUCCESS;
}

// Common command: Version info
DebugModule::CommandResult DebugModule::cmdVersion(const char* args) {
  respond("Debug Module v1.0");
  respond("Common framework for AMB82 and STM32 debugging");
  return CMD_SUCCESS;
}

// Common command: Status
DebugModule::CommandResult DebugModule::cmdStatus(const char* args) {
  char buffer[128];
  
  snprintf(buffer, sizeof(buffer), "Debug mode: %s", enabled ? "ENABLED" : "DISABLED");
  respond(buffer);
  
  snprintf(buffer, sizeof(buffer), "System status: %s", isReady() ? "READY" : "NOT READY");
  respond(buffer);
  
  return CMD_SUCCESS;
} 