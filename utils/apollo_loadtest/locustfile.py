
from __future__ import annotations
import json, logging, os, random, string, time
from dataclasses import dataclass, field
from threading import Lock
from typing import Any, List, Dict, Optional
from collections import defaultdict, deque

from locust import HttpUser, between, events, task
try:
    import paho.mqtt.client as mqtt
except ImportError:  # pragma: no cover
    mqtt = None

# Endpoint Health Monitoring & Circuit Breaker
@dataclass
class EndpointHealth:
    response_times: deque = field(default_factory=lambda: deque(maxlen=50))  # Last 50 requests
    failure_count: int = 0
    success_count: int = 0
    last_failure_time: float = 0
    circuit_open: bool = False
    last_circuit_check: float = 0
    
    @property
    def avg_response_time(self) -> float:
        return sum(self.response_times) / len(self.response_times) if self.response_times else 0
    
    @property
    def failure_rate(self) -> float:
        total = self.failure_count + self.success_count
        return self.failure_count / total if total > 0 else 0
    
    def should_skip_request(self) -> bool:
        now = time.time()
        
        if self.circuit_open and (now - self.last_circuit_check) > 30:  # Try again after 30s
            self.circuit_open = False
            self.last_circuit_check = now
            return False
        
        # Open circuit if too many failures or high response time
        if (self.failure_rate > 0.5 and (self.failure_count + self.success_count) > 10) or \
           (self.avg_response_time > 5000):  # 5 second threshold
            if not self.circuit_open:
                log.warning("Opening circuit for endpoint - failure_rate=%.2f, avg_time=%.1fms", 
                           self.failure_rate, self.avg_response_time)
            self.circuit_open = True
            self.last_circuit_check = now
            return True
        
        return False

class HealthMonitor:
    def __init__(self):
        self.endpoints: Dict[str, EndpointHealth] = defaultdict(EndpointHealth)
        self.lock = Lock()
    
    def record_request(self, endpoint: str, response_time: float, success: bool):
        with self.lock:
            health = self.endpoints[endpoint]
            health.response_times.append(response_time)
            
            if success:
                health.success_count += 1
            else:
                health.failure_count += 1
                health.last_failure_time = time.time()
    
    def should_skip_endpoint(self, endpoint: str) -> bool:
        with self.lock:
            return self.endpoints[endpoint].should_skip_request()
    
    def get_endpoint_health_factor(self, endpoint: str) -> float:
        with self.lock:
            health = self.endpoints[endpoint]
            if health.circuit_open:
                return 0.1
            elif health.failure_rate > 0.3:
                return 0.5
            elif health.avg_response_time > 2000:
                return 0.7
            else:
                return 1.0

health_monitor = HealthMonitor()

API_PREFIX = ""
WAIT_TIME = between(1, 8)
MAX_HOMES, MAX_DEVICES = 2, 10
MQTT_PROB = 0.30
DEVICE_ACCESS_RIGHTS = [
    "VIEW_IMAGES",
    "VIEW_LIVE_STREAM", 
    "VIEW_NOTIFICATIONS",
    "UNLOCK_DOOR",
    "ENROLL_FINGERPRINT",
    "DELETE_FINGERPRINT",
    "ENROLL_FACE",
    "DELETE_FACE",
    "ENROLL_RFID",
    "DELETE_RFID",
    "VIEW_DEVICE_INFO",
    "VIEW_DEVICE_SETTINGS"
]
POOL: Dict[str, Any] = {"homes": [], "devices": [], "guests": [], "guest_users": [], "lock": Lock()}

def _rid(prefix: str, k: int = 8) -> str:
    return f"{prefix}_{''.join(random.choices(string.ascii_lowercase + string.digits, k=k))}"

@dataclass
class Profile:
    kind: str
    homes:  List[str] = field(default_factory=list)
    devices: List[str] = field(default_factory=list)
    hw_ids:  List[str] = field(default_factory=list)
    guests:  List[str] = field(default_factory=list)
    real_guests: List[str] = field(default_factory=list)
    setup:   bool = False

