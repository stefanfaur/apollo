# /// script
# dependencies = [
#   "paho-mqtt",
# ]
# ///

import paho.mqtt.client as mqtt
from datetime import datetime

BROKER = "localhost"
PORT = 1883
TOPIC = "#"  # Subscribe to all topics

def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print(f"[{datetime.now().strftime('%Y-%m-%d %H:%M:%S')}] Connected to MQTT broker at {BROKER}:{PORT}")
        client.subscribe(TOPIC)
        print(f"Subscribed to all topics ('{TOPIC}')")
    else:
        print(f"Failed to connect, return code {rc}")

def on_message(client, userdata, msg):
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    payload = msg.payload.decode('utf-8', errors='ignore')
    print(f"[{timestamp}] Topic: {msg.topic}\n    Message: {payload}\n")


def main():
    client = mqtt.Client()
    client.on_connect = on_connect
    client.on_message = on_message

    client.connect(BROKER, PORT, keepalive=60)

    client.loop_forever()


if __name__ == "__main__":
    main()
