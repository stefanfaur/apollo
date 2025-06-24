package ro.faur.apollo.device.domain;

import jakarta.persistence.*;
import ro.faur.apollo.shared.domain.BaseEntity;

@Entity
@Table(name = "fingerprint_enroll_status")
public class FingerprintEnrollStatus extends BaseEntity {

    @Column(name = "device_uuid", nullable = false, unique = true)
    private String deviceUuid;

    @Column(name = "template_id")
    private Integer templateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EnrollStatus status;

    @Column(name = "error_code")
    private String errorCode;

    public FingerprintEnrollStatus() {
    }

    public FingerprintEnrollStatus(String deviceUuid, Integer templateId, EnrollStatus status) {
        this.deviceUuid = deviceUuid;
        this.templateId = templateId;
        this.status = status;
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public void setDeviceUuid(String deviceUuid) {
        this.deviceUuid = deviceUuid;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public EnrollStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollStatus status) {
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
} 