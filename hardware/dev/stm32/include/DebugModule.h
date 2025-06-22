#ifndef DEBUG_MODULE_H
#define DEBUG_MODULE_H

// Common debug module interface for both AMB82 and STM32 boards
class DebugModule {
public:
  // Initialize the debug module
  virtual bool begin() = 0;
  
  // Process debug commands from serial
  virtual void update() = 0;
  
  // Respond with a message to the debug console
  virtual void respond(const char* message) = 0;
  
  // Command processor types
  enum CommandResult {
    CMD_SUCCESS,
    CMD_UNKNOWN,
    CMD_ERROR,
    CMD_INVALID_ARGS
  };
  
  // Enable/disable debug mode
  virtual void setEnabled(bool enabled) = 0;
  
  // Check if debug mode is enabled
  virtual bool isEnabled() = 0;
  
  // Log message to debug console
  virtual void log(const char* message) = 0;
  
  // Check if debug module is ready
  virtual bool isReady() = 0;
  
  // Print help information
  virtual void printHelp() = 0;

protected:
  // Command handler function signature
  using CommandHandler = CommandResult (DebugModule::*)(const char* args);
  
  // Command structure
  struct Command {
    const char* command;
    const char* description;
    CommandHandler handler;
  };
  
  // Helper to parse command line
  bool parseArgs(const char* args, char* buffer, int bufferSize, int argc, ...);
  
  // Common commands for both platforms
  CommandResult cmdHelp(const char* args);
  CommandResult cmdVersion(const char* args);
  CommandResult cmdStatus(const char* args);
  
  bool enabled = false;
};

#endif // DEBUG_MODULE_H 