#include "LockController.h"

LockController::LockController(uint8_t lockPin) 
  : _lockPin(lockPin), _unlocked(false), _unlockStartTime(0), _unlockDuration(0) {
}

void LockController::begin() {
  pinMode(_lockPin, OUTPUT);
  digitalWrite(_lockPin, LOW); // Ensure it starts in locked state
  _unlocked = false;
}

void LockController::update() {
  // Check if unlock duration has expired
  if (_unlocked && (millis() - _unlockStartTime >= _unlockDuration)) {
    lock();
  }
}

void LockController::unlock(unsigned long durationMs) {
  digitalWrite(_lockPin, HIGH); // Activate unlock mechanism
  _unlocked = true;
  _unlockStartTime = millis();
  _unlockDuration = durationMs;
  Serial.println("Door unlocked");
}

void LockController::lock() {
  digitalWrite(_lockPin, LOW); // Deactivate unlock mechanism
  _unlocked = false;
  Serial.println("Door locked");
}

bool LockController::isUnlocked() const {
  return _unlocked;
} 