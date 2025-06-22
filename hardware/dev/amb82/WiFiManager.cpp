#include "WiFiManager.h"
#include <WiFi.h>
#include "AmebaFatFS.h"

// Static constants
const char* WiFiManager::CREDENTIALS_FILE = "wifi_config.txt";
const char* WiFiManager::AP_SSID = "SmartDoorLock_Setup";
const char* WiFiManager::AP_PASSWORD = "configure";

WiFiManager::WiFiManager() : server(80) {
  apActive = false;
}

bool WiFiManager::begin() {
  debugPrint("WiFiManager: Initializing...");
  
  // Initialize filesystem for credential storage
  if (!fs.begin()) {
    debugPrint("WiFiManager: Failed to initialize filesystem");
    return false;
  }
  
  debugPrint("WiFiManager: Filesystem initialized");
  
  // Try to load and connect with stored credentials first
  if (loadCredentials()) {
    debugPrint("WiFiManager: Found stored credentials, attempting connection...");
    if (connectToWiFi()) {
      debugPrint("WiFiManager: Successfully connected using stored credentials");
      return true;
    }
    debugPrint("WiFiManager: Stored credentials failed, starting AP mode");
  } else {
    debugPrint("WiFiManager: No stored credentials found, starting AP mode");
  }
  
  // Start AP mode for configuration
  return startConfigPortal();
}

bool WiFiManager::startConfigPortal() {
  debugPrint("WiFiManager: Starting configuration portal...");
  
  // Enable concurrent mode (AP + STA) for AMB82
  WiFi.enableConcurrent();
  debugPrint("WiFiManager: Enabled concurrent mode (AP + STA)");
  
  // Start Access Point: AMB82 expects channel as char* not int
  if (!WiFi.apbegin((char*)"SmartDoorLock_Setup", (char*)"configure", (char*)"1", 0)) {
    debugPrint("WiFiManager: Failed to start AP");
    return false;
  }
  
  apActive = true;
  debugPrint("WiFiManager: AP started - SSID: SmartDoorLock_Setup, Password: configure");
  debugPrint("WiFiManager: IP address: 192.168.1.1");
  
  // Start HTTP server
  server.setNonBlockingMode();
  server.begin();
  debugPrint("WiFiManager: HTTP server started on port 80");
  
  return true;
}