class ApolloUser(HttpUser):
    wait_time = WAIT_TIME

    # -- life-cycle
    def on_start(self) -> None:
        self._select_profile()
        self._auth()
        self.setup_ready_at = time.time() + random.uniform(5, 30)

    def on_stop(self) -> None:
        if getattr(self, "mqtt_clients", None):
            for c in self.mqtt_clients:
                c.loop_stop(); c.disconnect()

    # -- tasks
    @task(40)
    def notifications(self) -> None:
        if not self.profile.setup:
            self._maybe_finish_setup();  return
        
        # Check endpoint health before making request
        if health_monitor.should_skip_endpoint("NOTIF_LIST"):
            return
        
        health_factor = health_monitor.get_endpoint_health_factor("NOTIF_LIST")
        if random.random() > health_factor:
            return
        
        self.client.get("/api/notification/user", name="NOTIF_LIST", headers=self.h)

    @task(20)
    def home_devices(self) -> None:
        if not self.profile.setup:
            self._maybe_finish_setup();  return
        if not self.profile.homes: return
        
        if health_monitor.should_skip_endpoint("HOME_DEVICES"):
            return
        
        # Apply health factor
        health_factor = health_monitor.get_endpoint_health_factor("HOME_DEVICES")
        if random.random() > health_factor:
            return
        
        home = random.choice(self.profile.homes)
        self.client.get(f"/api/home/{home}/devices", name="HOME_DEVICES", headers=self.h)

    @task(5)
    def remote_unlock(self) -> None:
        if not self.profile.setup:
            self._maybe_finish_setup();  return
        if not self.profile.devices: return
        
        if health_monitor.should_skip_endpoint("DEV_UNLOCK"):
            return
        
        health_factor = health_monitor.get_endpoint_health_factor("DEV_UNLOCK")
        if random.random() > health_factor:
            return
        
        dev = random.choice(self.profile.devices)
        self.client.post(f"/api/devices/{dev}/unlock", name="DEV_UNLOCK", headers=self.h)

    @task(15)
    def guest_management(self) -> None:
        if not self.profile.setup:
            self._maybe_finish_setup();  return
        if not self.profile.homes: return
        
        home = random.choice(self.profile.homes)
        
        operation = random.choice(["list_guests", "list_admins", "update_guest_rights"])
        
        endpoint_name = {"list_guests": "HOME_GUESTS", "list_admins": "HOME_ADMINS", "update_guest_rights": "GUEST_UPDATE_RIGHTS"}[operation]
        if health_monitor.should_skip_endpoint(endpoint_name):
            return
        
        health_factor = health_monitor.get_endpoint_health_factor(endpoint_name)
        if random.random() > health_factor:
            return
        
        if operation == "list_guests":
            self.client.get(f"/api/home/{home}/guests", name="HOME_GUESTS", headers=self.h)
        elif operation == "list_admins":
            self.client.get(f"/api/home/{home}/admins", name="HOME_ADMINS", headers=self.h)
        elif operation == "update_guest_rights" and self.profile.real_guests and self.profile.devices:
            guest_uuid = random.choice(self.profile.real_guests)
            
            try:
                with self.client.get(f"/api/home/{home}/devices", headers=self.h, 
                                   name="HOME_DEVICES_FOR_UPDATE", catch_response=True) as resp:
                    if resp.status_code == 200:
                        home_devices = resp.json()
                        resp.success()
                        if not home_devices:
                            log.debug("Home %s has no devices, skipping guest rights update", home)
                            return
                    else:
                        resp.failure(f"Failed to get home devices for update: {resp.text}")
                        return
            except Exception as e:
                log.error("Error getting home devices for guest update: %s", e)
                return
            
            available_devices = home_devices[:2]
            device_rights = []
            
            for device in available_devices:
                device_id = device.get('uuid') or device.get('id')
                if device_id:
                    selected_rights = random.sample(DEVICE_ACCESS_RIGHTS, random.randint(1, min(3, len(DEVICE_ACCESS_RIGHTS))))
                    device_rights.append({
                        "deviceId": device_id,
                        "rights": selected_rights
                    })
            
            if device_rights:
                self.client.put(
                    f"/api/home/{home}/guests/{guest_uuid}/devices",
                    headers=self.h,
                    json=device_rights,
                    name="GUEST_UPDATE_RIGHTS"
                )

    def _select_profile(self) -> None:
        wheel = random.random()
        self.profile = (
            Profile("new")      if wheel < 0.20 else
            Profile("light")    if wheel < 0.60 else
            Profile("heavy")    if wheel < 0.90 else
            Profile("observer")
        )
        self.username = _rid("user"); self.password = "Locust1!"; self.h: Dict[str, str] = {}
        self.mqtt_clients: List[Any] = []

    def _auth(self) -> None:
        reg_payload = {
            "username": self.username,
            "email": f"{self.username}@example.com",
            "password": self.password
        }
        with self.client.post("/api/auth/register", json=reg_payload, name="AUTH_REG", catch_response=True) as resp:
            if resp.status_code == 200:
                resp.success()
            else:
                resp.failure(f"Registration failed: {resp.text}")
                return

        login_payload = {
            "username": self.username,
            "password": self.password
        }
        with self.client.post("/api/auth/login", json=login_payload, name="AUTH_LOGIN", catch_response=True) as resp:
            if resp.status_code == 200:
                try:
                    token = resp.json().get('token') or resp.json().get('accessToken')
                    if token:
                        self.h = {"Authorization": f"Bearer {token}"}
                        resp.success()
                    else:
                        resp.failure("No token in login response")
                except Exception as e:
                    resp.failure(f"Login response parse error: {e}")
            else:
                resp.failure(f"Login failed: {resp.text}")

    def _maybe_finish_setup(self) -> None:
        if self.profile.setup or time.time() < self.setup_ready_at: return
        if self.profile.kind == "observer":
            with POOL["lock"]:
                self.profile.homes   = POOL["homes"][:MAX_HOMES]
                self.profile.devices = POOL["devices"][:MAX_DEVICES]
                self.profile.guests  = POOL["guests"][:MAX_DEVICES]
                self.profile.real_guests = []
        else:
            self._create_homes(1 if self.profile.kind != "heavy" else 2)
            if random.random() < MQTT_PROB:
                self._create_devices(2 if self.profile.kind == "light" else 6)
                if self.profile.devices and random.random() < 0.6:
                    self._create_guest_users(2 if self.profile.kind == "light" else 3)
                    self._create_guests(1 if self.profile.kind == "light" else 2)
        self.profile.setup = True

    def _create_homes(self, how_many: int) -> None:
        for _ in range(min(how_many, MAX_HOMES)):
            if health_monitor.should_skip_endpoint("HOME_CREATE"):
                log.info("Skipping home creation due to endpoint health")
                continue
            
            r = self.client.post(
                "/api/home",
                params={"name": _rid("Home"), "address": "123 Test St"},
                headers=self.h, name="HOME_CREATE")
            uuid = r.json().get("uuid")
            if uuid:
                self.profile.homes.append(uuid)
                with POOL["lock"]: POOL["homes"].append(uuid)

    def _create_devices(self, how_many: int) -> None:
        for _ in range(min(how_many, MAX_DEVICES)):
            hw = _rid("HW")
            self.profile.hw_ids.append(hw)

            mqtt_client = self._create_mqtt_client(hw)
            if mqtt_client:
                self.mqtt_clients.append(mqtt_client)

                time.sleep(random.uniform(1, 3))
                device_uuid = self._register_device_via_mqtt(hw, mqtt_client)

                if device_uuid and self.profile.homes:
                    home_uuid = random.choice(self.profile.homes)
                    if self._link_device_to_home(device_uuid, hw, home_uuid):
                        self.profile.devices.append(device_uuid)
                        with POOL["lock"]:
                            POOL["devices"].append(device_uuid)

                time.sleep(random.uniform(2, 6))

    def _create_mqtt_client(self, hardware_id: str) -> Optional[Any]:
        if mqtt is None:
            log.warning("MQTT not available - skipping device setup for %s", hardware_id)
            return None

        try:
            mqtt_client = mqtt.Client(
                client_id=hardware_id,
                protocol=mqtt.MQTTv311,
                userdata=None,
                transport="tcp",
                callback_api_version=mqtt.CallbackAPIVersion.VERSION2,
            )

            import threading
            conn_event = threading.Event()

            def _on_connect(client, userdata, flags, reason_code, properties):
                rc_val = getattr(reason_code, "value", reason_code)
                if rc_val == 0:
                    conn_event.set()
                else:
                    log.error("MQTT connection failed reason_code=%s for %s", reason_code, hardware_id)

            mqtt_client.on_connect = _on_connect
            mqtt_client.on_disconnect = (
                lambda client, userdata, disconnect_flags, reason_code, properties: log.warning(
                    "MQTT disconnected reason_code=%s id=%s", reason_code, hardware_id
                )
            )

            mqtt_host = os.getenv("MQTT_HOST", "localhost")
            mqtt_port = int(os.getenv("MQTT_PORT", "1883"))

            mqtt_client.connect_async(mqtt_host, mqtt_port, keepalive=30)
            mqtt_client.loop_start()

            if not conn_event.wait(timeout=5):
                log.error("MQTT connect timeout for %s", hardware_id)
                mqtt_client.loop_stop()
                return None

            return mqtt_client
        except Exception as exc:
            log.error("MQTT connection failed for %s: %s", hardware_id, exc)
            self.environment.events.request.fire(
                request_type="MQTT",
                name="MQTT_CONNECT",
                response_time=0,
                response_length=0,
                response=None,
                context={},
                exception=exc,
            )
            return None

    def _register_device_via_mqtt(self, hardware_id: str, mqtt_client: Any) -> Optional[str]:
        hello_msg = {"hardwareId": hardware_id, "deviceType": "AMB82"}

        start_time = time.time()
        try:
            info = mqtt_client.publish("devices/hello", json.dumps(hello_msg), qos=0)
            info.wait_for_publish(timeout=5)

            self.environment.events.request.fire(
                request_type="MQTT",
                name="MQTT_HELLO",
                response_time=(time.time() - start_time) * 1000,
                response_length=0,
                response=None,
                context={},
                exception=None,
            )
        except Exception as exc:
            self.environment.events.request.fire(
                request_type="MQTT",
                name="MQTT_HELLO",
                response_time=(time.time() - start_time) * 1000,
                response_length=0,
                response=None,
                context={},
                exception=exc,
            )
            return None

        backoff = 0.5
        max_wait = 15
        total_waited = 0.0

        while total_waited < max_wait:
            with self.client.get(
                    f"/api/devices/hardware/{hardware_id}/uuid",
                    headers=self.h,
                    name="DEVICE_UUID_LOOKUP",
                    catch_response=True,
            ) as res:
                if res.status_code == 200 and res.text and res.text != "null":
                    device_uuid = res.text.strip().strip('"')
                    res.success()
                    return device_uuid
                else:
                    res.success()

            time.sleep(backoff)
            total_waited += backoff
            backoff = min(backoff * 1.5, 4)

        self.environment.events.request.fire(
            request_type="SETUP",
            name="DEVICE_REGISTRATION",
            response_time=total_waited * 1000,
            response_length=0,
            response=None,
            context={},
            exception=RuntimeError(f"Device {hardware_id} registration timeout after {max_wait}s"),
        )
        return None

    def _link_device_to_home(self, device_uuid: str, hardware_id: str, home_uuid: str) -> bool:
        body = {
            "name": _rid("Device"),
            "deviceType": random.choice(["DOORLOCK", "CAMERA", "SENSOR"]),
            "description": f"Load test device {hardware_id}",
            "hardwareId": hardware_id,
        }

        with self.client.post(
                f"/api/home/{home_uuid}/devices",
                headers=self.h,
                json=body,
                name="HOME_LINK_DEVICE",
                catch_response=True,
        ) as res:
            if res.status_code == 200:
                res.success()
                return True
            else:
                res.failure(f"Link device failed: {res.text}")
                return False

    def _create_guests(self, how_many: int) -> None:
        if not self.profile.homes or not self.profile.devices:
            log.warning("Cannot create guests - missing homes=%d devices=%d", 
                       len(self.profile.homes), len(self.profile.devices))
            return
        
        with POOL["lock"]:
            available_guest_users = POOL["guest_users"].copy()
        
        if not available_guest_users:
            log.warning("No guest users available for guest creation")
            return
            
        for i in range(min(how_many, MAX_DEVICES, len(available_guest_users))):
            home_uuid = random.choice(self.profile.homes)
            guest_email = available_guest_users[i % len(available_guest_users)]
            
            try:
                with self.client.get(f"/api/home/{home_uuid}/devices", headers=self.h, 
                                   name="HOME_DEVICES_FOR_GUEST", catch_response=True) as resp:
                    if resp.status_code == 200:
                        home_devices = resp.json()
                        resp.success()
                        if not home_devices:
                            log.warning("Home %s has no devices, skipping guest creation", home_uuid)
                            continue
                    else:
                        resp.failure(f"Failed to get home devices: {resp.text}")
                        continue
            except Exception as e:
                log.error("Error getting home devices for guest creation: %s", e)
                continue
            
            available_devices = home_devices[:3]
            device_rights = []
            
            for device in available_devices:
                device_id = device.get('uuid') or device.get('id')
                if device_id:
                    selected_rights = random.sample(DEVICE_ACCESS_RIGHTS, random.randint(1, min(4, len(DEVICE_ACCESS_RIGHTS))))
                    device_rights.append({
                        "deviceId": device_id,
                        "rights": selected_rights
                    })
            
            if not device_rights:
                log.warning("No valid device rights generated for home %s, skipping guest", home_uuid)
                continue
            
            # Create guest via API
            guest_payload = {
                "email": guest_email,
                "deviceRights": device_rights
            }
            
            log.info("Creating guest %d/%d for home %s with %d device rights", 
                    i+1, how_many, home_uuid, len(device_rights))
            log.info("Using existing guest user email: %s", guest_email)
            log.debug("Guest payload: %s", guest_payload)
            
            with self.client.post(
                f"/api/home/{home_uuid}/guests",
                headers=self.h,
                json=guest_payload,
                name="GUEST_CREATE",
                catch_response=True,
            ) as res:
                if res.status_code == 200:
                    try:
                        response_text = res.text.strip()
                        if response_text:
                            guest_data = res.json()
                            guest_uuid = guest_data.get("uuid")
                            if guest_uuid:
                                self.profile.guests.append(guest_uuid)
                                self.profile.real_guests.append(guest_uuid)  # Track as real guest
                                with POOL["lock"]:
                                    POOL["guests"].append(guest_uuid)
                                res.success()
                                log.info("Successfully created guest %s for home %s", guest_uuid, home_uuid)
                            else:
                                res.failure("No guest UUID in response")
                                log.error("Guest creation response missing UUID: %s", guest_data)
                        else:
                            res.success()
                            log.info("Guest creation successful (empty response) for home %s with email %s", home_uuid, guest_email)
                            placeholder_uuid = f"guest_{_rid('', 8)}"
                            self.profile.guests.append(placeholder_uuid)
                            with POOL["lock"]:
                                POOL["guests"].append(placeholder_uuid)
                    except ValueError as e:
                        res.success()
                        log.info("Guest creation successful (non-JSON response) for home %s with email %s", home_uuid, guest_email)
                        log.debug("Response text: '%s', Content-Type: %s", res.text, res.headers.get('Content-Type', 'unknown'))
                        placeholder_uuid = f"guest_{_rid('', 8)}"
                        self.profile.guests.append(placeholder_uuid)
                        with POOL["lock"]:
                            POOL["guests"].append(placeholder_uuid)
                    except Exception as e:
                        res.failure(f"Guest creation response parse error: {e}")
                        log.error("Failed to parse guest creation response: %s", e)
                else:
                    res.failure(f"Guest creation failed: {res.text}")
                    log.error("Guest creation failed with status %d for home %s", res.status_code, home_uuid)
                    log.error("Request payload was: %s", guest_payload)
                    log.error("Response body: %s", res.text)
                    log.error("Response headers: %s", dict(res.headers))
            
            time.sleep(random.uniform(1, 3))

    def _create_guest_users(self, how_many: int) -> None:
        """Create user accounts that can later be added as guests."""
        for i in range(how_many):
            # Check endpoint health before creating guest user
            if health_monitor.should_skip_endpoint("GUEST_USER_REG"):
                log.info("Skipping guest user creation due to endpoint health")
                continue
                
            guest_username = _rid("guestuser")
            guest_email = f"{guest_username}@example.com"
            guest_password = "Guest123!"
            
            # Register the guest user
            reg_payload = {
                "username": guest_username,
                "email": guest_email,
                "password": guest_password
            }
            
            with self.client.post("/api/auth/register", json=reg_payload, name="GUEST_USER_REG", catch_response=True) as resp:
                if resp.status_code == 200:
                    resp.success()
                    # Store the guest user email for later use
                    with POOL["lock"]:
                        POOL["guest_users"].append(guest_email)
                    log.info("Created guest user account: %s", guest_email)
                else:
                    resp.failure(f"Guest user registration failed: {resp.text}")
                    log.error("Failed to create guest user %s: %s", guest_email, resp.text)
            
            # Small delay between user creations
            time.sleep(random.uniform(0.5, 1.5))

