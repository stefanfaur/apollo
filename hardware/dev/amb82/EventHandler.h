#ifndef EVENT_HANDLER_H
#define EVENT_HANDLER_H

#include "EventType.h"
#include "VideoHandler.h"
#include "MqttClient.h"
#include "EventLogger.h"
#include "InternalHttpClient.h"

class EventHandler {
public:
    EventHandler(VideoHandler& video, InternalHttpClient& http, MqttClient& mqtt, EventLogger& logger);
    
    void begin();
    
    // Handle an incoming sensor event
    void handleEvent(EventType type, int value);
    
    // Update method to check for state changes (e.g., recording finished)
    void update();

private:
    VideoHandler& videoHandler;
    InternalHttpClient& httpClient;
    MqttClient& mqttClient;
    EventLogger& eventLogger;
    
    bool isWaitingForUpload;
    EventType lastEventType;
    
    // Private method to handle upload and notification logic
    void _uploadAndNotify();
};

#endif // EVENT_HANDLER_H 