void WiFiManager::handleClient() {
  if (!apActive) return;
  
  WiFiClient client = server.available();
  if (!client || !client.connected()) {
    return;
  }

  debugPrint("WiFiManager: Client connected");
  
  // Set timeout for reading
  client.setTimeout(3000);
  
  // Read the HTTP request
  String request = "";
  unsigned long startTime = millis();
  
  // Read until we get a complete request or timeout
  while (client.connected() && (millis() - startTime < 3000)) {
    if (client.available()) {
      char c = client.read();
      request += c;
      
      // For GET requests, stop after we get the first line
      if (request.indexOf("GET") == 0 && request.indexOf("\r\n") > 0) {
        // Read a bit more to get headers
        while (client.available() && (millis() - startTime < 500)) {
          char c2 = client.read();
          request += c2;
          if (request.endsWith("\r\n\r\n")) break;
        }
        break;
      }
      
      // For POST requests, read until we get complete headers and body
      if (request.indexOf("POST") == 0 && request.indexOf("\r\n\r\n") > 0) {
        // We have headers, now read body if Content-Length is present
        int contentLength = 0;
        int contentLengthIndex = request.indexOf("Content-Length:");
        if (contentLengthIndex >= 0) {
          int startPos = contentLengthIndex + 15;
          while (startPos < request.length() && (request.charAt(startPos) == ' ' || request.charAt(startPos) == '\t')) {
            startPos++;
          }
          int endPos = request.indexOf("\r\n", startPos);
          if (endPos > startPos) {
            String lengthStr = request.substring(startPos, endPos);
            lengthStr.trim();
            contentLength = lengthStr.toInt();
          }
        }
        
        // Read the POST body
        String postData = "";
        if (contentLength > 0) {
          // Check if body is already in the request
          int bodyStart = request.indexOf("\r\n\r\n");
          if (bodyStart >= 0) {
            bodyStart += 4;
            if (bodyStart < request.length()) {
              postData = request.substring(bodyStart);
            }
          }
          
          // Read remaining bytes if needed
          int remainingBytes = contentLength - postData.length();
          while (remainingBytes > 0 && client.connected() && (millis() - startTime < 5000)) {
            if (client.available()) {
              char c = client.read();
              postData += c;
              remainingBytes--;
            } else {
              delay(10);
            }
          }
          
          // Append the complete POST data to the request
          request = request.substring(0, request.indexOf("\r\n\r\n") + 4) + postData;
        }
        break;
      }
    }
    delay(1);
  }
  
  if (request.length() == 0) {
    debugPrint("WiFiManager: No request received");
    client.stop();
    return;
  }
  
  debugPrint("WiFiManager: Request: " + request.substring(0, min(100, (int)request.length())));
  
  // Handle POST request (form submission)
  if (request.indexOf("POST /") >= 0) {
    debugPrint("WiFiManager: Handling POST request");
    
    // Find the POST body (after \r\n\r\n)
    String postData = "";
    int bodyStart = request.indexOf("\r\n\r\n");
    if (bodyStart >= 0) {
      bodyStart += 4;
      postData = request.substring(bodyStart);
    }
    
    debugPrint("WiFiManager: POST data: " + postData);
    
    // Parse credentials
    String ssid = "";
    String password = "";
    
    int ssidStart = postData.indexOf("ssid=");
    if (ssidStart >= 0) {
      ssidStart += 5;
      int ssidEnd = postData.indexOf("&", ssidStart);
      if (ssidEnd == -1) ssidEnd = postData.length();
      ssid = postData.substring(ssidStart, ssidEnd);
      ssid = urlDecode(ssid);
      ssid.trim();
    }
    
    int passwordStart = postData.indexOf("password=");
    if (passwordStart >= 0) {
      passwordStart += 9;
      int passwordEnd = postData.indexOf("&", passwordStart);
      if (passwordEnd == -1) passwordEnd = postData.length();
      password = postData.substring(passwordStart, passwordEnd);
      password = urlDecode(password);
      password.trim();
    }
    
    debugPrint("WiFiManager: Parsed SSID: '" + ssid + "'");
    debugPrint("WiFiManager: Parsed Password: '" + password + "'");
    
    if (ssid.length() > 0) {
      // Save credentials immediately
      debugPrint("WiFiManager: Saving credentials and preparing to reset...");
      if (saveCredentials(ssid, password)) {
        debugPrint("WiFiManager: Credentials saved successfully");
        
        // Send success page
        sendSuccessPageWithHeaders(client);
        
        // Wait a moment for the page to be sent, then reset
        delay(2000);
        debugPrint("WiFiManager: Resetting board to apply new WiFi settings...");
        
        // Reset the board using AMB82 system reset
        NVIC_SystemReset();
        
      } else {
        debugPrint("WiFiManager: Failed to save credentials");
        sendErrorPageWithHeaders(client, "Failed to save WiFi credentials. Please try again.");
        return;
      }
    } else {
      sendErrorPageWithHeaders(client, "Please provide a valid WiFi network name.");
      return;
    }
  }
  
  // Handle GET request - send setup page
  sendSetupPageWithHeaders(client);
}

void WiFiManager::sendSetupPageWithHeaders(WiFiClient& client) {
  String html = generateSetupPageHTML();
  
  // Send proper HTTP headers with content length
  client.println("HTTP/1.1 200 OK");
  client.println("Content-Type: text/html; charset=UTF-8");
  client.println("Connection: close");
  client.println("Cache-Control: no-cache, no-store, must-revalidate");
  client.println("Pragma: no-cache");
  client.println("Expires: 0");
  client.println("Content-Length: " + String(html.length()));
  client.println(); // Empty line to end headers
  
  // Send HTML content
  client.print(html);
  client.flush(); // Ensure all data is sent
  
  debugPrint("WiFiManager: Setup page sent (size: " + String(html.length()) + " bytes)");
  
  // Close connection after a brief delay
  delay(100);
  client.stop();
}

