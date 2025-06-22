#ifndef SENSOR_MANAGER_H
#define SENSOR_MANAGER_H

#include <Arduino.h>

// Maximum number of sensors that can be managed
#define MAX_SENSORS 8

struct Sensor {
  uint8_t id;
  uint8_t pin;
  uint8_t mode;
  int lastValue;
};

class SensorManager {
public:
  SensorManager();
  
  // Initialize the sensor manager
  void begin();
  
  // Add a sensor to be managed
  bool addSensor(uint8_t id, uint8_t pin, uint8_t mode);
  
  // Read a sensor value by ID
  int readSensor(uint8_t id);
  
  // Check if a sensor exists
  bool hasSensor(uint8_t id) const;
  
  // Get the total number of registered sensors
  uint8_t getSensorCount() const;
  
private:
  Sensor _sensors[MAX_SENSORS];
  uint8_t _sensorCount;
};

#endif // SENSOR_MANAGER_H 