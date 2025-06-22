#ifndef FINGERPRINT_SENSOR_H
#define FINGERPRINT_SENSOR_H

#include <Adafruit_Fingerprint.h>

// polling interval for finger detection (ms)
#define MATCH_POLL_INTERVAL_MS 50

// Library baud-rate code for 115 200 in Adafruit_Fingerprint::setBaudRate()
#define FP_BAUD_CODE_115200 6

class FingerprintSensor {
public:
    explicit FingerprintSensor(HardwareSerial* serial);

    bool begin();
    void update();
    void startEnrollment(uint8_t id);

    // Expose matching loop (mainly for tests)  
    void updateMatching();

private:
    enum class EnrollState : uint8_t {
        IDLE,
        ENROLL_START,
        WAIT_FIRST_PRESS,
        FIRST_PROCESSED,
        WAIT_REMOVE,
        WAIT_SECOND_PRESS,
        SECOND_PROCESSED,
        CREATE_MODEL,
        STORE_MODEL,
        SUCCESS,
        FAILURE
    };

    enum class MatchState : uint8_t {
        IDLE_POLLING,
        CAPTURE_IMAGE,
        SEARCH_DATABASE,
        HANDLE_SUCCESS,
        HANDLE_FAILURE
    };

    Adafruit_Fingerprint finger;
    EnrollState enrollState;

    unsigned long stateTimer;
    uint8_t enrollId;
    uint8_t errorCode;

    MatchState matchState;
    uint32_t last_check_time;

    void updateEnrollment();
};

#endif // FINGERPRINT_SENSOR_H 