void WiFiManager::sendSuccessPageWithHeaders(WiFiClient& client) {
  String html = generateSuccessPageHTML();
  
  // Send proper HTTP headers with content length
  client.println("HTTP/1.1 200 OK");
  client.println("Content-Type: text/html; charset=UTF-8");
  client.println("Connection: close");
  client.println("Cache-Control: no-cache, no-store, must-revalidate");
  client.println("Pragma: no-cache");
  client.println("Expires: 0");
  client.println("Content-Length: " + String(html.length()));
  client.println(); // Empty line to end headers
  
  // Send HTML content
  client.print(html);
  client.flush(); // Ensure all data is sent
  
  debugPrint("WiFiManager: Success page sent (size: " + String(html.length()) + " bytes)");
  
  // Close connection after ensuring data is sent
  delay(100);
  client.stop();

}

void WiFiManager::sendErrorPageWithHeaders(WiFiClient& client, const String& errorMessage) {
  String html = generateErrorPageHTML(errorMessage);
  
  // Send proper HTTP headers with content length
  client.println("HTTP/1.1 200 OK");
  client.println("Content-Type: text/html; charset=UTF-8");
  client.println("Connection: close");
  client.println("Cache-Control: no-cache, no-store, must-revalidate");
  client.println("Pragma: no-cache");
  client.println("Expires: 0");
  client.println("Content-Length: " + String(html.length()));
  client.println(); // Empty line to end headers
  
  // Send HTML content
  client.print(html);
  client.flush(); // Ensure all data is sent
  
  debugPrint("WiFiManager: Error page sent (size: " + String(html.length()) + " bytes)");
  
  // Close connection after ensuring data is sent
  delay(100);
  client.stop();
}

