#include "EventHandler.h"
#include <Arduino.h>

EventHandler::EventHandler(VideoHandler& video, InternalHttpClient& http, MqttClient& mqtt, EventLogger& logger)
    : videoHandler(video), httpClient(http), mqttClient(mqtt), eventLogger(logger), 
      isWaitingForUpload(false), lastEventType(EventType::UNKNOWN) 
{ 
}

void EventHandler::begin() {
    Serial.println("EventHandler initialized.");
}

void EventHandler::handleEvent(EventType type, int value) {
    String eventDescription = eventTypeToString(type);
    Serial.print("EventHandler received event: ");
    Serial.print(eventDescription);
    Serial.print(", Value: ");
    Serial.println(value);

    // Log the event first
    eventLogger.logEvent(value, eventDescription.c_str());

    // Helper used by multiple cases for publishing
    String eventTypeStr;

    // Perform actions based on the specific event type
    switch (type) {
        case EventType::MOTION_DETECTED:
            Serial.println("Motion detected! Starting 10s video recording (via EventHandler).");
            // Start a 10-second video recording, enable upload
            if (videoHandler.startRecording(10000, true)) { 
                isWaitingForUpload = true; // Set flag to wait for completion
                lastEventType = type;      // Store the triggering event type
            } else {
                 Serial.println("ERROR: Failed to start recording from EventHandler.");
            }
            break;

        case EventType::DOOR_OPENED:
        case EventType::DOOR_OPENED_2: {
            Serial.println("Door opened event received (handled by EventHandler).");
            extern const char* MQTT_NOTIFICATION_TOPIC;
            extern const char* HARDWARE_ID;
            eventTypeStr = eventTypeToString(lastEventType);
            mqttClient.publishNotification(MQTT_NOTIFICATION_TOPIC, HARDWARE_ID, 
                                         "Door Opened", "Door opened event received", "", eventTypeStr.c_str());
            break;
        }

        case EventType::DOOR_OPENED_UNAUTH:
            Serial.println("Unauthorized door open event received (handled by EventHandler).");
            if (videoHandler.startRecording(10000, true)) { 
                isWaitingForUpload = true; // Set flag to wait for completion
                lastEventType = type;      // Store the triggering event type
            } else {
                 Serial.println("ERROR: Failed to start recording from EventHandler.");
            }
            break;

        case EventType::FINGERPRINT_FAILURE: {
            Serial.println("Fingerprint authentication failure detected. Starting 5s video recording (via EventHandler).");
            // Start a 5-second video recording, enable upload
            if (videoHandler.startRecording(5000, true)) {
                isWaitingForUpload = true;
                lastEventType = type;
            } else {
                Serial.println("ERROR: Failed to start recording from EventHandler.");
            }
            break;
        }

        case EventType::UNKNOWN:
        default:
            Serial.println("Received unknown sensor event type (handled by EventHandler).");
            break;
    }
}

void EventHandler::update() {
    // Check if we were waiting for a recording to finish
    if (isWaitingForUpload && !videoHandler.isRecording()) {
        Serial.println("Recording finished, triggering upload and notification (via EventHandler).");
        _uploadAndNotify();
        isWaitingForUpload = false; // Reset the flag
        lastEventType = EventType::UNKNOWN; // Reset the event type
    }
}

// Private method to handle the upload and notification task
void EventHandler::_uploadAndNotify() {
    if (!videoHandler.shouldUploadRecording()) {
        Serial.println("Upload skipped for this recording (upload flag was false).");
        return;
    }

    String videoPath = videoHandler.getVideoFilePath();
    if (videoPath.length() == 0) {
        Serial.println("Error: Video file path is empty. Cannot upload.");
        return;
    }
    
    Serial.print("EventHandler starting upload for: ");
    Serial.println(videoPath);

    // Pass the shared fatfs object from videoHandler
    if (httpClient.uploadFile(videoPath, videoHandler.fatfs)) {
        String mediaUrl = httpClient.getUploadedFileUrl();
        Serial.print("Upload successful (EventHandler). Media URL: ");
        Serial.println(mediaUrl);

        // Prepare notification message
        String title = "Video Recording Alert";
        String message;
        if (eventLogger.hasEvents()) {
            message = eventLogger.getEventSummary(); // Get summary of events during recording
        } else {
            message = "Video recording completed, no specific events detected during recording.";
        }

        // Get the string representation of the event that triggered this
        String eventTypeStr = eventTypeToString(lastEventType);

        extern const char* MQTT_NOTIFICATION_TOPIC;
        extern const char* HARDWARE_ID;

        char timestampBuffer[16];
        unsigned long now = millis();
        snprintf(timestampBuffer, sizeof(timestampBuffer), "%lu", now);

        // Publish notification via MQTT, including the event type
        if (mqttClient.publishNotification(MQTT_NOTIFICATION_TOPIC, HARDWARE_ID, 
                                         eventTypeStr.c_str(), message.c_str(), mediaUrl.c_str(), timestampBuffer)) {
            Serial.println("MQTT notification sent successfully (EventHandler).");
        } else {
            Serial.println("Failed to send MQTT notification (EventHandler).");
        }
    } else {
        Serial.println("Failed to upload video file (EventHandler).");
    }
    
    // Clear events after upload attempt (success or fail) associated with this recording cycle
    eventLogger.clear(); 
}

// External recording start notification
void EventHandler::startExternalRecording(EventType type) {
    isWaitingForUpload = true;
    lastEventType = type;
} 