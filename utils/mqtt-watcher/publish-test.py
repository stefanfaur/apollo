# /// script
# dependencies = [
#   "paho-mqtt",
# ]
# ///

import paho.mqtt.client as mqtt
import time
import random
from datetime import datetime

# Configuration
BROKER = "localhost"
PORT = 1883
TOPICS = [
    "devices/hello",
    "devices/temperature",
    "devices/status"
]
PUBLISH_COUNT = 10      # Number of rounds to publish
PUBLISH_INTERVAL = 1.0  # Seconds between each round

# Generate a mock payload for a given topic
def generate_payload(topic, iteration):
    if topic == "devices/hello":
        return f"Hello #{iteration} from mock device"
    elif topic == "devices/temperature":
        # Random temperature between 20.0 and 30.0
        return f"{random.uniform(20.0, 30.0):.2f}"
    elif topic == "devices/status":
        return random.choice(["online", "offline", "idle"])
    else:
        return ""


def main():
    # Initialize MQTT client
    client = mqtt.Client()
    client.connect(BROKER, PORT, keepalive=60)

    print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] Connected to MQTT broker at {BROKER}:{PORT}")

    # Publish mock messages
    for i in range(1, PUBLISH_COUNT + 1):
        timestamp = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        for topic in TOPICS:
            payload = generate_payload(topic, i)
            client.publish(topic, payload)
            print(f"[{timestamp}] Published to {topic}: {payload}")
        time.sleep(PUBLISH_INTERVAL)

    client.disconnect()
    print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] Disconnected from MQTT broker")


if __name__ == "__main__":
    main()
