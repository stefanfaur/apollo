# apollo: OSS home security management software

# 1. Component Description

Apollo is a home security management system, consisting of:

* Main components:
  * Mobile application - `safr-mobile/safr`
  * Backend - Java microservices - `microservices`
  * Infrastructure - databases, MQTT, MinIO - `microservices/docker-infra`
* Auxiliary components:
  * Hardware device simulator - `utils/device-simulator/apollo-sim`
  * Testing tools - `utils/apollo-loadtest`
  * Monitoring - `microservices/apollo-k8s/`

# 2. Project Compilation and Execution

## 1. Mobile Application

### 1.1 Installing Dependencies

The mobile app is located in the `safr-mobile/safr` directory. 
To install the required dependencies, make sure you have Node.js and npm installed on your system. Then, run the following commands in your terminal:

```bash
cd safr-mobile/safr
npm install
```

### 1.2 Running the Application

The app can be run on an Android emulator, an iOS simulator, a physical device using Expo Go, or directly in the browser. 
To run the app, use the command:

```bash
npx expo start
```

This opens a terminal menu where you can choose where to run the app.


## 2. Backend

The microservices are configured via environment variables (included at the end of this file).

These can be set in the terminal (using `source .env`) before running the microservices or included in a `.env` file and loaded in each service using the `EnvFile` plugin for IntelliJ.

### 2.1 Installing Dependencies and Compilation

The backend is located in the `microservices` directory and can be built and run either using Java or Docker.

#### 2.1.1 Running with Java

For the database, MQTT, and MinIO, make sure you have Docker and Docker Compose installed.
To start them, run:

```bash
cd microservices/docker-infra
docker-compose up -d
```

Ensure you have Java JDK and Maven installed, then run:

```bash
cd microservices
# Download dependencies and build each microservice
mvn clean install
```

If the project is opened in IntelliJ IDEA, you can run each microservice directly from the IDE, using the "Services" section on the left side of the screen.

To run without IntelliJ, use the following commands:

```bash
# first place the .env file in the microservices directory
source .env
# run each microservice in a detached terminal
java -jar api-gateway/target/api-gateway-*.jar &
java -jar user-service/target/user-service-*.jar &
java -jar device-service/target/device-service-*.jar &
java -jar media-analysis-service/target/media-analysis-service-*.jar &
java -jar home-service/target/home-service-*.jar &
java -jar notification-service/target/notification-service-*.jar &
java -jar file-storage-service/target/file-storage-service-*.jar &
```

To stop all microservices, you can use the `kill` command in the terminal to terminate the Java processes:

```bash
kill $(ps aux | grep '[j]ava' | awk '{print $2}')
```

#### 2.1.2 Running with Docker

To run the microservices using Docker, make sure you have Docker and Docker Compose installed. Then, run the following commands:

```bash
cd microservices
# This command will build all microservices, package them into images, and store them in the local Docker registry.
docker compose build
# This command will start all microservices in the background, along with the infrastructure.
docker compose up -d
```

To stop all microservices, use:

```bash
# In the microservices directory
docker compose down
```

## 3. Testing and Debugging Tools

### 3.1 Swagger UI

Swagger UI is available for all microservices through the API Gateway at `http://localhost:8080/swagger-ui.html`. It provides an interface to explore and test all microservice APIs.

### 3.2 Hardware Device Simulator

To simulate hardware devices, you can use the simulator available in the `utils/device-simulator/apollo-sim` directory. It allows the simulation of IoT devices and testing interactions with the backend.
This is a React project that can be run with:

```bash
cd utils/device-simulator/apollo-sim
npm install
npm start
```

### 3.3 Load Testing with Locust

To run load tests, execute the following commands:

```bash
cd utils/apollo-loadtest
locust -f locustfile.py --host=http://localhost:8080
```

Then, from the web interface, configure the number of users and spawn rate, and start the test.

### 3.4 Monitoring with Grafana

The monitoring stack is available only in Kubernetes/K3s.

Grafana can be accessed publicly at https://grafana.faur.sh with user `admin` and password `apollo123`.
Data can be explored in the DrillDown section or in Dashboards. Note that the deployment resources are limited, so response times may be high, especially during load testing.

If you have `k3d` installed, you can run the following commands to start a local K3s cluster with monitoring:

```bash
cd microservices/apollo-k8s
sudo ./scripts/deploy.sh
```

## 4. The `.env` File

The evaluation committee in the submitted README has access to a `.env` file with the environment variables required to run the microservices and test the complete functionality.

Another user can create a `.env` file in the `microservices` directory using the following template:

```bash
cd microservices
cp env.template .env
# fill in the variables in the .env file after copying
# then set the environment variables in the terminal
./scripts/setup-local-env.sh
# or use IntelliJ with the EnvFile plugin to load the variables
# or
source .env
```
