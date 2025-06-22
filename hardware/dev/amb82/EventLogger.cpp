#include "EventLogger.h"
#include <string.h>

EventLogger::EventLogger() {
  eventCount = 0;
  nextIndex = 0;
  serialMonitoringEnabled = false;
  initialized = false;
  
  // Zero out the events array
  memset(events, 0, sizeof(events));
}

void EventLogger::begin(bool enableSerialMonitoring) {
  // This method must be called after Serial is initialized
  serialMonitoringEnabled = enableSerialMonitoring;
  initialized = true;
  
  // Only print to Serial if it's likely to be ready
  if (Serial) {
    Serial.println("Event logger initialized");
  }
}

void EventLogger::logEvent(int value, const char* description) {
  if (!initialized) {
    return; // Skip if not initialized properly
  }
  
  // Store the event in the next available slot
  SensorEvent& event = events[nextIndex];
  event.timestamp = millis();
  event.value = value;
  
  // Safely copy the description with length limit
  strncpy(event.description, description, sizeof(event.description) - 1);
  event.description[sizeof(event.description) - 1] = '\0'; // Ensure null termination
  
  // Update indices
  nextIndex = (nextIndex + 1) % MAX_EVENTS;
  if (eventCount < MAX_EVENTS) {
    eventCount++;
  }
  
  // Only print if Serial is likely ready
  if (Serial) {
    Serial.print("Event logged: ");
    Serial.print(description);
    Serial.print(" (value: ");
    Serial.print(value);
    Serial.print(", time: ");
    char timeBuffer[16];
    formatTimestamp(event.timestamp, timeBuffer, sizeof(timeBuffer));
    Serial.print(timeBuffer);
    Serial.println(")");
  }
}

void EventLogger::logSerialMessage(const char* message) {
  if (!initialized) {
    return; // Skip if not initialized properly
  }
  
  // Store the event in the next available slot
  SensorEvent& event = events[nextIndex];
  event.timestamp = millis();
  event.value = 0;  // No specific value for serial messages
  
  // Add prefix and safely copy the message with length limit
  strncpy(event.description, "Serial: ", sizeof(event.description) - 1);
  strncat(event.description, message, 
          sizeof(event.description) - strlen(event.description) - 1);
  event.description[sizeof(event.description) - 1] = '\0'; // Ensure null termination
  
  // Update indices
  nextIndex = (nextIndex + 1) % MAX_EVENTS;
  if (eventCount < MAX_EVENTS) {
    eventCount++;
  }
  
  // Only print if Serial is likely ready
  if (Serial) {
    Serial.print("Serial message logged: ");
    Serial.println(message);
  }
}

void EventLogger::clear() {
  eventCount = 0;
  nextIndex = 0;
  
  // Only print if Serial is likely ready
  if (Serial && initialized) {
    Serial.println("Event log cleared");
  }
}

String EventLogger::getEventSummary() {
  if (!initialized || eventCount == 0) {
    return "No events recorded";
  }
  
  String summary = String(eventCount);
  summary += " events recorded: ";
  
  // Calculate the index of the oldest event (only matters when buffer is full)
  int startIndex = (eventCount < MAX_EVENTS) ? 0 : nextIndex;
  
  for (int i = 0; i < eventCount; i++) {
    if (i > 0) {
      summary += "; ";
    }
    
    // Get the event index in chronological order
    int eventIndex = (startIndex + i) % MAX_EVENTS;
    SensorEvent& event = events[eventIndex];
    
    summary += event.description;
    
    // Only add value if it's not a serial message (which has value=0)
    if (event.value != 0) {
      summary += " (";
      summary += String(event.value);
      summary += ")";
    }
    
    // Add timestamp
    summary += " at ";
    char timeBuffer[16];
    formatTimestamp(event.timestamp, timeBuffer, sizeof(timeBuffer));
    summary += timeBuffer;
    
    // Limit summary length
    if (summary.length() > 200 && i < eventCount - 1) {
      // Use separate concatenation operations to avoid F() macro issues
      summary += "... and ";
      summary += String(eventCount - i - 1);
      summary += " more";
      break;
    }
  }
  
  return summary;
}

bool EventLogger::hasEvents() {
  return initialized && (eventCount > 0);
}

void EventLogger::update() {
  // DEPRECATED, now using processSerialMessage() instead so we don't hijack serial input
}

// New method to allow main application to pass messages from Serial2
void EventLogger::processSerialMessage(const char* message) {
  if (!initialized) {
    return; // Skip if not initialized properly
  }
  
  // Log the message that was processed by the main application
  logSerialMessage(message);
}

void EventLogger::formatTimestamp(unsigned long timestamp, char* buffer, size_t bufferSize) {
  // Format timestamp as mm:ss.SSS
  unsigned long seconds = timestamp / 1000;
  unsigned long minutes = seconds / 60;
  seconds = seconds % 60;
  unsigned long milliseconds = timestamp % 1000;
  
  // Safely format the timestamp
  snprintf(buffer, bufferSize, "%02lu:%02lu.%03lu", minutes, seconds, milliseconds);
} 