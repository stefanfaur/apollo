#include "fingerprint_sensor.h"
#include "MessageProtocol.h"
#include "LockController.h"
#include "debug_serial.h"  // Added for detailed debug output
#include "Buzzer.h"

// External helper from main.cpp to send status back to AMB82
extern void sendStatusMessage(uint8_t command, uint8_t* payload, uint8_t length);
extern LockController lockController;
extern Buzzer buzzer;

#ifndef FP_BAUD
#define FP_BAUD 115200
#endif

// Legacy constant no longer used (retained for reference)
// static const uint16_t MATCH_INTERVAL_MS = 200;

static const uint16_t ENROLL_TIMEOUT_MS = 20000;
static const uint16_t REMOVE_TIMEOUT_MS = 5000;

FingerprintSensor::FingerprintSensor(HardwareSerial* serial)
    : finger(serial), enrollState(EnrollState::IDLE), stateTimer(0), enrollId(0), errorCode(0),
      matchState(MatchState::IDLE_POLLING), last_check_time(0) {}

bool FingerprintSensor::begin() {
    debug_println("[FP] Initializing fingerprint sensor...");

    // 1) Try at the desired high baud rate first
    finger.begin(FP_BAUD);
    if (finger.verifyPassword()) {
        debug_println("[FP] Sensor verified at high baud.");
        return true;
    }

    // 2) Fallback to the sensor's default rate (57 600)
    const uint32_t DEFAULT_BAUD = 57600;
    debug_println("[FP] High-baud verify failed – trying default 57 600 …");

    finger.begin(DEFAULT_BAUD);
    if (!finger.verifyPassword()) {
        debug_println("[FP][ERROR] Sensor not responding at default baud either – check wiring.");
        return false;
    }

    debug_println("[FP] Sensor detected at 57 600 – switching it to high speed …");

    // Adafruit_Fingerprint uses code 6 for 115200 (see library docs)
    const uint8_t CODE_115200 = 6;
    if (finger.setBaudRate(CODE_115200) != FINGERPRINT_OK) {
        debug_println("[FP][ERROR] Failed to set sensor baud rate to 115 200.");
        return false;
    }

    delay(100); // give sensor time to reboot at the new speed

    // 3) Re-initialise at the target baud and verify again
    finger.begin(FP_BAUD);
    if (finger.verifyPassword()) {
        debug_println("[FP] Successfully switched sensor to 115 200 baud.");
        return true;
    }

    debug_println("[FP][ERROR] Verification failed after baud switch – staying at default speed.");
    // Optionally: leave finger.begin(DEFAULT_BAUD) so at least it works, but for now report failure.
    return false;
}

void FingerprintSensor::startEnrollment(uint8_t id) {
    if (enrollState == EnrollState::IDLE) {
        enrollId = id;
        enrollState = EnrollState::ENROLL_START;
        debug_printf("[FP] Starting enrollment for ID %u\n", id);
    } else {
        debug_println("[FP][WARN] Cannot start enrollment – sensor busy.");
    }
}

void FingerprintSensor::update() {
    if (enrollState == EnrollState::IDLE) {
        updateMatching();
    } else {
        updateEnrollment();
    }
}

/******************** Matching ***************************/
void FingerprintSensor::updateMatching() {
    // Polling stage – lightweight finger presence check.
    if (matchState == MatchState::IDLE_POLLING) {
        if (millis() - last_check_time < MATCH_POLL_INTERVAL_MS) {
            return; // wait until next poll window
        }
        last_check_time = millis();

        if (finger.getImage() == FINGERPRINT_OK) {
            debug_println("[FP] Finger detected – capturing image");
            matchState = MatchState::CAPTURE_IMAGE;
        }
        return; // early exit regardless of result
    }

    // Once we leave IDLE_POLLING we process without artificial delay.
    int p = -1;
    switch (matchState) {
        case MatchState::CAPTURE_IMAGE: {
            p = finger.image2Tz();
            if (p == FINGERPRINT_OK) {
                matchState = MatchState::SEARCH_DATABASE;
            } else {
                debug_printf("[FP][WARN] image2Tz() failed: %d\n", p);
                matchState = MatchState::HANDLE_FAILURE;
            }
            break; }

        case MatchState::SEARCH_DATABASE: {
            p = finger.fingerFastSearch();
            if (p == FINGERPRINT_OK) {
                matchState = MatchState::HANDLE_SUCCESS;
            } else if (p == FINGERPRINT_NOTFOUND) {
                debug_println("[FP] No fingerprint match found.");
                matchState = MatchState::HANDLE_FAILURE;
            } else {
                debug_printf("[FP][WARN] fingerFastSearch() error: %d\n", p);
                matchState = MatchState::HANDLE_FAILURE;
            }
            break; }

        case MatchState::HANDLE_SUCCESS: {
            debug_printf("[FP] Match found! ID=%u, confidence=%u\n", finger.fingerID, finger.confidence);
            buzzer.matchSuccess();
            uint8_t payload[] = { (uint8_t)finger.fingerID };
            lockController.unlock(3000);
            sendStatusMessage(CMD_UNLOCK_FP, payload, 1);

            // Allow time for user to remove finger to prevent duplicate triggers
            delay(1000);

            matchState = MatchState::IDLE_POLLING;
            break; }

        case MatchState::HANDLE_FAILURE: {
            buzzer.matchFailure();
            // Optionally you could signal host here that match failed.
            matchState = MatchState::IDLE_POLLING;
            break; }

        default: {
            matchState = MatchState::IDLE_POLLING;
            break; }
    }
}

