#ifndef EVENT_LOGGER_H
#define EVENT_LOGGER_H

#include <Arduino.h>

struct SensorEvent {
  unsigned long timestamp;
  int value;
  char description[32]; // Fixed-size character array instead of String
};

class EventLogger {
public:
  // Default constructor does minimal initialization
  EventLogger();
  
  // Initialize with optional serial monitoring
  void begin(bool enableSerialMonitoring = false);
  
  // Log a new sensor event
  void logEvent(int value, const char* description);
  
  // Log a message from serial
  void logSerialMessage(const char* message);
  
  // Clear all logged events
  void clear();
  
  // Get a summary of all logged events as a String
  String getEventSummary();
  
  // Check if any events were logged
  bool hasEvents();
  
  // DEPRECATED, use processSerialMessage() instead
  void update();
  
  // Process a serial message passed from the main application
  void processSerialMessage(const char* message);
  
private:
  static const int MAX_EVENTS = 3; // Maximum number of events to store, reduced from 5
  SensorEvent events[MAX_EVENTS]; // Fixed-size array
  int eventCount; // Current number of events stored
  int nextIndex; // Next position to write an event (for circular buffer)
  bool serialMonitoringEnabled;
  bool initialized;
  
  // Format timestamp for event logging
  void formatTimestamp(unsigned long timestamp, char* buffer, size_t bufferSize);
};

#endif // EVENT_LOGGER_H 