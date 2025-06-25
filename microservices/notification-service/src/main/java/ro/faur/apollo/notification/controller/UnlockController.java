package ro.faur.apollo.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.faur.apollo.notification.service.MqttService;

import java.util.Map;

@RestController
@RequestMapping("/internal/mqtt")
public class UnlockController {

    private final MqttService mqttService;

    public UnlockController(MqttService mqttService) {
        this.mqttService = mqttService;
    }

    @PostMapping("/unlock")
    public ResponseEntity<?> unlock(@RequestBody Map<String, Object> body) {
        String hardwareId = (String) body.get("hardwareId");
        if (hardwareId == null) {
            return ResponseEntity.badRequest().body("hardwareId required");
        }
        mqttService.publishUnlock(hardwareId);
        return ResponseEntity.accepted().build();
    }
} 