### apollo-door 

- Software app side:
    - Mobile app using React Native and Expo 
    - Backend using Spring Boot
    - Google Social Login
    - Database PostgreSQL
    - Docker for backend and database deployment(Postgres and Redit)
      - Also used for MQTT and Prometheus
    - CI/CD using Github Actions(will do later)

- Hardware side:
    - STM32H750VBT6 microcontroller board (with high res camera)
        - still investigating to see if this is doable in reasonable time or if something simpler should be used
    - ESP32 microcontroller board (for wifi communication)
    - 3D printed case for the hardware(later maybe CNC milled)
    - Custom PCB to fit it all together
    - Custom power supply for the hardware
        - battery powered/mains powered