String WiFiManager::generateSetupPageHTML() {
  return R"(<!DOCTYPE html>
<html>
<head>
  <meta charset='UTF-8'>
  <meta name='viewport' content='width=device-width, initial-scale=1.0'>
  <title>Apollo Smart Lock Setup</title>
  <style>
    * { 
      box-sizing: border-box; 
      margin: 0; 
      padding: 0; 
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif; 
    }
    
    body { 
      background: linear-gradient(135deg, #0A0F2C 0%, #1a1f3a 40%, #1F2937 100%); 
      min-height: 100vh; 
      padding: 20px; 
      display: flex; 
      align-items: center; 
      justify-content: center; 
    }
    
    .container { 
      max-width: 400px; 
      width: 100%; 
    }
    
    .header { 
      text-align: center; 
      margin-bottom: 30px; 
    }
    
    .logo { 
      display: flex; 
      flex-direction: column; 
      align-items: center; 
      margin-bottom: 15px; 
    }
    
    .logo-icon { 
      width: 80px; 
      height: 80px; 
      border-radius: 20px; 
      background: linear-gradient(45deg, #4F46E5 0%, #6366F1 100%); 
      display: flex; 
      align-items: center; 
      justify-content: center; 
      font-size: 40px; 
      margin-bottom: 15px; 
      box-shadow: 0 10px 30px rgba(79, 70, 229, 0.2); 
      transform: perspective(500px) rotateX(15deg); 
    }
    
    .logo h1 { 
      color: #ECEDEE; 
      font-size: 32px; 
      font-weight: 300; 
      letter-spacing: 2px; 
    }
    
    .subtitle { 
      color: #9CA3AF; 
      font-size: 16px; 
      font-weight: 400; 
    }
    
    .card { 
      background: rgba(31, 41, 55, 0.8); 
      backdrop-filter: blur(20px); 
      border-radius: 24px; 
      padding: 30px; 
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3); 
      border: 1px solid rgba(156, 163, 175, 0.1); 
      margin-bottom: 20px; 
    }
    
    .form-group { 
      margin-bottom: 24px; 
    }
    
    .form-group label { 
      display: block; 
      margin-bottom: 8px; 
      font-weight: 600; 
      color: #ECEDEE; 
      font-size: 14px; 
      text-transform: uppercase; 
      letter-spacing: 0.5px; 
    }
    
    .form-group input { 
      width: 100%; 
      padding: 16px 20px; 
      border: 2px solid #374151; 
      border-radius: 16px; 
      font-size: 16px; 
      background: #0A0F2C; 
      color: #ECEDEE; 
      transition: all 0.3s ease; 
    }
    
    .form-group input::placeholder {
      color: #9CA3AF;
    }
    
    .form-group input:focus { 
      outline: none; 
      border-color: #4F46E5; 
      background: #1F2937; 
      box-shadow: 0 0 0 4px rgba(255, 199, 79, 0.2); 
      transform: translateY(-2px); 
    }
    
    .btn-connect { 
      width: 100%; 
      padding: 18px; 
      background: linear-gradient(45deg, #4F46E5 0%, #6366F1 100%); 
      color: #FFFFFF; 
      border: none; 
      border-radius: 16px; 
      font-size: 16px; 
      font-weight: 600; 
      cursor: pointer; 
      transition: all 0.3s ease; 
      text-transform: uppercase; 
      letter-spacing: 1px; 
    }
    
    .btn-connect:hover { 
      transform: translateY(-3px); 
      box-shadow: 0 10px 30px rgba(79, 70, 229, 0.3); 
    }
    
    .btn-connect:active { 
      transform: translateY(-1px); 
    }
    
    .footer { 
      text-align: center; 
      color: #9CA3AF; 
      font-size: 14px; 
      font-weight: 400; 
    }
    
    @media (max-width: 480px) { 
      .container { 
        padding: 10px; 
      } 
      .card { 
        padding: 24px 20px; 
      } 
      .logo-icon { 
        width: 70px; 
        height: 70px; 
        font-size: 35px; 
      } 
      .logo h1 { 
        font-size: 28px; 
      }
    }
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <div class="logo">
        <div class="logo-icon">🔐</div>
        <h1>apollo</h1>
      </div>
      <p class="subtitle">Smart Lock WiFi Setup</p>
    </div>
    
    <div class="card">
      <form method="POST" action="/">
        <div class="form-group">
          <label for="ssid">WiFi Network</label>
          <input type="text" id="ssid" name="ssid" required placeholder="Enter your WiFi network name">
        </div>
        <div class="form-group">
          <label for="password">Password</label>
          <input type="password" id="password" name="password" placeholder="Enter your WiFi password">
        </div>
        <button type="submit" class="btn-connect">Connect</button>
      </form>
    </div>
    
    <div class="footer">
      <p>Apollo Smart Lock v1.0</p>
    </div>
  </div>

  <script>
    document.addEventListener('DOMContentLoaded', function() {
      const inputs = document.querySelectorAll('input');
      inputs.forEach(input => {
        input.addEventListener('focus', function() {
          this.parentElement.style.transform = 'scale(1.02)';
        });
        
        input.addEventListener('blur', function() {
          this.parentElement.style.transform = 'scale(1)';
        });
      });
    });
  </script>
</body>
</html>)";
}

String WiFiManager::generateSuccessPageHTML() {
  return R"(<!DOCTYPE html>
<html>
<head>
  <meta charset='UTF-8'>
  <meta name='viewport' content='width=device-width, initial-scale=1.0'>
  <title>Apollo Smart Lock Setup - Success</title>
  <style>
    * { 
      box-sizing: border-box; 
      margin: 0; 
      padding: 0; 
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif; 
    }
    
    body { 
      background: linear-gradient(135deg, #0A0F2C 0%, #1a1f3a 40%, #1F2937 100%); 
      min-height: 100vh; 
      padding: 20px; 
      display: flex; 
      align-items: center; 
      justify-content: center; 
    }
    
    .container { 
      max-width: 400px; 
      width: 100%; 
    }
    
    .header { 
      text-align: center; 
      margin-bottom: 30px; 
    }
    
    .logo { 
      display: flex; 
      flex-direction: column; 
      align-items: center; 
      margin-bottom: 15px; 
    }
    
    .logo-icon { 
      width: 80px; 
      height: 80px; 
      border-radius: 20px; 
      background: linear-gradient(45deg, #10B981 0%, #34D399 100%); 
      display: flex; 
      align-items: center; 
      justify-content: center; 
      font-size: 40px; 
      margin-bottom: 15px; 
      box-shadow: 0 10px 30px rgba(16, 185, 129, 0.2); 
      transform: perspective(500px) rotateX(15deg); 
    }
    
    .logo h1 { 
      color: #ECEDEE; 
      font-size: 32px; 
      font-weight: 300; 
      letter-spacing: 2px; 
    }
    
    .subtitle { 
      color: #9CA3AF; 
      font-size: 16px; 
      font-weight: 400; 
    }
    
    .card { 
      background: rgba(31, 41, 55, 0.8); 
      backdrop-filter: blur(20px); 
      border-radius: 24px; 
      padding: 30px; 
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3); 
      border: 1px solid rgba(156, 163, 175, 0.1); 
      margin-bottom: 20px; 
      text-align: center; 
    }
    
    .success-content h2 { 
      color: #ECEDEE; 
      font-size: 24px; 
      font-weight: 600; 
      margin-bottom: 16px; 
    }
    
    .success-content p { 
      color: #9CA3AF; 
      font-size: 16px; 
      line-height: 1.5; 
      margin-bottom: 12px; 
    }
    
    .success-indicator { 
      margin-top: 30px; 
      position: relative; 
    }
    
    .pulse { 
      width: 60px; 
      height: 60px; 
      margin: 0 auto; 
      background: linear-gradient(45deg, #10B981 0%, #FACC15 100%); 
      border-radius: 50%; 
      animation: pulse 2s infinite; 
    }
    
    @keyframes pulse { 
      0% { 
        transform: scale(0.95); 
        box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7); 
      } 
      70% { 
        transform: scale(1); 
        box-shadow: 0 0 0 20px rgba(16, 185, 129, 0); 
      } 
      100% { 
        transform: scale(0.95); 
        box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); 
      } 
    }
    
    .footer { 
      text-align: center; 
      color: #9CA3AF; 
      font-size: 14px; 
      font-weight: 400; 
    }
    
    @media (max-width: 480px) { 
      .container { 
        padding: 10px; 
      } 
      .card { 
        padding: 24px 20px; 
      } 
      .logo-icon { 
        width: 70px; 
        height: 70px; 
        font-size: 35px; 
      } 
      .logo h1 { 
        font-size: 28px; 
      }
    }
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <div class="logo">
        <div class="logo-icon">✓</div>
        <h1>apollo</h1>
      </div>
      <p class="subtitle">Setup Complete!</p>
    </div>
    
    <div class="card">
      <div class="success-content">
        <h2>Connected Successfully</h2>
        <p>Your Apollo Smart Lock is now connecting to the WiFi network.</p>
        <p>The setup network will disconnect shortly.</p>
        <div class="success-indicator">
          <div class="pulse"></div>
        </div>
      </div>
    </div>
    
    <div class="footer">
      <p>You can now close this page</p>
    </div>
  </div>
</body>
</html>)";
}

