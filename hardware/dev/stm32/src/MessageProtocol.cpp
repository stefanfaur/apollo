#include "MessageProtocol.h"

uint8_t calculateChecksum(uint8_t command, uint8_t length, uint8_t* payload) {
  uint8_t sum = command + length;
  for (int i = 0; i < length; i++) {
    sum += payload[i];
  }
  return sum;
}

void sendMessage(HardwareSerial &serialPort, uint8_t command, uint8_t* payload, uint8_t length) {
  uint8_t checksum = calculateChecksum(command, length, payload);
  serialPort.write(MSG_HEADER);
  serialPort.write(command);
  serialPort.write(length);
  for (int i = 0; i < length; i++) {
    serialPort.write(payload[i]);
  }
  serialPort.write(checksum);
}

// Non-blocking readMessage with per-byte timeout
bool readMessage(HardwareSerial &serialPort, Message &msg) {
  const unsigned long timeoutMs = 50;  // timeout for each byte
  unsigned long start;

  // Wait until at least 4 bytes are available or timeout.
  start = millis();
  while (serialPort.available() < 4 && (millis() - start < timeoutMs)) {
    yield();
  }
  if (serialPort.available() < 4) {
    return false;
  }

  if (serialPort.read() != MSG_HEADER) {
    return false;
  }
  msg.command = serialPort.read();
  msg.length = serialPort.read();
  
  if (msg.length > sizeof(msg.payload)) {
    // Flush invalid message
    for (int i = 0; i < msg.length; i++) {
      serialPort.read();
    }
    serialPort.read(); // checksum
    return false;
  }
  
  for (int i = 0; i < msg.length; i++) {
    start = millis();
    while (!serialPort.available() && (millis() - start < timeoutMs)) {
      yield();
    }
    if (!serialPort.available()) {
      return false;
    }
    msg.payload[i] = serialPort.read();
  }
  
  start = millis();
  while (!serialPort.available() && (millis() - start < timeoutMs)) {
    yield();
  }
  if (!serialPort.available()) {
    return false;
  }
  msg.checksum = serialPort.read();
  
  uint8_t calc = calculateChecksum(msg.command, msg.length, msg.payload);
  return (calc == msg.checksum);
}
