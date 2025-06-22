#ifndef BUZZER_H
#define BUZZER_H

#include <Arduino.h>

class Buzzer {
public:
    explicit Buzzer(uint8_t pin);
    void begin();

    // Prompt tones
    void promptPlaceFinger();     // "Place finger" instruction
    void promptRemoveFinger();    // "Remove finger" instruction
    void promptPlaceAgain();      // "Place finger again" instruction

    // Enrollment outcome
    void enrollSuccess();
    void enrollFailure();

    // Match outcome
    void matchSuccess();
    void matchFailure();

    // Generic melody helper (public so other modules can reuse)
    void playMelody(const uint16_t* notes, const uint16_t* durations, size_t length);

private:
    uint8_t _pin;
    void playNote(uint16_t freq, uint16_t duration);
};

#endif // BUZZER_H 