package ro.faur.apollo.device.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.faur.apollo.device.domain.EnrollStatus;
import ro.faur.apollo.device.domain.FingerprintEnrollStatus;
import ro.faur.apollo.device.repository.DeviceRepository;
import ro.faur.apollo.device.repository.FingerprintEnrollStatusRepository;
import ro.faur.apollo.device.service.feign.NotificationServiceClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class FingerprintEnrollService {

    private static final Logger logger = LoggerFactory.getLogger(FingerprintEnrollService.class);

    private final FingerprintEnrollStatusRepository statusRepository;
    private final DeviceRepository deviceRepository;
    private final ObjectMapper objectMapper;
    private final NotificationServiceClient notificationServiceClient;

    public FingerprintEnrollService(FingerprintEnrollStatusRepository statusRepository,
                                    DeviceRepository deviceRepository,
                                    ObjectMapper objectMapper,
                                    NotificationServiceClient notificationServiceClient) {
        this.statusRepository = statusRepository;
        this.deviceRepository = deviceRepository;
        this.objectMapper = objectMapper;
        this.notificationServiceClient = notificationServiceClient;
    }

    public void startEnrollment(String deviceUuid, int templateId, String hardwareId) {
        // Use atomic upsert to prevent race conditions
        statusRepository.upsertStatus(deviceUuid, templateId, EnrollStatus.PENDING.name(), null);

        // Delegate MQTT publish to Notification Service
        Map<String, Object> payload = new HashMap<>();
        payload.put("hardwareId", hardwareId);
        payload.put("userFpId", templateId);
        try {
            notificationServiceClient.startFingerprintEnroll(payload);
        } catch (Exception e) {
            logger.error("Failed to delegate enroll start to Notification Service", e);
        }
    }

    public EnrollStatus getStatus(String deviceUuid) {
        FingerprintEnrollStatus status = statusRepository.findByDeviceUuid(deviceUuid);
        return status != null ? status.getStatus() : null;
    }

    public void updateStatus(String deviceUuid, EnrollStatus statusEnum, String errorCode) {
        // Use atomic upsert to prevent race conditions
        statusRepository.upsertStatus(deviceUuid, null, statusEnum.name(), errorCode);
    }
} 