#ifndef LOCK_CONTROLLER_H
#define LOCK_CONTROLLER_H

#include <Arduino.h>

class LockController {
public:
  LockController(uint8_t lockPin);
  
  // Initialize the lock controller
  void begin();
  
  // Process lock state in the main loop
  void update();
  
  // Unlock the door for a specific duration (milliseconds)
  void unlock(unsigned long durationMs);
  
  // Lock the door immediately
  void lock();
  
  // Check if door is currently unlocked
  bool isUnlocked() const;

private:
  uint8_t _lockPin;
  bool _unlocked;
  unsigned long _unlockStartTime;
  unsigned long _unlockDuration;
};

#endif // LOCK_CONTROLLER_H 