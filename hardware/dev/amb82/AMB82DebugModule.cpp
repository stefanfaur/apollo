#include "AMB82DebugModule.h"
#include "MessageProtocol.h"
#include <WiFi.h>
#include <HTTPClient.h>
#include <SD.h>
#include <stdio.h>
#include "AmebaFatFS.h"
#include "WiFiManager.h"

// Define the commands table
const AMB82DebugModule::Command AMB82DebugModule::COMMANDS[] = {
  {"help", "Show available commands", &AMB82DebugModule::cmdHelp},
  {"version", "Show debug module version", &AMB82DebugModule::cmdVersion},
  {"status", "Show system status", &AMB82DebugModule::cmdStatus},
  {"record", "Start video recording (usage: record [duration_ms])", &AMB82DebugModule::cmdRecordVideo},
  {"record-send", "Record video and upload to MinIO (usage: record-send [duration_ms])", &AMB82DebugModule::cmdRecordSendVideo},
  {"stop", "Stop video recording", &AMB82DebugModule::cmdStopVideo},
  {"wifi", "Test WiFi connection (usage: wifi [ssid] [password])", &AMB82DebugModule::cmdWifiTest},
  {"mqtt", "Test MQTT connection (usage: mqtt [broker] [port])", &AMB82DebugModule::cmdMqttTest},
  {"mqttpub", "Publish test message to test/test topic", &AMB82DebugModule::cmdMqttPubTest},
  {"minio", "Test MinIO access (usage: minio [host] [port] [bucket])", &AMB82DebugModule::cmdMinioTest},
  {"msg", "Send a message to STM32 (usage: msg [cmd] [data])", &AMB82DebugModule::cmdSendMessage},
  {"curl", "Test HTTP request (usage: curl [url])", &AMB82DebugModule::cmdCurlTest},
  {"sysinfo", "Show system information", &AMB82DebugModule::cmdSystemInfo},
  {"fstest", "Test file system operations", &AMB82DebugModule::cmdFileSystemTest},
  {"reset", "Reset video subsystem", &AMB82DebugModule::cmdReset},
  {"clearwifi", "Clear stored WiFi credentials", &AMB82DebugModule::cmdClearWiFiCredentials},
  {"fpenroll", "Enroll fingerprint ID (usage: fpenroll [id])", &AMB82DebugModule::cmdFingerprintEnroll}
};

const int AMB82DebugModule::COMMAND_COUNT = sizeof(COMMANDS) / sizeof(COMMANDS[0]);

AMB82DebugModule::AMB82DebugModule(VideoHandler& videoHandler, InternalHttpClient& httpClient, MqttClient& mqttClient, WiFiManager& wifiManager)
  : _videoHandler(videoHandler), _httpClient(httpClient), _mqttClient(mqttClient), _wifiManager(wifiManager) {
  commandLength = 0;
  memset(commandBuffer, 0, MAX_COMMAND_LENGTH);
  
  // Initialize record-and-upload flags
  _recordAndUpload = false;
  _recordStartTime = 0;
  _recordDuration = 0;
  
  // Configure the debug client to be non-blocking with a reasonable timeout
  debugClient.setNonBlockingMode();
  debugClient.setRecvTimeout(30000); // 3 seconds timeout
}

bool AMB82DebugModule::begin() {
  respond("AMB82 Debug Module initialized");
  respond("Type 'help' for available commands");
  enabled = true;
  return true;
}

