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
      // Determine if we should perform a digital or analog read based on
      // the configured pin mode. Digital reads return a HIGH/LOW value
      // while analog reads return a 10-/12-bit integer depending on the
      // MCU.  This simplifies the previous logic by always using
      // digitalRead for pins configured as INPUT / INPUT_PULLUP and
      // analogRead otherwise.

      if (_sensors[i].mode == INPUT || _sensors[i].mode == INPUT_PULLUP) {
        _sensors[i].lastValue = digitalRead(_sensors[i].pin);
      } else {
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