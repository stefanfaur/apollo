package ro.faur.apollo.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.faur.apollo.notification.service.MqttService;

import java.util.Map;

@RestController
@RequestMapping("/internal/mqtt/fingerprint/enroll")
public class EnrollController {

    private final MqttService mqttService;

    public EnrollController(MqttService mqttService) {
        this.mqttService = mqttService;
    }

    @PostMapping("/start")
    public ResponseEntity<?> startEnroll(@RequestBody Map<String, Object> body) {
        String hardwareId = (String) body.get("hardwareId");
        Integer userFpId = (Integer) body.get("userFpId");
        if (hardwareId == null || userFpId == null) {
            return ResponseEntity.badRequest().body("hardwareId and userFpId required");
        }
        mqttService.publishEnrollStart(hardwareId, userFpId);
        return ResponseEntity.accepted().build();
    }
} 