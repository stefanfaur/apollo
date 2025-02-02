package ro.faur.apollo.notification.domain.dtos;

import org.springframework.stereotype.Component;
import ro.faur.apollo.device.domain.dtos.DeviceDtoMapper;
import ro.faur.apollo.notification.domain.Notification;

@Component
public class NotificationDtoMapper {

    private final DeviceDtoMapper deviceDtoMapper;

    public NotificationDtoMapper(DeviceDtoMapper deviceDtoMapper) {
        this.deviceDtoMapper = deviceDtoMapper;
    }

    public NotificationDTO toDto(Notification notification) {
        return new NotificationDTO(
                notification.getUuid(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getMediaUrl(),
                notification.getCreatedAt(),
                deviceDtoMapper.toDto(notification.getEmitter())
        );
    }

    public Notification toEntity(NotificationDTO notificationDTO) {
        return new Notification(
                notificationDTO.getTitle(),
                notificationDTO.getMessage(),
                notificationDTO.getType(),
                notificationDTO.getMediaUrl(),
                deviceDtoMapper.toEntity(notificationDTO.getEmitter())
        );
    }
}
