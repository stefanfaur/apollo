from locust import HttpUser, task, between

class APIPerformanceTest(HttpUser):
    wait_time = between(1, 2)
    token = None

    def on_start(self):
        response = self.client.post(
            "/api/auth/login",
            json={"username": "tester", "password": "password"}
        )
        if response.status_code == 200:
            self.token = response.json().get("token")

    @task(3)
    def create_and_fetch_homes(self):
        home_response = self.client.post(
            "/api/home",
            params={"name": "Performance Test Home", "address": "Test Address"},
            headers={"Authorization": f"Bearer {self.token}"}
        )
        if home_response.status_code == 200:
            home_uuid = home_response.json().get("uuid")
            self.client.get(
                f"/api/home/{home_uuid}",
                headers={"Authorization": f"Bearer {self.token}"}
            )
        self.client.get(
            "/api/home",
            headers={"Authorization": f"Bearer {self.token}"}
        )

    @task(3)
    def create_and_fetch_devices(self):
        home_response = self.client.post(
            "/api/home",
            params={"name": "Device Test Home", "address": "Device Address"},
            headers={"Authorization": f"Bearer {self.token}"}
        )
        if home_response.status_code == 200:
            home_uuid = home_response.json().get("uuid")
            device_response = self.client.post(
                f"/api/devices/{home_uuid}",
                params={
                    "name": "Test Device",
                    "deviceType": "SENSOR",
                    "description": "Device Description",
                    "hardwareId": "HW-Test-123"
                },
                headers={"Authorization": f"Bearer {self.token}"}
            )
            if device_response.status_code == 200:
                device_uuid = device_response.json().get("uuid")
                self.client.get(
                    f"/api/devices/{device_uuid}",
                    headers={"Authorization": f"Bearer {self.token}"}
                )
        self.client.get(
            "/api/devices",
            headers={"Authorization": f"Bearer {self.token}"}
        )

    @task(2)
    def get_user_notifications(self):
        self.client.get(
            "/api/notification/user",
            headers={"Authorization": f"Bearer {self.token}"}
        )

    @task(2)
    def get_user_profile(self):
        self.client.get(
            "/api/user/me",
            headers={"Authorization": f"Bearer {self.token}"}
        )

    @task(1)
    def fetch_notifications_for_device(self):
        home_response = self.client.post(
            "/api/home",
            params={"name": "Notification Test Home", "address": "Notif Address"},
            headers={"Authorization": f"Bearer {self.token}"}
        )
        if home_response.status_code == 200:
            home_uuid = home_response.json().get("uuid")
            device_response = self.client.post(
                f"/api/devices/{home_uuid}",
                params={
                    "name": "Notification Device",
                    "deviceType": "CAMERA",
                    "description": "Camera Device",
                    "hardwareId": "HW-Cam-123"
                },
                headers={"Authorization": f"Bearer {self.token}"}
            )
            if device_response.status_code == 200:
                device_uuid = device_response.json().get("uuid")
                self.client.get(
                    f"/api/notification/device/{device_uuid}",
                    headers={"Authorization": f"Bearer {self.token}"}
                )

