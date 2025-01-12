from locust import HttpUser, task, between
import random
import string

class APIPerformanceTest(HttpUser):
    wait_time = between(1, 2)
    token = None
    user_data = {}
    created_homes = []

    def generate_random_string(self, length=8):
        return ''.join(random.choices(string.ascii_letters + string.digits, k=length))

    def on_start(self):
        if not self.user_data:
            self.register_user()
        self.login_user()

    def register_user(self):
        username = f"user_{self.generate_random_string()}"
        password = "password123"
        email = f"{username}@example.com"
        response = self.client.post(
            "/api/auth/register",
            json={"username": username, "password": password, "email": email}
        )
        if response.status_code == 200:
            self.user_data = {"username": username, "password": password}

    def login_user(self):
        response = self.client.post(
            "/api/auth/login",
            json={
                "username": self.user_data["username"],
                "password": self.user_data["password"]
            }
        )
        if response.status_code == 200:
            self.token = response.json().get("token")

    def create_home(self):
        response = self.client.post(
            "/api/home",
            params={
                "name": f"Home-{self.generate_random_string()}",
                "address": f"{self.generate_random_string()} Street"
            },
            headers={"Authorization": f"Bearer {self.token}"}
        )
        if response.status_code == 200:
            home_uuid = response.json().get("uuid")
            self.created_homes.append(home_uuid)

    def create_device(self, home_uuid):
        self.client.post(
            f"/api/devices/{home_uuid}",
            params={
                "name": f"Device-{self.generate_random_string()}",
                "deviceType": random.choice(["SENSOR", "CAMERA", "DOORLOCK"]),
                "description": f"Description-{self.generate_random_string()}",
                "hardwareId": self.generate_random_string()
            },
            headers={"Authorization": f"Bearer {self.token}"}
        )

    @task(1)
    def manage_homes_and_devices(self):
        if len(self.created_homes) < 6:
            self.create_home()

        for home_uuid in self.created_homes:
            for _ in range(5):
                self.create_device(home_uuid)

        self.client.get(
            "/api/home",
            headers={"Authorization": f"Bearer {self.token}"}
        )

    @task(1)
    def fetch_user_notifications(self):
        self.client.get(
            "/api/notification/user",
            headers={"Authorization": f"Bearer {self.token}"}
        )

    @task(1)
    def fetch_user_profile(self):
        self.client.get(
            "/api/user/me",
            headers={"Authorization": f"Bearer {self.token}"}
        )

    @task(1)
    def fetch_home_and_device_data(self):
        if self.created_homes:
            home_uuid = random.choice(self.created_homes)
            self.client.get(
                f"/api/home/{home_uuid}",
                headers={"Authorization": f"Bearer {self.token}"}
            )
            response = self.client.get(
                f"/api/devices/{home_uuid}",
                headers={"Authorization": f"Bearer {self.token}"}
            )
            if response.status_code == 200:
                devices = response.json()
                if devices:
                    device_uuid = random.choice(devices).get("uuid")
                    self.client.get(
                        f"/api/notification/device/{device_uuid}",
                        headers={"Authorization": f"Bearer {self.token}"}
                    )
