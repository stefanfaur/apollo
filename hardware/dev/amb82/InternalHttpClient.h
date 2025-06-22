#ifndef HTTP_CLIENT_H
#define HTTP_CLIENT_H

#include <Arduino.h>
#include <WiFi.h>
#include "AmebaFatFS.h" // Include AmebaFatFS

class InternalHttpClient {
public:
  InternalHttpClient(const char* minioHost, int minioPort, const char* bucket);
  
  // Initialize the HTTP client (assumes WiFi is already connected)
  bool begin();
  
  // Upload a file to MinIO bucket
  bool uploadFile(const String& filePath, AmebaFatFS& fs); // Pass FS by reference
  
  // Get the URL of the last uploaded file
  String getUploadedFileUrl();
  
private:
  String minioHost;
  int minioPort;
  String bucket;
  String lastUploadedUrl;
  bool connected;
  WiFiClient wifiClient; // Persistent WiFiClient instance
};

#endif // HTTP_CLIENT_H 