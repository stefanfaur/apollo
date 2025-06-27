"""Locust load-test for Apollo Smart-Home backend.

Requires the services & MQTT broker to be reachable from the machine/pod
executing Locust. End-points are configurable via environment variables so
this file works both locally and inside Kubernetes.
"""

# pylint: disable=import-error

from __future__ import annotations

import json
import os
import random
import threading
import time
import uuid
from typing import Any, Optional, List

from locust import HttpUser, User, between, events, task  # type: ignore
from locust.exception import StopUser  # type: ignore

try:
    # paho is an optional import for pure-HTTP users
    from paho.mqtt import client as mqtt  # type: ignore
except ImportError:
    mqtt = None  # type: ignore


# Shared state for coordinating between MQTT and HTTP users
registered_devices: List[str] = []  # hardware IDs that sent MQTT hello
device_lock = threading.Lock()


############################
# Helper / Config
############################

API_GATEWAY_URL = os.getenv("API_GATEWAY_URL", "http://localhost:8080")

MQTT_HOST = os.getenv("MQTT_HOST", "localhost")
MQTT_PORT = int(os.getenv("MQTT_PORT", "1883"))
MQTT_WS_PORT = int(os.getenv("MQTT_WS_PORT", "1884"))

# Locust records only HTTP out-of-the-box. We will manually fire custom
# events for MQTT publishes so they show up in the statistics panel.

def record_mqtt_success(request_type: str, name: str, start_time: float):
    total_time = int((time.time() - start_time) * 1000)  # ms
    events.request.fire(request_type=request_type, name=name, response_time=total_time, response_length=0)


def record_mqtt_failure(request_type: str, name: str, start_time: float, exc: Exception):
    total_time = int((time.time() - start_time) * 1000)
    events.request.fire(request_type=request_type, name=name, response_time=total_time, response_length=0, exception=exc)


############################
# MQTT Device User
############################

class MQTTDeviceUser(User):
    """Simulates a physical AMB82 device publishing to MQTT."""

    abstract = False
    wait_time = between(2, 5)

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        if mqtt is None:
            raise StopUser("paho-mqtt not installed – cannot run MQTT tasks")

        self.hardware_id: str = f"AMB82_{uuid.uuid4().hex[:6]}"
        # Using Any to avoid mypy/pylint complaints when mqtt is mocked/missing.
        self._mqtt_client: Any = mqtt.Client(client_id=self.hardware_id) if mqtt else None
        self._mqtt_client.on_message = self.on_message
        self._mqtt_client.on_connect = self.on_connect
        self._mqtt_client.connect(MQTT_HOST, MQTT_PORT, keepalive=30)
        self._mqtt_client.loop_start()

    ###################
    # MQTT Callbacks  #
    ###################

    def on_connect(self, client, userdata, flags, rc):  # noqa: D401, N803
        # Subscribe to command topics once connected
        client.subscribe("devices/commands/unlock")
        client.subscribe("doorlock/1/enroll/start")

    def on_message(self, client, userdata, msg):  # noqa: D401, N803
        # For the purposes of load-test we simply record success.
        record_mqtt_success("MQTT-SUB", msg.topic, start_time=time.time())

    ###################
    # Locust tasks    #
    ###################

    @task(2)
    def publish_hello(self):
        payload = {"hardwareId": self.hardware_id, "deviceType": "AMB82"}
        name = "devices/hello"
        start = time.time()
        try:
            self._mqtt_client.publish(name, json.dumps(payload), qos=0)
            record_mqtt_success("MQTT", name, start)
            
            # Register this device in shared state so ApiUser can use it
            with device_lock:
                if self.hardware_id not in registered_devices:
                    registered_devices.append(self.hardware_id)
                    
        except Exception as exc:  # pylint: disable=broad-except
            record_mqtt_failure("MQTT", name, start, exc)

    @task(5)
    def publish_notification(self):
        payload = {
            "hardwareId": self.hardware_id,
            "title": "Motion Detected",
            "message": "Motion detected near the door",
            "mediaUrl": "",  # keep empty so AI service is not triggered
            "eventType": "MOTION_DETECTED",
            "timestamp": str(int(time.time() * 1000)),
        }
        name = "devices/notifications"
        start = time.time()
        try:
            self._mqtt_client.publish(name, json.dumps(payload), qos=0)
            record_mqtt_success("MQTT", name, start)
        except Exception as exc:
            record_mqtt_failure("MQTT", name, start, exc)

    def on_stop(self):
        # Clean shutdown
        self._mqtt_client.loop_stop()
        self._mqtt_client.disconnect()


# Backend Command User removed - we now test the real user flow through API gateway


############################
# Public API Mobile App User
############################

