#include "SensorManager.h"

SensorManager::SensorManager() : _sensorCount(0) {
  // Initialize all sensors to invalid state
  for (int i = 0; i < MAX_SENSORS; i++) {
    _sensors[i].id = 0xFF; // Invalid ID
    _sensors[i].lastValue = 0;
  }
}

void SensorManager::begin() {
  // Initialize all registered sensors with their pin modes
  for (int i = 0; i < _sensorCount; i++) {
    pinMode(_sensors[i].pin, _sensors[i].mode);
  }
}

bool SensorManager::addSensor(uint8_t id, uint8_t pin, uint8_t mode) {
  // Check if we've reached the maximum number of sensors
  if (_sensorCount >= MAX_SENSORS) {
    return false;
  }
  
  // Check if sensor with this ID already exists
  if (hasSensor(id)) {
    return false;
  }
  
  // Add the sensor
  _sensors[_sensorCount].id = id;
  _sensors[_sensorCount].pin = pin;
  _sensors[_sensorCount].mode = mode;
  _sensors[_sensorCount].lastValue = 0;
  _sensorCount++;
  
  // Initialize pin mode
  pinMode(pin, mode);
  
  return true;
}

int SensorManager::readSensor(uint8_t id) {
  // Find the sensor with matching ID
  for (int i = 0; i < _sensorCount; i++) {
    if (_sensors[i].id == id) {
      // Read the sensor based on its mode
      if (_sensors[i].mode == INPUT || _sensors[i].mode == INPUT_PULLUP) {
        // For digital sensors
        if (_sensors[i].pin >= A0) {
          // Analog pin being used as digital input
          _sensors[i].lastValue = digitalRead(_sensors[i].pin);
        } else {
          // Regular analog read for analog pins
          _sensors[i].lastValue = analogRead(_sensors[i].pin);
        }
      } else {
        // For analog sensors
        _sensors[i].lastValue = analogRead(_sensors[i].pin);
      }
      return _sensors[i].lastValue;
    }
  }
  
  // Sensor not found
  return -1;
}

bool SensorManager::hasSensor(uint8_t id) const {
  for (int i = 0; i < _sensorCount; i++) {
    if (_sensors[i].id == id) {
      return true;
    }
  }
  return false;
}

uint8_t SensorManager::getSensorCount() const {
  return _sensorCount;
} 