void AMB82DebugModule::update() {
  if (!enabled) {
    return;
  }
  
  // Process any pending serial data
  while (Serial.available() > 0) {
    char c = Serial.read();
    
    // Handle backspace/delete
    if (c == '\b' || c == 127) {
      if (commandLength > 0) {
        commandLength--;
        Serial.print("\b \b"); // Erase the character on the console
      }
      continue;
    }
    
    // Echo character back to console
    Serial.write(c);
    
    // Process on newline
    if (c == '\n' || c == '\r') {
      Serial.println();
      
      // Null terminate the command and process it
      if (commandLength > 0) {
        commandBuffer[commandLength] = '\0';
        processCommand(commandBuffer);
      }
      
      // Reset the buffer
      commandLength = 0;
      Serial.print("> "); // Print prompt
      continue;
    }
    
    // Add to buffer if there's space
    if (commandLength < MAX_COMMAND_LENGTH - 1) {
      commandBuffer[commandLength++] = c;
    }
  }
  
  // Check if we need to upload a recording that's completed
  if (_recordAndUpload) {
    static bool wasRecording = false;
    bool isRecording = _videoHandler.isRecording();
    
    // Detect when recording just finished (transition from recording to not recording)
    if (wasRecording && !isRecording) {
      String videoPath = _videoHandler.getVideoFilePath();
      String message = "Video recording complete. Uploading file: " + videoPath;
      respond(message.c_str());
      
      // Upload the file to MinIO - Pass the shared FS object
      if (_httpClient.uploadFile(videoPath, _videoHandler.fatfs)) {
        String mediaUrl = _httpClient.getUploadedFileUrl();
        String successMsg = "Upload successful. Media URL: " + mediaUrl;
        respond(successMsg.c_str());
      } else {
        respond("Failed to upload video file");
      }
      
      // Reset flag
      _recordAndUpload = false;
    }
    
    wasRecording = isRecording;
  }
}

void AMB82DebugModule::respond(const char* message) {
  Serial.println(message);
}

void AMB82DebugModule::setEnabled(bool isEnabled) {
  enabled = isEnabled;
  if (enabled) {
    Serial.println("Debug mode enabled");
    Serial.print("> ");
  } else {
    Serial.println("Debug mode disabled");
  }
}

bool AMB82DebugModule::isEnabled() {
  return enabled;
}

void AMB82DebugModule::log(const char* message) {
  if (enabled) {
    Serial.print("[DEBUG] ");
    Serial.println(message);
  }
}

bool AMB82DebugModule::isReady() {
  return enabled;
}

void AMB82DebugModule::printHelp() {
  respond("Available commands:");
  for (int i = 0; i < COMMAND_COUNT; i++) {
    char buffer[128];
    snprintf(buffer, sizeof(buffer), "  %-10s - %s", COMMANDS[i].command, COMMANDS[i].description);
    respond(buffer);
  }
}

