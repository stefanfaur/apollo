#include "debug_serial.h"

#if defined(STM32_SERIES_F4)
// Instantiate SoftwareSerial for debug
SoftwareSerial stm32_debug_serial(DEBUG_SERIAL_RX_PIN, DEBUG_SERIAL_TX_PIN);

void debug_serial_init() {
    stm32_debug_serial.begin(38400);
    delay(100);
}

void debug_print(const char* msg) {
    stm32_debug_serial.print(msg);
}

void debug_println(const char* msg) {
    stm32_debug_serial.println(msg);
}

void debug_printf(const char* format, ...) {
    char buffer[128];
    va_list args;
    va_start(args, format);
    vsnprintf(buffer, sizeof(buffer), format, args);
    va_end(args);
    stm32_debug_serial.print(buffer);
}

SoftwareSerial& get_debug_serial() {
    return stm32_debug_serial;
}
#endif // STM32_SERIES_F4 