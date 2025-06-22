#ifndef EVENT_TYPE_H
#define EVENT_TYPE_H

#include <Arduino.h>

// Enum for sensor event types received from STM32
enum class EventType : uint8_t {
    MOTION_DETECTED = 0x01,
    DOOR_OPENED     = 0x02,
    DOOR_OPENED_UNAUTH = 0x04, 
    UNKNOWN        = 0xFF  
};

String eventTypeToString(EventType type);

#endif // EVENT_TYPE_H 