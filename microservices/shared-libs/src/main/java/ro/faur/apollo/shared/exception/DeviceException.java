package ro.faur.apollo.shared.exception;

/**
 * Device-related exceptions for inter-service communication.
 * These exceptions are used across multiple microservices to maintain consistency.
 */
public class DeviceException {

    /**
     * Thrown when a device with the specified hardware ID is not registered in the system.
     * This typically happens when trying to link a device to a home before the device
     * has sent its initial MQTT hello message.
     */
    public static class DeviceNotRegisteredException extends RuntimeException {
        public DeviceNotRegisteredException(String message) {
            super(message);
        }
        
        public DeviceNotRegisteredException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Thrown when trying to link a device to a home, but the device is already
     * linked to another home. A device can only belong to one home at a time.
     */
    public static class DeviceAlreadyLinkedException extends RuntimeException {
        public DeviceAlreadyLinkedException(String message) {
            super(message);
        }
        
        public DeviceAlreadyLinkedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
} 