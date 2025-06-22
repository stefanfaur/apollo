#ifndef DEBUG_SERIAL_H
#define DEBUG_SERIAL_H

#include <Arduino.h>
#if defined(STM32_SERIES_F4)
#include <SoftwareSerial.h>

// SoftwareSerial pins for debug console
#define DEBUG_SERIAL_RX_PIN PB7
#define DEBUG_SERIAL_TX_PIN PB6

void debug_serial_init();
void debug_print(const char* msg);
void debug_println(const char* msg);
void debug_printf(const char* format, ...);

SoftwareSerial& get_debug_serial();
#else
// Fallback macros for non-STM32 builds (unit-tests / host)
#define debug_serial_init()    Serial.begin(19200)
#define debug_print(msg)       Serial.print(msg)
#define debug_println(msg)     Serial.println(msg)
#define debug_printf(...)      Serial.printf(__VA_ARGS__)
#define get_debug_serial()     Serial
#endif

#endif // DEBUG_SERIAL_H 