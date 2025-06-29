# APOLLO: SISTEM DE GESTIONARE A SECURITĂȚII LOCUINȚEI

Codul sursă este disponibil pe GitHub https://github.com/stefanfaur/apollo.

# 1. Descrierea Componentelor
Apollo este un sistem de gestionare a securității locuinței, format din:
* Componente principale:
  * Aplicație mobilă - `safr-mobile/safr`
  * Backend - microservicii Java - `microservices`
  * Infrastructură - baze de date, MQTT, MinIO - `microservices/docker-infra`
* Componente auxiliare:
  * Simulator de dispozitive hardware - `utils/device-simulator/apollo-sim`
  * Unelte de testare - `utils/apollo-loadtest`
  * Monitorizare - `microservices/apollo-k8s/`

# 2. Compilarea și rularea proiectului

## 1. Aplicația Mobilă

### 1.1 Instalare dependințe

Aplicația mobilă se află în directorul `safr-mobile/safr`. 
Pentru a instala dependențele necesare, asigurați-vă că aveți Node.js și npm instalate pe sistemul dumneavoastră. Apoi, rulați următoarele comenzi în terminal:

```bash

cd safr-mobile/safr
npm install
```

### 1.2 Rularea aplicației

Aplicația poate fi rulată pe un emulator Android, un simulator iOS, pe un dispozitiv fizic prin intermediul Expo Go sau direct în browser. 
Pentru a rula aplicația, utilizați comanda:

```bash

npx expo start
```

Aceasta deschide un meniu în terminal în care puteți alege unde rulați aplicația.


## 2. Backend-ul

Microserviciile se configurează prin variabile de mediu(incluse la sfârșitul acestui fișier).

Acestea pot fi setate în terminal (prin `source .env`) înainte de a rula microserviciile sau pot fi incluse într-un fișier `.env` și încărcate în fiecare serviciu folosind pluginul `EnvFile` pentru IntelliJ.

### 2.1 Instalare dependințe și compilare

Backend-ul se află în directorul `microservices` și poate fi compilat și rulat fie folosind Java, fie folosind Docker.

#### 2.1.1 Rularea cu Java

Pentru baza de date, MQTT și MinIO, asigurați-vă că aveți Docker și Docker Compose instalate.
Pentru a le porni rulați:

```bash

cd microservices/docker-infra
docker-compose up -d
```
Asigurați-vă că aveți Java JDK și Maven instalat, apoi rulați:

```bash

cd microservices
# Descarcă dependențele și compilează fiecare microserviciu
mvn clean install
```

Dacă se deschide proiectul în IntelliJ IDEA, puteți rula fiecare microserviciu direct din IDE, folosind sectiunea "Services" din partea stângă a ecranului.

Pentru a rula fără IntelliJ, utilizați comenzile:

```bash

# prima data puneți fișierul .env în directorul microservices
source .env
# rulați fiecare microserviciu în terminal, detașat
java -jar api-gateway/target/api-gateway-*.jar &
java -jar user-service/target/user-service-*.jar &
java -jar device-service/target/device-service-*.jar &
java -jar media-analysis-service/target/media-analysis-service-*.jar &
java -jar home-service/target/home-service-*.jar &
java -jar notification-service/target/notification-service-*.jar &
java -jar file-storage-service/target/file-storage-service-*.jar &
```

Pentru a opri toate microserviciile, puteți utiliza comanda `kill` în terminal pentru a termina procesele Java:
```bash

kill $(ps aux | grep '[j]ava' | awk '{print $2}')
```

#### 2.1.2 Rularea cu Docker

Pentru a rula microserviciile folosind Docker, asigurați-vă că aveți Docker și Docker Compose instalate. Apoi rulați următoarele comenzi:

```bash
cd microservices
# Această comandă va construi toate microserviciile, le va împacheta în imagini și le va stoca în registrul local Docker.
docker compose build
# Această comandă va porni toate microserviciile în fundal, împreună cu infrastructura.
docker compose up -d
```

Pentru a opri toate microserviciile, utilizați:

```bash

# În directorul microservices
docker compose down
```

## 3. Uneltele de testare/depanare

### 3.1 Swagger UI
Swagger UI este disponibil pentru toate microserviciile prin intermediul API Gateway, la adresa `http://localhost:8080/swagger-ui.html`. Acesta oferă o interfață pentru a explora și testa toate API-urile microserviciilor.

### 3.2 Simulatorul dispozitivelor hardware

Pentru a simula dispozitivele hardware, puteți utiliza simulatorul disponibil în directorul `utils/device-simulator/apollo-sim`. Acesta permite simularea dispozitivelor IoT și testarea interacțiunilor cu backend-ul.
Acesta este un proiect React care poate fi rulat cu:

```bash
cd utils/device-simulator/apollo-sim
npm install
npm start
```

### 3.3 Load Testing cu Locust

Pentru a rula teste de încarcare, rulați următoarele comenzi:

```bash

cd utils/apollo-loadtest
locust -f locustfile.py --host=http://localhost:8080
```
Apoi din interfața web, configurați numărul de utilizatori și rata de creștere, apoi porniți testul.

### 3.4 Monitorizare cu Grafana

Stack-ul de monitorizare este disponibil doar în Kubernetes/K3s.

Se poate accesa Grafana public la adresa https://grafana.faur.sh cu utilizatorul `admin` și parola `apollo123`.
Datele se pot expora în secțiunea DrillDown sau in Dashboard-uri. De menționat că resursele deployment-ului respectiv nu sunt ridicate, așa că timpii de răspuns pot fi mari, mai ales la load test.

Dacă aveți `k3d` instalat, puteti rula următoarele comenzi pentru a porni un cluster K3s local cu monitorizare:

```bash

cd microservices/apollo-k8s
sudo ./scripts/deploy.sh
```

## 4. Fisierul `.env`

Comisia de evaluare în README-ul trimis are la dispozitie un fișier `.env` cu variabilele de mediu necesare pentru rularea microserviciilor și testarea funcționalităților complete.

Un alt utilizator poate crea un fișier `.env` în directorul `microservices`, utilizând următorul șablon:


```bash

cd microservices
cp env.template .env
# se completează variabilele din fișierul .env după copiere
#apoi se setează variabilele de mediu în terminal
./scripts/setup-local-env.sh
# sau se poate folosi IntelliJ cu pluginul EnvFile pentru a încărca variabilele
# sau
source .env
```