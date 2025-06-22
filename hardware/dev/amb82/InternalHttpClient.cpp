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
    
    // Send HTTP PUT request
    wifiClient.print("PUT ");
    wifiClient.print(path);
    wifiClient.println(" HTTP/1.1");
    
    // Send headers
    wifiClient.print("Host: ");
    wifiClient.print(minioHost);
    wifiClient.print(":");
    wifiClient.println(minioPort);
    wifiClient.println("Content-Type: application/octet-stream");
    wifiClient.print("Content-Length: ");
    wifiClient.println(fileSize);
    wifiClient.println("Connection: close");
    wifiClient.println(); // End of headers
    
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
      String statusLine = "";
      int statusCode = 0;
      
      while (millis() - timeout < 20000) { // 20 seconds for response
        if (wifiClient.available()) {
          if (!responseReceived) {
            // Read status line
            statusLine = wifiClient.readStringUntil('\n');
            Serial.println("Response: " + statusLine);
            
            // Parse status code
            int statusCodeStartPos = statusLine.indexOf(' ') + 1;
            int statusCodeEndPos = statusLine.indexOf(' ', statusCodeStartPos);
            if (statusCodeStartPos > 0 && statusCodeEndPos > statusCodeStartPos) {
              statusCode = statusLine.substring(statusCodeStartPos, statusCodeEndPos).toInt();
              Serial.print("Status code: ");
              Serial.println(statusCode);
              responseReceived = true;
            } else {
              Serial.println("Failed to parse status code from: " + statusLine);
            }
          }
          
          // Read a line at a time
          String line = wifiClient.readStringUntil('\n');
          
          if (line == "\r" || line.length() == 0) {
            // End of headers, check status code
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