void AMB82DebugModule::processCommand(const char* command) {
  // Find the first space to separate command from args
  const char* space = strchr(command, ' ');
  char cmdName[32] = {0};
  
  if (space) {
    // Copy command name up to space
    strncpy(cmdName, command, space - command);
    cmdName[space - command] = '\0';
  } else {
    // No arguments, just copy the command
    strncpy(cmdName, command, sizeof(cmdName) - 1);
  }
  
  // Process the command
  DebugModule::CommandResult result = processCommandInternal(command);
  
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

AMB82DebugModule::CommandResult AMB82DebugModule::processCommandInternal(const char* command) {
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

// AMB82-specific command implementations

DebugModule::CommandResult AMB82DebugModule::cmdRecordVideo(const char* args) {
  // Default duration
  unsigned long duration = 10000; // 10 seconds
  
  // Parse duration if provided
  if (args && *args) {
    char argBuffer[32];
    char* durationArg = nullptr;
    if (parseArgs(args, argBuffer, sizeof(argBuffer), 1, &durationArg)) {
      duration = strtoul(durationArg, nullptr, 10);
    } else {
      return CMD_INVALID_ARGS;
    }
  }
  
  respond("Starting video recording (save to SD only)");
  char buffer[64];
  snprintf(buffer, sizeof(buffer), "Duration: %lu ms", duration);
  respond(buffer);
  
  // Pass false as second parameter to prevent auto-upload
  if (_videoHandler.startRecording(duration, false)) {
    return CMD_SUCCESS;
  } else {
    respond("Failed to start recording");
    return CMD_ERROR;
  }
}

DebugModule::CommandResult AMB82DebugModule::cmdRecordSendVideo(const char* args) {
  // Default duration
  unsigned long duration = 10000; // 10 seconds
  
  // Parse duration if provided
  if (args && *args) {
    char argBuffer[32];
    char* durationArg = nullptr;
    if (parseArgs(args, argBuffer, sizeof(argBuffer), 1, &durationArg)) {
      duration = strtoul(durationArg, nullptr, 10);
    } else {
      return CMD_INVALID_ARGS;
    }
  }
  
  respond("Starting video recording with auto-upload");
  char buffer[64];
  snprintf(buffer, sizeof(buffer), "Duration: %lu ms", duration);
  respond(buffer);
  
  // Use true for shouldUpload parameter to enable auto-upload
  if (_videoHandler.startRecording(duration, true)) {
    // Set up a timer to wait for recording to complete, then upload
    respond("Recording started. Will upload when complete.");
    
    // Start a separate thread or set a flag to monitor in update()
    _recordAndUpload = true;
    _recordStartTime = millis();
    _recordDuration = duration;
    
    return CMD_SUCCESS;
  } else {
    respond("Failed to start recording");
    return CMD_ERROR;
  }
}

DebugModule::CommandResult AMB82DebugModule::cmdStopVideo(const char* args) {
  if (!_videoHandler.isRecording()) {
    respond("No active recording to stop");
    return CMD_ERROR;
  }
  
  _videoHandler.stopRecording();
  respond("Video recording stopped");
  return CMD_SUCCESS;
}

// Add a system reset command to help with debugging
DebugModule::CommandResult AMB82DebugModule::cmdReset(const char* args) {
  respond("Performing video subsystem reset...");
  
  // First ensure recording is stopped if active
  if (_videoHandler.isRecording()) {
    respond("Stopping active recording first...");
    _videoHandler.stopRecording();
    delay(1000); // Give time for recording to stop completely
  }

  // Parse subsystem to reset if provided
  bool fullReset = false;
  
  if (args && *args) {
    if (strcmp(args, "full") == 0) {
      respond("Performing full reset (all subsystems)");
      fullReset = true;
    } else if (strcmp(args, "soft") == 0) {
      respond("Performing soft reset (components only, not camera)");
    }
  }
  
  if (fullReset) {
    // Perform a more intensive reset by fully ending and restarting components
    _videoHandler.end();
    delay(1000); // Give components more time to shut down
    
    if (_videoHandler.begin()) {
      respond("Full video system reset successful");
    } else {
      respond("ERROR: Full video system reset failed");
      return CMD_ERROR;
    }
  } else {
    // Just reset the stream connections which is less intrusive
    respond("Resetting video stream connections...");
    
    // Don't have direct access to resetStreamConnections, so simulate
    // the effect by stopping and starting a zero-length recording
    // This will reset the connections without actually recording
    if (_videoHandler.startRecording(100, false)) {
      delay(200);
      _videoHandler.stopRecording();
      respond("Video stream connections reset successful");
    } else {
      respond("WARNING: Could not reset video stream connections");
    }
  }
  
  return CMD_SUCCESS;
}

DebugModule::CommandResult AMB82DebugModule::cmdWifiTest(const char* args) {
  char argBuffer[128];
  char* ssid = nullptr;
  char* password = nullptr;
  
  if (args && *args) {
    // Parse custom credentials if provided
    if (parseArgs(args, argBuffer, sizeof(argBuffer), 2, &ssid, &password)) {
      respond("Testing WiFi with provided credentials");
    } else {
      return CMD_INVALID_ARGS;
    }
  } else {
    // Use default credentials
    respond("Testing WiFi with default credentials");
    ssid = (char*)"FRAME";
    password = (char*)"HY04IOABBA8GI";
  }
  
  char buffer[128];
  snprintf(buffer, sizeof(buffer), "Connecting to SSID: %s", ssid);
  respond(buffer);
  
  // Try to connect
  WiFi.begin(ssid, password);
  
  // Wait for connection with timeout
  int timeout = 20; // 10 seconds
  while (WiFi.status() != WL_CONNECTED && timeout > 0) {
    respond("Connecting...");
    delay(500);
    timeout--;
  }
  
  if (WiFi.status() == WL_CONNECTED) {
    respond("WiFi connected successfully!");
    char ipBuffer[16];
    IPAddress ip = WiFi.localIP();
    sprintf(ipBuffer, "%d.%d.%d.%d", ip[0], ip[1], ip[2], ip[3]);
    snprintf(buffer, sizeof(buffer), "IP address: %s", ipBuffer);
    respond(buffer);
    return CMD_SUCCESS;
  } else {
    respond("Failed to connect to WiFi");
    return CMD_ERROR;
  }
}

DebugModule::CommandResult AMB82DebugModule::cmdMqttTest(const char* args) {
  char argBuffer[128];
  char* broker = nullptr;
  char* portStr = nullptr;
  int port = 1883;
  
  if (args && *args) {
    // Parse custom broker/port if provided
    if (parseArgs(args, argBuffer, sizeof(argBuffer), 2, &broker, &portStr)) {
      port = atoi(portStr);
    } else {
      return CMD_INVALID_ARGS;
    }
  } else {
    broker = (char*)"test.mosquitto.org";
  }
  
  char buffer[128];
  snprintf(buffer, sizeof(buffer), "Testing MQTT connection to %s:%d", broker, port);
  respond(buffer);
  
  // Configure MQTT client first
  if (_mqttClient.begin("DEBUG_CLIENT")) {
    respond("MQTT client configuration successful");
    
    // Process MQTT client updates to establish connection (non-blocking)
    respond("Attempting to connect...");
    
    // First update to trigger connection attempt
    _mqttClient.update();
    
    // Non-blocking wait with maximum attempt time
    unsigned long startTime = millis();
    int connectionAttempts = 0;
    bool connected = false;
    
    respond("Waiting for connection (max 5 seconds)...");
    
    while ((millis() - startTime) < 5000 && connectionAttempts < 10 && !connected) {
      // Process updates to allow connection to establish
      _mqttClient.update();
      
      // Try subscribing to see if we're connected
      if (_mqttClient.subscribe("debug/test")) {
        connected = true;
        respond("Successfully connected and subscribed to test topic");
      } else {
        // Wait a bit before next attempt
        delay(500);
        connectionAttempts++;
      }
    }
    
    if (!connected) {
      respond("Failed to establish MQTT connection within timeout");
      return CMD_ERROR;
    }
    
    // Try to publish a test message
    respond("Attempting to publish test message...");
    if (_mqttClient.publishNotification("debug/test", "AMB82_DEBUG", 
                                      "Debug", "Debug Test", "MQTT connectivity test", "", "")) {
      respond("Successfully published test message");
      return CMD_SUCCESS;
    } else {
      respond("Failed to publish test message");
      return CMD_ERROR;
    }
  } else {
    respond("Failed to configure MQTT client");
    return CMD_ERROR;
  }
}

DebugModule::CommandResult AMB82DebugModule::cmdMqttPubTest(const char* args) {
  const char* topic = "test/test";
  if (!_mqttClient.isConnected()) {
    respond("MQTT client is not connected. Configure/connect first with 'mqtt' command");
    return CMD_ERROR;
  }

  // Publish a simple JSON debug message
  bool result = _mqttClient.publishNotification(topic, "AMB82_DEBUG", "DEBUG", "Debug", "MQTT publish test", "", "");
  if (result) {
    respond("Test message published to test/test topic");
    return CMD_SUCCESS;
  } else {
    respond("Failed to publish test message");
    return CMD_ERROR;
  }
}

DebugModule::CommandResult AMB82DebugModule::cmdMinioTest(const char* args) {
  char argBuffer[256];
  char* host = nullptr;
  char* portStr = nullptr;
  char* bucket = nullptr;
  
  // Parse args if provided
  if (args && *args) {
    if (parseArgs(args, argBuffer, sizeof(argBuffer), 3, &host, &portStr, &bucket)) {
      char buffer[128];
      snprintf(buffer, sizeof(buffer), "Testing MinIO connection to %s:%s, bucket: %s", 
               host, portStr, bucket);
      respond(buffer);
      
      // In  debug environment, simulate the file creation
      respond("Simulating test file creation...");
      
      // Try to upload using the internal HTTP client - pass shared fs
      if (_httpClient.uploadFile(String("/simulated_test_upload.txt"), _videoHandler.fatfs)) {
        String url = _httpClient.getUploadedFileUrl();
        snprintf(buffer, sizeof(buffer), "File upload simulated successfully. URL: %s", url.c_str());
        respond(buffer);
        return CMD_SUCCESS;
      } else {
        respond("Failed to simulate file upload to MinIO");
        return CMD_ERROR;
      }
    } else {
      return CMD_INVALID_ARGS;
    }
  } else {
    respond("Missing arguments. Usage: minio [host] [port] [bucket]");
    return CMD_INVALID_ARGS;
  }
}

DebugModule::CommandResult AMB82DebugModule::cmdSendMessage(const char* args) {
  char argBuffer[64];
  char* cmdStr = nullptr;
  char* dataStr = nullptr;
  
  if (args && *args) {
    if (parseArgs(args, argBuffer, sizeof(argBuffer), 2, &cmdStr, &dataStr)) {
      uint8_t cmd = (uint8_t)strtol(cmdStr, nullptr, 16);
      uint8_t data[8] = {0};
      int dataLen = 0;
      
      // Parse data bytes (assuming hex format)
      char* token = strtok(dataStr, ",");
      while (token && dataLen < 8) {
        data[dataLen++] = (uint8_t)strtol(token, nullptr, 16);
        token = strtok(nullptr, ",");
      }
      
      // Send the message
      sendMessage(Serial2, cmd, data, dataLen);
      
      respond("Message sent to STM32");
      return CMD_SUCCESS;
    } else {
      respond("Invalid arguments. Usage: msg [cmd_hex] [data_hex,data_hex,...]");
      return CMD_INVALID_ARGS;
    }
  } else {
    respond("Missing arguments. Usage: msg [cmd_hex] [data_hex,data_hex,...]");
    return CMD_INVALID_ARGS;
  }
}

DebugModule::CommandResult AMB82DebugModule::cmdCurlTest(const char* args) {
  char argBuffer[256];
  char* url = nullptr;
  
  if (args && *args) {
    if (parseArgs(args, argBuffer, sizeof(argBuffer), 1, &url)) {
      char buffer[128];
      snprintf(buffer, sizeof(buffer), "Testing HTTP request to: %s", url);
      respond(buffer);
      
      // Check WiFi status first
      if (WiFi.status() != WL_CONNECTED) {
        respond("Error: WiFi not connected. Connect first with 'wifi' command");
        return CMD_ERROR;
      }
      
      // Parse the URL to get server name and path
      char* serverName = url;
      char* path = "/";
      
      // Find the protocol prefix and skip it
      char* prefix = strstr(url, "://");
      if (prefix) {
        serverName = prefix + 3;  // Skip "://"
      }
      
      // Find the first slash to separate server and path
      char* firstSlash = strchr(serverName, '/');
      if (firstSlash) {
        *firstSlash = '\0';  // Null-terminate the server name
        path = firstSlash + 1;  // Path starts after the slash
      }
      
      // Use our non-blocking debugClient
      HttpClient http(debugClient);
      
      // Make GET request
      respond("Sending HTTP request...");
      int statusCode = http.get(serverName, path);
      
      if (statusCode == 0) {
        // Skip response headers
        http.skipResponseHeaders();
        
        // Get response status code
        int httpCode = http.responseStatusCode();
        snprintf(buffer, sizeof(buffer), "HTTP request successful. Response code: %d", httpCode);
        respond(buffer);
        
        // Read the response body with non-blocking approach
        respond("Response preview (first 100 chars):");
        String payload = "";
        int maxChars = 100;
        int totalRead = 0;
        unsigned long timeout = millis();
        
        // Read response body with timeout
        while (totalRead < maxChars && (millis() - timeout < 5000)) {
          if (http.available()) {
            char c = http.read();
            payload += c;
            totalRead++;
          } else {
            // Small delay to avoid CPU hogging
            delay(10);
          }
        }
        
        respond(payload.c_str());
        http.stop();
        return CMD_SUCCESS;
      } else {
        snprintf(buffer, sizeof(buffer), "HTTP request failed, error code: %d", statusCode);
        respond(buffer);
        http.stop();
        return CMD_ERROR;
      }
    } else {
      return CMD_INVALID_ARGS;
    }
  } else {
    respond("Missing URL. Usage: curl [url]");
    return CMD_INVALID_ARGS;
  }
}

DebugModule::CommandResult AMB82DebugModule::cmdSystemInfo(const char* args) {
  char buffer[128];
  
  respond("===== AMB82 System Information =====");
  
  // WiFi status
  respond("-- WiFi Status --");
  if (WiFi.status() == WL_CONNECTED) {
    snprintf(buffer, sizeof(buffer), "Connected to: %s", WiFi.SSID());
    respond(buffer);
    
    IPAddress ip = WiFi.localIP();
    sprintf(buffer, "IP address: %d.%d.%d.%d", ip[0], ip[1], ip[2], ip[3]);
    respond(buffer);
    
    snprintf(buffer, sizeof(buffer), "Signal strength: %ld dBm", WiFi.RSSI());
    respond(buffer);
  } else {
    respond("WiFi disconnected");
  }
  
  // Recording status
  respond("-- Video Recording Status --");
  if (_videoHandler.isRecording()) {
    respond("Recording: Active");
    snprintf(buffer, sizeof(buffer), "File: %s", _videoHandler.getVideoFilePath().c_str());
    respond(buffer);
  } else {
    respond("Recording: Inactive");
  }
  
  // File system info
  respond("-- File System Info --");
  if (_videoHandler.fatfs.status() == 0) {
      Serial.println("DebugModule: FS not mounted, mounting...");
      if (!_videoHandler.fatfs.begin()) {
          respond("Failed to initialize file system");
      } else {
          respond("Initialized file system (shared)");
      }
  } else {
      respond("File system already mounted (shared)");
  }
  
  return CMD_SUCCESS;
}

DebugModule::CommandResult AMB82DebugModule::cmdFileSystemTest(const char* args) {
  respond("===== File System Test =====");
  
  if (_videoHandler.fatfs.status() == 0) {
      Serial.println("DebugModule: FS not mounted, mounting...");
      if (!_videoHandler.fatfs.begin()) {
          respond("Failed to initialize file system");
          return CMD_ERROR;
      }
  } 
  
  char buffer[128];
  char path[128];
  char testFileName[] = "debug_test.txt";
  char testContent[] = "This is a test file created by AMB82 Debug Module";
  
  // Create and write to a test file
  respond("Creating test file...");
  sprintf(path, "%s%s", _videoHandler.fatfs.getRootPath(), testFileName);
  
  File file = _videoHandler.fatfs.open(path);
  if (!file) {
    respond("Failed to create test file");
    return CMD_ERROR;
  }
  
  file.println(testContent);
  file.close();
  respond("File created and written successfully");
  
  // Read back the file
  respond("Reading file back...");
  file = _videoHandler.fatfs.open(path);
  if (!file) {
    respond("Failed to open the test file for reading");
    return CMD_ERROR;
  }
  
  char readBuffer[128];
  memset(readBuffer, 0, sizeof(readBuffer));
  int bytesRead = file.read(readBuffer, sizeof(readBuffer));
  
  file.close();
  
  snprintf(buffer, sizeof(buffer), "Read %d bytes from file", bytesRead);
  respond(buffer);
  respond("==== File Content ====");
  respond(readBuffer);
  respond("==== End Content ====");
  
  // List files in root directory
  respond("Listing files in root directory:");
  respond("Root path: %s");
  respond(_videoHandler.fatfs.getRootPath());
  char dirBuffer[512];
  char *p;
  
  memset(dirBuffer, 0, sizeof(dirBuffer));
  _videoHandler.fatfs.readDir(_videoHandler.fatfs.getRootPath(), dirBuffer, sizeof(dirBuffer));
  
  p = dirBuffer;
  while (strlen(p) > 0) {
    respond(p);
    p += strlen(p) + 1;
  }
  
  return CMD_SUCCESS;
}

// Implementation of common command handlers
DebugModule::CommandResult AMB82DebugModule::cmdHelp(const char* args) {
  printHelp();
  return CMD_SUCCESS;
}

DebugModule::CommandResult AMB82DebugModule::cmdVersion(const char* args) {
  respond("AMB82 Debug Module v1.0");
  return CMD_SUCCESS;
}

DebugModule::CommandResult AMB82DebugModule::cmdStatus(const char* args) {
  respond("AMB82 System Status:");
  
  // Check WiFi status
  if (WiFi.status() == WL_CONNECTED) {
    char buffer[64];
    snprintf(buffer, sizeof(buffer), "WiFi: Connected to %s", WiFi.SSID());
    respond(buffer);
  } else {
    respond("WiFi: Disconnected");
  }
  
  // Check recording status
  if (_videoHandler.isRecording()) {
    respond("Recording: Active");
  } else {
    respond("Recording: Inactive");
  }
  
  return CMD_SUCCESS;
}

DebugModule::CommandResult AMB82DebugModule::cmdClearWiFiCredentials(const char* args) {
  respond("Clearing stored WiFi credentials...");
  
  // Check if credentials exist first
  if (!_wifiManager.hasStoredCredentials()) {
    respond("No stored WiFi credentials found");
    return CMD_SUCCESS;
  }
  
  // Clear credentials using the new API
  _wifiManager.clearCredentials();
  respond("WiFi credentials cleared successfully");
  respond("Device will start in configuration mode on next restart");
  return CMD_SUCCESS;
}

DebugModule::CommandResult AMB82DebugModule::cmdFingerprintEnroll(const char* args) {
  char argBuffer[32];
  char* idStr = nullptr;

  if (!(args && *args && parseArgs(args, argBuffer, sizeof(argBuffer), 1, &idStr))) {
    respond("Missing arguments. Usage: fpenroll [id]");
    return CMD_INVALID_ARGS;
  }

  int id = atoi(idStr);
  if (id <= 0 || id >= 128) {
    respond("Invalid ID. Must be 1-127");
    return CMD_INVALID_ARGS;
  }

  // Send enroll start to STM32
  uint8_t payload[1] = { (uint8_t)id };
  sendMessage(Serial2, CMD_ENROLL_START, payload, 1);

  char buf[80];
  snprintf(buf, sizeof(buf), "Enroll command sent for ID %d. Waiting for response...", id);
  respond(buf);

  unsigned long startTime = millis();
  const unsigned long timeoutMs = 30000; // 30 seconds

  while (millis() - startTime < timeoutMs) {
    if (Serial2.available() > 0) {
      Message msg;
      if (readMessage(Serial2, msg)) {
        switch (msg.command) {
          case CMD_PROMPT_USER: {
            if (msg.length == 1) {
              const char* promptStr = "Unknown";
              switch (msg.payload[0]) {
                case 0x01: promptStr = "Place finger on sensor"; break;
                case 0x02: promptStr = "Remove finger"; break;
                case 0x03: promptStr = "Place same finger again"; break;
              }
              respond(promptStr);
            }
            break; }

          case CMD_ENROLL_SUCCESS: {
            if (msg.length == 1) {
              snprintf(buf, sizeof(buf), "Enroll success! ID %d stored", msg.payload[0]);
              respond(buf);
            } else {
              respond("Enroll success received (no ID)");
            }
            return CMD_SUCCESS; }

          case CMD_ENROLL_FAILURE: {
            if (msg.length == 1) {
              uint8_t code = msg.payload[0];
              snprintf(buf, sizeof(buf), "Enroll failed. Error code 0x%02X", code);
              respond(buf);
            } else {
              respond("Enroll failure received (no code)");
            }
            return CMD_ERROR; }
        }
      }
    }
    delay(20); // prevent CPU hog
  }

  respond("Timeout waiting for enroll result");
  return CMD_ERROR;
} 