/******************** Enrollment *************************/
void FingerprintSensor::updateEnrollment() {
    int p;
    switch(enrollState) {
        case EnrollState::ENROLL_START: {
            debug_println("[FP] ENROLL_START → Prompting user to place finger (step 1)");
            uint8_t prompt = 0x01; // PLACE_FINGER
            buzzer.promptPlaceFinger();
            sendStatusMessage(CMD_PROMPT_USER, &prompt, 1);
            stateTimer = millis();
            enrollState = EnrollState::WAIT_FIRST_PRESS;
            break; }

        case EnrollState::WAIT_FIRST_PRESS: {
            if (millis() - stateTimer > ENROLL_TIMEOUT_MS) {
                debug_println("[FP][ERROR] WAIT_FIRST_PRESS timeout");
                errorCode = 0x01; // TIMEOUT
                enrollState = EnrollState::FAILURE;
                break;
            }
            p = finger.getImage();
            if (p == FINGERPRINT_OK) {
                debug_println("[FP] First image captured, processing...");
                enrollState = EnrollState::FIRST_PROCESSED;
            }
            break; }

        case EnrollState::FIRST_PROCESSED: {
            if (finger.image2Tz(1) == FINGERPRINT_OK) {
                debug_println("[FP] FIRST_PROCESSED → Prompting user to remove finger");
                uint8_t prompt = 0x02; // REMOVE_FINGER
                buzzer.promptRemoveFinger();
                sendStatusMessage(CMD_PROMPT_USER, &prompt, 1);
                stateTimer = millis();
                enrollState = EnrollState::WAIT_REMOVE;
            } else {
                debug_println("[FP][ERROR] image2Tz(1) failed during FIRST_PROCESSED");
                errorCode = 0x04; // SENSOR_ERROR
                enrollState = EnrollState::FAILURE;
            }
            break; }

        case EnrollState::WAIT_REMOVE: {
            if (millis() - stateTimer > REMOVE_TIMEOUT_MS) {
                debug_println("[FP][ERROR] WAIT_REMOVE timeout");
                errorCode = 0x01; // TIMEOUT
                enrollState = EnrollState::FAILURE;
                break;
            }
            p = finger.getImage();
            if (p == FINGERPRINT_NOFINGER) {
                debug_println("[FP] Finger removed – prompt to place again");
                uint8_t prompt = 0x03; // PLACE_FINGER_AGAIN
                buzzer.promptPlaceAgain();
                sendStatusMessage(CMD_PROMPT_USER, &prompt, 1);
                stateTimer = millis();
                enrollState = EnrollState::WAIT_SECOND_PRESS;
            }
            break; }

        case EnrollState::WAIT_SECOND_PRESS: {
            if (millis() - stateTimer > ENROLL_TIMEOUT_MS) {
                debug_println("[FP][ERROR] WAIT_SECOND_PRESS timeout");
                errorCode = 0x01; // TIMEOUT
                enrollState = EnrollState::FAILURE;
                break;
            }
            p = finger.getImage();
            if (p == FINGERPRINT_OK) {
                debug_println("[FP] Second image captured, processing...");
                enrollState = EnrollState::SECOND_PROCESSED;
            }
            break; }

        case EnrollState::SECOND_PROCESSED: {
            if (finger.image2Tz(2) == FINGERPRINT_OK) {
                debug_println("[FP] SECOND_PROCESSED complete – creating model");
                enrollState = EnrollState::CREATE_MODEL;
            } else {
                debug_printf("[FP][ERROR] image2Tz(2) failed: %d\n", p);
                errorCode = 0x04;
                enrollState = EnrollState::FAILURE;
            }
            break; }

        case EnrollState::CREATE_MODEL: {
            p = finger.createModel();
            if (p == FINGERPRINT_OK) {
                debug_println("[FP] Model created successfully – storing...");
                enrollState = EnrollState::STORE_MODEL;
            } else if (p == FINGERPRINT_ENROLLMISMATCH) {
                debug_println("[FP][ERROR] CREATE_MODEL mismatch");
                errorCode = 0x02; // MISMATCH
                enrollState = EnrollState::FAILURE;
            } else {
                debug_printf("[FP][ERROR] createModel failed: %d\n", p);
                errorCode = 0x04;
                enrollState = EnrollState::FAILURE;
            }
            break; }

        case EnrollState::STORE_MODEL: {
            p = finger.storeModel(enrollId);
            if (p == FINGERPRINT_OK) {
                debug_println("[FP] Template stored successfully – enrollment SUCCESS");
                enrollState = EnrollState::SUCCESS;
                matchState = MatchState::IDLE_POLLING;
            } else {
                debug_printf("[FP][ERROR] storeModel failed: %d\n", p);
                errorCode = 0x03; // STORAGE_FAILED
                enrollState = EnrollState::FAILURE;
                matchState = MatchState::IDLE_POLLING;
            }
            break; }

        case EnrollState::SUCCESS: {
            debug_println("[FP] Enrollment SUCCESS – notifying host");
            buzzer.enrollSuccess();
            uint8_t payload[] = { enrollId };
            sendStatusMessage(CMD_ENROLL_SUCCESS, payload, 1);
            enrollState = EnrollState::IDLE;
            matchState = MatchState::IDLE_POLLING;
            break; }

        case EnrollState::FAILURE: {
            debug_printf("[FP] Enrollment FAILURE code 0x%02X – notifying host\n", errorCode);
            buzzer.enrollFailure();
            uint8_t payload[] = { errorCode };
            sendStatusMessage(CMD_ENROLL_FAILURE, payload, 1);
            enrollState = EnrollState::IDLE;
            matchState = MatchState::IDLE_POLLING;
            break; }

        default:
            break;
    }
} 