String WiFiManager::generateErrorPageHTML(const String& errorMessage) {
  String html = R"(<!DOCTYPE html>
<html>
<head>
  <meta charset='UTF-8'>
  <meta name='viewport' content='width=device-width, initial-scale=1.0'>
  <title>Apollo Smart Lock Setup - Error</title>
  <style>
    * { 
      box-sizing: border-box; 
      margin: 0; 
      padding: 0; 
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', system-ui, sans-serif; 
    }
    
    body { 
      background: linear-gradient(135deg, #0A0F2C 0%, #1a1f3a 40%, #1F2937 100%); 
      min-height: 100vh; 
      padding: 20px; 
      display: flex; 
      align-items: center; 
      justify-content: center; 
    }
    
    .container { 
      max-width: 400px; 
      width: 100%; 
    }
    
    .header { 
      text-align: center; 
      margin-bottom: 30px; 
    }
    
    .logo { 
      display: flex; 
      flex-direction: column; 
      align-items: center; 
      margin-bottom: 15px; 
    }
    
    .logo-icon { 
      width: 80px; 
      height: 80px; 
      border-radius: 20px; 
      background: linear-gradient(45deg, #EF4444 0%, #F87171 100%); 
      display: flex; 
      align-items: center; 
      justify-content: center; 
      font-size: 40px; 
      margin-bottom: 15px; 
      box-shadow: 0 10px 30px rgba(239, 68, 68, 0.2); 
      transform: perspective(500px) rotateX(15deg); 
    }
    
    .logo h1 { 
      color: #ECEDEE; 
      font-size: 32px; 
      font-weight: 300; 
      letter-spacing: 2px; 
    }
    
    .subtitle { 
      color: #9CA3AF; 
      font-size: 16px; 
      font-weight: 400; 
    }
    
    .card { 
      background: rgba(31, 41, 55, 0.8); 
      backdrop-filter: blur(20px); 
      border-radius: 24px; 
      padding: 30px; 
      box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3); 
      border: 1px solid rgba(156, 163, 175, 0.1); 
      margin-bottom: 20px; 
      text-align: center; 
    }
    
    .error-content h2 { 
      color: #ECEDEE; 
      font-size: 24px; 
      font-weight: 600; 
      margin-bottom: 16px; 
    }
    
    .error-content p { 
      color: #9CA3AF; 
      font-size: 16px; 
      line-height: 1.5; 
      margin-bottom: 12px; 
    }
    
    .error-message { 
      background: rgba(239, 68, 68, 0.1); 
      border: 1px solid rgba(239, 68, 68, 0.3); 
      color: #FCA5A5; 
      padding: 16px; 
      border-radius: 12px; 
      margin: 20px 0; 
      font-size: 14px; 
    }
    
    .footer { 
      text-align: center; 
      color: #9CA3AF; 
      font-size: 14px; 
      font-weight: 400; 
    }
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      <div class="logo">
        <div class="logo-icon">⚠️</div>
        <h1>apollo</h1>
      </div>
      <p class="subtitle">Setup Error</p>
    </div>
    
    <div class="card">
      <div class="error-content">
        <h2>Connection Failed</h2>
        <div class="error-message">)" + errorMessage + R"(</div>
        <p>Please go back and try again.</p>
      </div>
    </div>
    
    <div class="footer">
      <p><a href="/" style="color: #4F46E5;">← Go Back</a></p>
    </div>
  </div>
</body>
</html>)";
  return html;
}