class ApiUser(HttpUser):
    """Simulates a normal mobile application flow over the public APIs."""

    host = API_GATEWAY_URL  # All traffic routed through gateway
    wait_time = between(1, 3)

    def on_start(self):
        self.username = f"user_{uuid.uuid4().hex[:6]}"
        self.email = f"{self.username}@example.com"
        self.password = "pass1234!"

        # 1. Register (via gateway)
        self.client.post(
            "/api/auth/register",
            json={"username": self.username, "email": self.email, "password": self.password},
            name="POST /auth/register",
        )

        # 2. Login (retrieve JWT via gateway)
        resp = self.client.post(
            "/api/auth/login",
            json={"username": self.username, "password": self.password},
            name="POST /auth/login",
        )
        self.jwt = resp.json().get("token") if resp.status_code == 200 else None
        self.headers = {"Authorization": f"Bearer {self.jwt}"} if self.jwt else {}

        # 3. Create realistic number of homes (0-5, weighted toward 1-2)
        self.home_uuids: List[str] = []
        num_homes = random.choices(
            [0, 1, 2, 3, 4, 5], 
            weights=[5, 40, 30, 15, 7, 3],  # Most users have 1-2 homes, some have none, few have many
            k=1
        )[0]
        
        home_names = ["Main House", "Vacation Home", "Apartment", "Office", "Cabin", "Studio"]
        addresses = ["123 Main St", "456 Oak Ave", "789 Pine Rd", "321 Elm Dr", "654 Maple Ln", "987 Cedar Way"]
        
        for i in range(num_homes):
            resp = self.client.post(
                "/api/home",
                params={
                    "name": f"{random.choice(home_names)} {self.username}",
                    "address": f"{random.choice(addresses)}, City {i+1}"
                },
                headers=self.headers,
                name="POST /home",
            )
            if resp.status_code == 200:
                home_uuid = resp.json().get("uuid")
                if home_uuid:
                    self.home_uuids.append(home_uuid)
        
        # 4. Add realistic number of devices to each home (0-5 per home, weighted)
        for home_uuid in self.home_uuids:
            num_devices = random.choices(
                [0, 1, 2, 3, 4, 5],
                weights=[10, 35, 25, 20, 7, 3],  # Most homes have 1-2 devices, some empty, few have many
                k=1
            )[0]
            
            for _ in range(num_devices):
                # Try to add a device (may fail if no registered devices available yet)
                self._add_device_to_home(home_uuid)
    
    def _add_device_to_home(self, home_uuid: str):
        """Helper method to add a device to a specific home."""
        # Get a registered device from MQTT hello messages
        with device_lock:
            if not registered_devices:
                return  # Skip if no devices have sent hello messages yet
            hardware_id = random.choice(registered_devices)
        
        device_names = ["Front Door", "Back Door", "Garage Door", "Office Door", "Bedroom Door"]
        device_descriptions = [
            "Main entrance security", 
            "Backyard access control", 
            "Garage security system",
            "Office access point",
            "Bedroom privacy lock"
        ]
        
        payload = {
            "name": random.choice(device_names),
            "deviceType": "AMB82", 
            "description": random.choice(device_descriptions),
            "hardwareId": hardware_id,
        }
        self.client.post(
            f"/api/home/{home_uuid}/devices",
            json=payload,
            headers=self.headers,
            name="POST /home/{homeId}/devices",
        )

    # --- Tasks ---

    @task(6)
    def list_homes(self):
        # Users frequently check their homes list
        self.client.get(
            "/api/home",
            headers=self.headers,
            name="GET /home",
        )

    @task(3)
    def list_devices(self):
        # Users check all their devices occasionally
        self.client.get(
            "/api/devices",
            headers=self.headers,
            name="GET /devices",
        )

    @task(8)
    def get_home_devices(self):
        if not self.home_uuids:
            return  # Skip if no homes created
            
        # Pick a random home to get devices for (users check devices frequently)
        home_uuid = random.choice(self.home_uuids)
        self.client.get(
            f"/api/home/{home_uuid}/devices",
            headers=self.headers,
            name="GET /home/{homeId}/devices",
        )

    @task(1)
    def create_device_in_home(self):
        if not self.home_uuids:
            return  # Skip if no homes created
        
        # Occasionally add new devices (realistic: users don't add devices often)
        home_uuid = random.choice(self.home_uuids)
        self._add_device_to_home(home_uuid)
    
    @task(1)
    def get_specific_home_details(self):
        if not self.home_uuids:
            return  # Skip if no homes created
            
        # Users sometimes check details of a specific home
        home_uuid = random.choice(self.home_uuids)
        self.client.get(
            f"/api/home/{home_uuid}",
            headers=self.headers,
            name="GET /home/{homeId}",
        )
    
    @task(1)
    def get_home_admins(self):
        if not self.home_uuids:
            return  # Skip if no homes created
            
        # Users occasionally check who has admin access to their homes
        home_uuid = random.choice(self.home_uuids)
        self.client.get(
            f"/api/home/{home_uuid}/admins",
            headers=self.headers,
            name="GET /home/{homeId}/admins",
        )
    
    @task(1)
    def get_home_guests(self):
        if not self.home_uuids:
            return  # Skip if no homes created
            
        # Users occasionally check guest access to their homes
        home_uuid = random.choice(self.home_uuids)
        self.client.get(
            f"/api/home/{home_uuid}/guests",
            headers=self.headers,
            name="GET /home/{homeId}/guests",
        )

    @task(1)
    def search_users(self):
        # User search requires auth
        self.client.get(
            "/api/users/search",
            params={"email": "example", "page": 0, "size": 5},
            headers=self.headers,
            name="GET /users/search",
        )

    @task(5)
    def get_notifications(self):
        # Get notifications for current user (very common - users check notifications frequently)
        self.client.get(
            "/api/notification/user",
            headers=self.headers,
            name="GET /notification/user",
        )
    
    @task(2)
    def get_device_notifications(self):
        if not self.home_uuids:
            return  # Skip if no homes created
            
        # Pick a random home and device to get notifications for
        home_uuid = random.choice(self.home_uuids)
        
        # Get devices for this home first
        resp = self.client.get(
            f"/api/home/{home_uuid}/devices",
            headers=self.headers,
            name="GET /home/{homeId}/devices (for notifications)",
        )
        
        if resp.status_code == 200:
            devices = resp.json()
            if devices:
                # Pick a random device to get notifications for
                device = random.choice(devices)
                device_uuid = device.get("uuid")
                if device_uuid:
                    self.client.get(
                        f"/api/notification/device/{device_uuid}",
                        headers=self.headers,
                        name="GET /notification/device/{deviceId}",
                    )

    @task(2)
    def get_current_user(self):
        # Get current user info (users check their profile occasionally)
        self.client.get(
            "/api/users/me",
            headers=self.headers,
            name="GET /users/me",
        )

    @task(4)
    def remote_unlock_device(self):
        if not self.home_uuids:
            return  # Skip if no homes created
            
        # Pick a random home to unlock devices in (common user action)
        home_uuid = random.choice(self.home_uuids)
        
        # Get devices for this home first (real app flow)
        resp = self.client.get(
            f"/api/home/{home_uuid}/devices",
            headers=self.headers,
            name="GET /home/{homeId}/devices (for unlock)",
        )
        
        if resp.status_code == 200:
            devices = resp.json()
            if devices:
                # Pick a random device from the home to unlock
                device = random.choice(devices)
                device_uuid = device.get("uuid")
                if device_uuid:
                    self.client.post(
                        f"/api/devices/{device_uuid}/unlock",
                        headers=self.headers,
                        name="POST /devices/{deviceId}/unlock",
                    )

    @task(1) 
    def start_fingerprint_enrollment(self):
        if not self.home_uuids:
            return  # Skip if no homes created
            
        # Pick a random home for fingerprint enrollment
        home_uuid = random.choice(self.home_uuids)
        
        # Get devices for this home first (real app flow)
        resp = self.client.get(
            f"/api/home/{home_uuid}/devices",
            headers=self.headers,
            name="GET /home/{homeId}/devices (for enroll)",
        )
        
        if resp.status_code == 200:
            devices = resp.json()
            if devices:
                # Pick a random device from the home for fingerprint enrollment
                device = random.choice(devices)
                device_uuid = device.get("uuid")
                if device_uuid:
                    payload = {"userFpId": random.randint(1, 127)}
                    self.client.post(
                        f"/api/devices/{device_uuid}/fingerprint/enroll",
                        json=payload,
                        headers=self.headers,
                        name="POST /devices/{deviceId}/fingerprint/enroll",
                    )

    @task(1)
    def check_fingerprint_enrollment_status(self):
        if not self.home_uuids:
            return  # Skip if no homes created
            
        # Pick a random home to check enrollment status
        home_uuid = random.choice(self.home_uuids)
        
        # Get devices for this home first (real app flow)
        resp = self.client.get(
            f"/api/home/{home_uuid}/devices",
            headers=self.headers,
            name="GET /home/{homeId}/devices (for status)",
        )
        
        if resp.status_code == 200:
            devices = resp.json()
            if devices:
                # Pick a random device from the home to check enrollment status
                device = random.choice(devices)
                device_uuid = device.get("uuid")
                if device_uuid:
                    self.client.get(
                        f"/api/devices/{device_uuid}/fingerprint/enroll/status",
                        headers=self.headers,
                        name="GET /devices/{deviceId}/fingerprint/enroll/status",
                    )
