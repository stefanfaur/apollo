#include "EventType.h"

String eventTypeToString(EventType type) {
    switch (type) {
        case EventType::MOTION_DETECTED:
            return "Motion detected";
        case EventType::DOOR_OPENED:
            return "Door opened";
        case EventType::DOOR_OPENED_UNAUTH:
            return "Door opened (Unauthorized)";
        case EventType::UNKNOWN:
        default:
            return "Unknown event";
    }
} 