#include "InternalHttpClient.h"
#include "AmebaFatFS.h"

// Flag to track if upload is in progress
static bool uploadInProgress = false;

InternalHttpClient::InternalHttpClient(const char* host, int port, const char* bucketName) {
  minioHost = String(host);
  minioPort = port;
  bucket = String(bucketName);
  connected = false;
  lastUploadedUrl = "";
  
  // Set WiFi client timeout to a reasonable value
  wifiClient.setRecvTimeout(30000); // 30 seconds timeout
}

bool InternalHttpClient::begin() {
  Serial.println("InternalHttpClient: Initializing...");
  
  // Check if WiFi is already connected
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("InternalHttpClient: ERROR - WiFi not connected");
    connected = false;
    return false;
  }
  
  Serial.println("InternalHttpClient: WiFi connection confirmed");
  Serial.print("InternalHttpClient: IP address: ");
  Serial.println(WiFi.localIP());
  
  // Set WiFi client timeout to a reasonable value
  wifiClient.setRecvTimeout(30000); // 30 seconds timeout
  
  connected = true;
  return true;
}

bool InternalHttpClient::uploadFile(const String& filePath, AmebaFatFS& fs) {
  // Check for duplicate uploads using static flag
  if (uploadInProgress) {
    Serial.println("Error: Another upload is already in progress");
    return false;
  }
  
  // Set flag to indicate upload is starting
  uploadInProgress = true;
  
  if (!connected) {
    Serial.println("Error: WiFi not connected");
    uploadInProgress = false;
    return false;
  }
  
  // Extract the filename from the path
  int lastSlash = filePath.lastIndexOf('/');
  String filename = (lastSlash >= 0) ? filePath.substring(lastSlash + 1) : filePath;
  
  Serial.print("Uploading file to MinIO: ");
  Serial.println(filename);
  
  bool success = false;
  
  // Construct the upload URL path
  String path = "/" + bucket + filename; 
  Serial.print("Upload URL: http://");
  Serial.print(minioHost);
  Serial.print(":");
  Serial.print(minioPort);
  Serial.println(path);
  
  // Check if file exists and get its size using the passed FS reference
  // AmebaFatFS fs; // REMOVE local instance
  // if (!fs.begin()) {
  //  Serial.println("Failed to mount file system");
  //  uploadInProgress = false;
  //  return false;
  // }
  
  // Check status and begin if not mounted (safer)
  if (fs.status() == 0) {
    Serial.println("Filesystem not mounted, attempting to mount...");
    if (!fs.begin()) {
        Serial.println("Failed to mount file system");
        uploadInProgress = false;
        return false;
    }
  }

  // Make sure file path is correctly formatted for AmebaFatFS
  String adjustedPath = filePath;
  
  Serial.print("Adjusted file path: ");
  Serial.println(adjustedPath);
  
  if (!fs.exists(adjustedPath)) {
    Serial.println("File does not exist: " + adjustedPath);
    //   uploadInProgress = false;
    //   return false;
  }
  
  File file = fs.open(adjustedPath);
  if (!file) {
    Serial.println("Failed to open file: " + adjustedPath);
    //   uploadInProgress = false;
    //   return false;
  }
  
  // Get file size for Content-Length header
  size_t fileSize = file.size();
  Serial.print("File size: ");
  Serial.println(fileSize);
  
  // Connect to server
  Serial.println("Connecting to MinIO server...");
  if (wifiClient.connect(minioHost.c_str(), minioPort)) {
    Serial.println("Connected to server, sending HTTP headers");
    
    // --- Print and send HTTP PUT request line ---
    String requestLine = String("PUT ") + path + " HTTP/1.1";
    wifiClient.println(requestLine);
    Serial.println(requestLine);
    
    // Lambda to send header and mirror to Serial for debugging
    auto sendHeader = [&](const String& header) {
      wifiClient.println(header);
      Serial.println(header);
    };
    
    // --- Send headers ---
    sendHeader(String("Host: ") + minioHost + ":" + String(minioPort));
    sendHeader("Content-Type: application/octet-stream");
    sendHeader(String("Content-Length: ") + String(fileSize));
    sendHeader("Expect: 100-continue"); // Request early approval to send body
    sendHeader("Connection: close");
    wifiClient.println(); // End of headers
    Serial.println(); // Mirror blank line delimiters

    // --- Wait briefly for a 100-continue or any early server response ---
    unsigned long headerWaitStart = millis();
    while (!wifiClient.available() && (millis() - headerWaitStart < 3000)) {
      delay(10);
    }
    if (wifiClient.available()) {
      String earlyResp = wifiClient.readStringUntil('\n');
      Serial.print("[Handshake] ");
      Serial.println(earlyResp);
      // If server sends 100 Continue we proceed, otherwise we keep the line for later processing
      
      // Abort early if the server immediately returned an error (e.g., 4xx or 5xx)
      if (earlyResp.startsWith("HTTP/1.1 4") || earlyResp.startsWith("HTTP/1.1 5")) {
        Serial.println("Server rejected request during handshake. Aborting upload.");
        wifiClient.stop();
        file.close();
        uploadInProgress = false;
        return false;
      }
    }
    
    // Helper to dump any available server data (max 512 bytes) for debugging
    auto dumpServerResponse = [&](const char* tag) {
      int avail = wifiClient.available();
      if (avail > 0) {
        Serial.print("[Debug] Server responded during ");
        Serial.println(tag);
        char tmpBuf[513];
        int toRead = (avail > 512) ? 512 : avail;
        int actuallyRead = wifiClient.read(reinterpret_cast<uint8_t*>(tmpBuf), toRead);
        if (actuallyRead > 0) {
          tmpBuf[actuallyRead] = '\0';
          Serial.print(tmpBuf);
        }
        Serial.println();
      }
    };
    
    // --- Detect early server response after headers ---
    dumpServerResponse("header phase");
    
    // Send the file content in chunks
    const size_t bufferSize = 1024; // Larger buffer for performance
    uint8_t buffer[bufferSize];
    size_t bytesRead = 0;
    size_t totalBytesSent = 0;
    unsigned long uploadStartTime = millis();
    
    Serial.println("Starting file upload...");
    
    // Allow reasonable time between retries
    bool uploadFailed = false;
    
    while (file.available() && !uploadFailed) {
      bytesRead = file.read(buffer, bufferSize);
      if (bytesRead > 0) {
        // Try sending data with minimal retries
        size_t bytesSent = 0;
        int retries = 0;
        const int MAX_RETRIES = 3;
        
        while (bytesSent < bytesRead && retries < MAX_RETRIES) {
          // Check if still connected before attempting to write
          if (!wifiClient.connected()) {
            Serial.println("Error: WiFiClient disconnected before write operation");
            dumpServerResponse("premature disconnect");
            uploadFailed = true;
            break;
          }
          
          // Write remaining data in this chunk
          size_t sent = wifiClient.write(buffer + bytesSent, bytesRead - bytesSent);
          
          if (sent > 0) {
            bytesSent += sent;
          } else {
            retries++;
            if (retries < MAX_RETRIES) {
              // Brief delay before retry
              delay(200);
            }
          }
        }
        
        // Check if all bytes were sent for this chunk
        if (bytesSent < bytesRead) {
          Serial.println("Failed to send complete chunk after retries");
          dumpServerResponse("chunk send failure");
          uploadFailed = true;
          break;
        }
        
        totalBytesSent += bytesSent;
        
        // Log progress for large files (every 10%)
        if (fileSize > 10000 && (totalBytesSent % (fileSize / 10)) < bufferSize) {
          Serial.print("Upload progress: ");
          Serial.print((totalBytesSent * 100) / fileSize);
          Serial.println("%");
        }
        
        // Minimal delay to prevent buffer overflows but maintain speed
        delay(5);
      }
      
      // Check for total upload timeout (10 minutes for large videos)
      if ((millis() - uploadStartTime) > 600000) {
        Serial.println("Upload operation timed out after 10 minutes");
        dumpServerResponse("timeout");
        uploadFailed = true;
        break;
      }
    }
    
    if (!uploadFailed) {
      unsigned long uploadDuration = millis() - uploadStartTime;
      Serial.print("File data sent. Total bytes: ");
      Serial.print(totalBytesSent);
      Serial.print(" in ");
      Serial.print(uploadDuration);
      Serial.println("ms");
      
      // Wait for response
      Serial.println("Waiting for server response...");
      unsigned long timeout = millis();
      bool responseReceived = false;
      int statusCode = 0;
      
      while (millis() - timeout < 20000) { // 20 seconds for response
        if (wifiClient.available()) {
          // Read a complete line (up to CR/LF)
          String line = wifiClient.readStringUntil('\n');
          line.trim();
          Serial.println("Response: " + line);

          // Ignore leading blank lines before status line arrives
          if (line.length() == 0 && !responseReceived) {
            continue;
          }

          // Capture the status line once
          if (!responseReceived && line.startsWith("HTTP/")) {
            int firstSpace = line.indexOf(' ');
            int secondSpace = line.indexOf(' ', firstSpace + 1);
            if (firstSpace > 0 && secondSpace > firstSpace) {
              statusCode = line.substring(firstSpace + 1, secondSpace).toInt();
              Serial.print("Status code: ");
              Serial.println(statusCode);
              responseReceived = true;
            } else {
              Serial.println("Failed to parse status code from: " + line);
            }
            continue; // Go read next header line
          }

          // After status line received, an empty line marks end of headers
          if (responseReceived && line.length() == 0) {
            if (statusCode == 200 || statusCode == 201) {
              lastUploadedUrl = "http://" + minioHost + ":" + String(minioPort) + path;
              success = true;
              Serial.println("Upload successful!");
            } else {
              Serial.print("Upload failed with status code: ");
              Serial.println(statusCode);
            }
            break;
          }
        }

        // Small delay to avoid CPU hogging in the loop
        delay(10);
      }
      
      if (!responseReceived) {
        Serial.println("Error: No response received from server (timeout)");
      }
    }
    
    wifiClient.stop();
    Serial.println("Connection closed");
  } else {
    Serial.println("Error: Connection to server failed");
  }
  
  file.close();
  uploadInProgress = false; // Reset flag when upload is complete
  return success;
}

String InternalHttpClient::getUploadedFileUrl() {
  int bucketPos = lastUploadedUrl.indexOf("apollo-bucket/");
  if (bucketPos != -1) {
    // Return everything after "apollo-bucket/"
    return lastUploadedUrl.substring(bucketPos + 13);
  }
  return lastUploadedUrl; // Fallback
} 