String WiFiManager::extractValue(const String& data, const String& key) {
  int startIndex = data.indexOf(key);
  if (startIndex == -1) return "";
  
  startIndex += key.length();
  int endIndex = data.indexOf('&', startIndex);
  if (endIndex == -1) endIndex = data.length();
  
  String value = data.substring(startIndex, endIndex);
  value.replace("+", " ");
  return urlDecode(value);
}

String WiFiManager::urlDecode(const String& str) {
  String decoded = "";
  char c;
  char code0;
  char code1;
  for (unsigned int i = 0; i < str.length(); i++) {
    c = str.charAt(i);
    if (c == '+') {
      decoded += ' ';
    } else if (c == '%') {
      i++;
      code0 = str.charAt(i);
      i++;
      code1 = str.charAt(i);
      c = (h2int(code0) << 4) | h2int(code1);
      decoded += c;
    } else {
      decoded += c;
    }
  }
  return decoded;
}

unsigned char WiFiManager::h2int(char c) {
  if (c >= '0' && c <= '9') {
    return((unsigned char)c - '0');
  }
  if (c >= 'a' && c <= 'f') {
    return((unsigned char)c - 'a' + 10);
  }
  if (c >= 'A' && c <= 'F') {
    return((unsigned char)c - 'A' + 10);
  }
  return(0);
}

bool WiFiManager::connectToWiFi() {
  // Try fallback credentials if stored credentials fail
  debugPrint("WiFiManager: Attempting fallback credentials...");
  return connectToWiFi("FRAME", "HY04IOABBA8GI");
}

bool WiFiManager::connectToWiFi(const char* ssid, const char* password) {
  debugPrint("WiFiManager: Attempting to connect to WiFi...");
  
  WiFi.begin((char*)ssid, (char*)password);
  
  // Wait for connection with timeout
  unsigned long startTime = millis();
  while (WiFi.status() != WL_CONNECTED && millis() - startTime < 10000) {
    delay(500);
  }
  
  if (WiFi.status() == WL_CONNECTED) {
    debugPrint("WiFiManager: WiFi connected successfully");
    // Use individual octets instead of toString() for AMB82 compatibility
    IPAddress ip = WiFi.localIP();
    char ipStr[16];
    snprintf(ipStr, sizeof(ipStr), "%d.%d.%d.%d", ip[0], ip[1], ip[2], ip[3]);
    debugPrint("WiFiManager: IP address: " + String(ipStr));
    
    // DO NOT stop AP here - let the scheduled shutdown handle it after success page is sent
    // The AP will be stopped in update() method after the success page display delay
    
    return true;
  } else {
    debugPrint("WiFiManager: WiFi connection failed");
    return false;
  }
}