@events.init.add_listener
def clear_pools(environment, **kw):
    POOL["homes"].clear(); POOL["devices"].clear(); POOL["guests"].clear(); POOL["guest_users"].clear()

@events.test_start.add_listener  
def start_health_monitoring(environment, **kw):
    def report_health_status():
        with health_monitor.lock:
            unhealthy_endpoints = []
            for endpoint, health in health_monitor.endpoints.items():
                if health.circuit_open:
                    unhealthy_endpoints.append(f"{endpoint}(CIRCUIT_OPEN)")
                elif health.failure_rate > 0.3:
                    unhealthy_endpoints.append(f"{endpoint}(FAIL_RATE:{health.failure_rate:.2f})")
                elif health.avg_response_time > 2000:
                    unhealthy_endpoints.append(f"{endpoint}(SLOW:{health.avg_response_time:.0f}ms)")
            
            if unhealthy_endpoints:
                log.info("🔴 Throttled endpoints: %s", ", ".join(unhealthy_endpoints))
            else:
                log.info("🟢 All endpoints healthy")
    
    # Schedule periodic reporting (every 30 seconds)
    import threading
    def periodic_reporter():
        while True:
            time.sleep(30)
            report_health_status()
    
    reporter_thread = threading.Thread(target=periodic_reporter, daemon=True)
    reporter_thread.start()

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
log = logging.getLogger("locustfile")

@events.request.add_listener
def record_endpoint_health(**meta):
    endpoint = meta.get("name", "unknown")
    response_time = meta.get("response_time", 0)
    exception = meta.get("exception")
    response = meta.get("response")
    
    success = True
    if exception:
        success = False
    elif response and hasattr(response, 'status_code') and response.status_code >= 400:
        success = False
    
    health_monitor.record_request(endpoint, response_time, success)

@events.request.add_listener
def log_request(**meta):
    """
    """
    if meta.get("exception"):
        log.error("ERR  | %-8s | %-24s | %.0f ms | %s",
                  meta.get("request_type"), meta.get("name"),
                  meta.get("response_time", 0), meta["exception"])
    elif (r := meta.get("response")) is not None and getattr(r, "status_code", 200) >= 400:
        log.warning("HTTP %s | %-24s | %.0f ms | %s",
                    r.status_code, meta["name"],
                    meta["response_time"], (r.text or "")[:120])
