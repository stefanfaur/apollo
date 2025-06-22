#include "Buzzer.h"

Buzzer::Buzzer(uint8_t pin) : _pin(pin) {}

void Buzzer::begin() {
    pinMode(_pin, OUTPUT);
    digitalWrite(_pin, LOW);
}

void Buzzer::playNote(uint16_t freq, uint16_t duration) {
    tone(_pin, freq, duration);
    delay(duration + 20); // allow tone to finish with small gap
}

void Buzzer::playMelody(const uint16_t* notes, const uint16_t* durations, size_t length) {
    for (size_t i = 0; i < length; ++i) {
        playNote(notes[i], durations[i]);
    }
    noTone(_pin);
}

/**************** Specific melodies ****************/ 

// Short rising chirp ("do-mi")
void Buzzer::promptPlaceFinger() {
    const uint16_t notes[]     = { 880, 1047 };          // A5, C6
    const uint16_t durations[] = { 120, 120 };
    playMelody(notes, durations, 2);
}

// Falling chirp ("mi-do")
void Buzzer::promptRemoveFinger() {
    const uint16_t notes[]     = { 1047, 880 };
    const uint16_t durations[] = { 120, 120 };
    playMelody(notes, durations, 2);
}

// Triple playful arpeggio
void Buzzer::promptPlaceAgain() {
    const uint16_t notes[]     = { 659, 784, 988 };      // E5, G5, B5
    const uint16_t durations[] = { 80, 80, 160 };
    playMelody(notes, durations, 3);
}

// Success jingle: pentatonic flourish
void Buzzer::enrollSuccess() {
    const uint16_t notes[]     = { 784, 988, 1175, 1568 }; // G5, B5, D6, G6
    const uint16_t durations[] = { 80, 80, 80, 200 };
    playMelody(notes, durations, 4);
}

// Failure buzz: minor 3rd drop + rumble
void Buzzer::enrollFailure() {
    const uint16_t notes[]     = { 988, 830, 659 }; // B5, G#5, E5
    const uint16_t durations[] = { 100, 180, 300 };
    playMelody(notes, durations, 3);
}

// Match success (quick double-ding)
void Buzzer::matchSuccess() {
    const uint16_t notes[]     = { 1175, 1568 }; // D6, G6
    const uint16_t durations[] = { 70, 200 };
    playMelody(notes, durations, 2);
}

// Match failure (short buzz sweep down)
void Buzzer::matchFailure() {
    // Descending sweep approximated by rapid descending notes
    const uint16_t notes[]     = { 1047, 988, 880, 784 };
    const uint16_t durations[] = { 60, 60, 60, 120 };
    playMelody(notes, durations, 4);
} 