bool WiFiManager::saveCredentials(const String& ssid, const String& password) {
  debugPrint("WiFiManager: Saving WiFi credentials...");
  
  // For AMB82, we need to delete the file first for write mode
  if (fs.exists("/wifi_config.txt")) {
    fs.remove("/wifi_config.txt");
  }
  
  File file = fs.open("/wifi_config.txt");
  if (!file) {
    debugPrint("WiFiManager: Failed to open file for writing");
    return false;
  }
  
  file.println("ssid=" + ssid);
  file.println("password=" + password);
  file.close();
  
  debugPrint("WiFiManager: Credentials saved successfully");
  return true;
}

bool WiFiManager::loadCredentials() {
  debugPrint("WiFiManager: Loading stored credentials...");
  
  if (!fs.exists("/wifi_config.txt")) {
    debugPrint("WiFiManager: No stored credentials found");
    return false;
  }
  
  File file = fs.open("/wifi_config.txt");
  if (!file) {
    debugPrint("WiFiManager: Failed to open credentials file");
    return false;
  }
  
  String ssid = "";
  String password = "";
  
  while (file.available()) {
    String line = file.readStringUntil('\n');
    line.trim();
    
    if (line.startsWith("ssid=")) {
      ssid = line.substring(5);
    } else if (line.startsWith("password=")) {
      password = line.substring(9);
    }
  }
  
  file.close();
  
  if (ssid.length() > 0) {
    debugPrint("WiFiManager: Found stored credentials for: " + ssid);
    return connectToWiFi(ssid.c_str(), password.c_str());
  }
  
  return false;
}

void WiFiManager::clearCredentials() {
  debugPrint("WiFiManager: Clearing stored credentials...");
  
  if (fs.exists("/wifi_config.txt")) {
    fs.remove("/wifi_config.txt");
    debugPrint("WiFiManager: Credentials cleared successfully");
  } else {
    debugPrint("WiFiManager: No credentials file to clear");
  }
}

void WiFiManager::update() {
  if (apActive) {
    // Check for clients more frequently for better responsiveness
    static unsigned long lastClientCheck = 0;
    if (millis() - lastClientCheck > 50) {
      handleClient();
      lastClientCheck = millis();
    }
  }
}

bool WiFiManager::isConfigPortalActive() {
  return apActive;
}

bool WiFiManager::isConnected() {
  return WiFi.status() == WL_CONNECTED;
}

void WiFiManager::debugPrint(const String& message) {
  Serial.println(message);
}

// Compatibility methods for old API
bool WiFiManager::hasStoredCredentials() {
  return fs.exists("/wifi_config.txt");
}

bool WiFiManager::startConfigurationMode() {
  return startConfigPortal();
}

void WiFiManager::handleConfigurationRequests() {
  update();
}

bool WiFiManager::isConfigurationComplete() {
  return !apActive && isConnected();
}

bool WiFiManager::storeCredentials(const char* ssid, const char* password) {
  return saveCredentials(String(ssid), String(password));
}

// DEPRECATED
void WiFiManager::sendSetupPage(WiFiClient& client) {
  sendSetupPageWithHeaders(client);
}

void WiFiManager::sendSuccessPage(WiFiClient& client) {
  sendSuccessPageWithHeaders(client);
}

void WiFiManager::sendErrorPage(WiFiClient& client, const String& errorMessage) {
  sendErrorPageWithHeaders(client, errorMessage);
}

void WiFiManager::sendHTTPHeader(WiFiClient& client) {
  client.println("HTTP/1.1 200 OK");
  client.println("Content-Type: text/html; charset=UTF-8");
  client.println("Connection: close");
  client.println();
} 