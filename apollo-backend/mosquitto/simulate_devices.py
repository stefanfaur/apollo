import time
import random
import paho.mqtt.client as mqtt

BROKER_URL = "localhost"
BROKER_PORT = 1883

DEVICES = [
    {"hardware_id": "hwid123", "device_type": "door_lock"},
    {"hardware_id": "hwid456", "device_type": "camera"},
    {"hardware_id": "hwid789", "device_type": "motion_sensor"},
]

NOTIFICATION_INTERVAL = (2, 10)
NOTIFICATION_COUNT = 10

# simulate a single device
def simulate_device(client, device):
    hardware_id = device["hardware_id"]
    device_type = device["device_type"]

    # send initial hello message
    hello_message = f"{hardware_id},{device_type}"
    client.publish("devices/hello", hello_message)
    print(f"Device {hardware_id} ({device_type}) sent hello message.")

    # send random notifications over time
    for _ in range(NOTIFICATION_COUNT):
        title = f"Notification {random.randint(1, 100)}"
        message = f"Random message {random.randint(1, 1000)}"
        notification_message = f"{hardware_id},{title},{message}"
        client.publish("devices/notifications", notification_message)
        print(f"Device {hardware_id} sent notification: {title} - {message}")

        # wait for a random interval before sending the next notification
        time.sleep(random.randint(*NOTIFICATION_INTERVAL))

# main function to simulate all devices
def main():
    client = mqtt.Client()

    try:
        client.connect(BROKER_URL, BROKER_PORT)
        print(f"Connected to MQTT broker at {BROKER_URL}:{BROKER_PORT}")

        # simulate each device in a loop
        for device in DEVICES:
            simulate_device(client, device)

        client.disconnect()
        print("Simulation completed. Disconnected from the broker.")

    except Exception as e:
        print(f"Error connecting to MQTT broker: {e}")

# Entry point
if __name__ == "__main__":
    main()
