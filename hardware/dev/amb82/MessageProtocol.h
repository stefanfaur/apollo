#ifndef MESSAGE_PROTOCOL_H
#define MESSAGE_PROTOCOL_H

#include <Arduino.h>

// ----- Message Protocol Definitions -----
#define MSG_HEADER 0xAA

enum Command {
  CMD_START_VIDEO = 0x01,
  CMD_STOP_VIDEO  = 0x02,
  CMD_SENSOR_DATA = 0x03,
  CMD_ACK         = 0x04,
  CMD_MQTT_MSG    = 0x05,
  CMD_UNLOCK      = 0x06,
  CMD_SENSOR_EVENT = 0x07,
  CMD_ENROLL_START   = 0x08,
  CMD_ENROLL_SUCCESS = 0x50,
  CMD_ENROLL_FAILURE = 0x51,
  CMD_UNLOCK_FP      = 0x52,
  CMD_PROMPT_USER    = 0x53,
};

struct Message {
  uint8_t header;
  uint8_t command;
  uint8_t length;
  uint8_t payload[64]; // maximum payload size
  uint8_t checksum;
};

uint8_t calculateChecksum(uint8_t command, uint8_t length, uint8_t* payload);
void sendMessage(HardwareSerial &serialPort, uint8_t command, uint8_t* payload, uint8_t length);
bool readMessage(HardwareSerial &serialPort, Message &msg);

#endif // MESSAGE_PROTOCOL_H
