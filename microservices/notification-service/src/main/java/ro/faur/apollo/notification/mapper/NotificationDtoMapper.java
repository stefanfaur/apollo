package ro.faur.apollo.notification.mapper;

import org.springframework.stereotype.Component;
import ro.faur.apollo.notification.domain.Notification;
import ro.faur.apollo.notification.dto.NotificationDTO;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationDtoMapper {

    public NotificationDTO toDto(Notification notification) {
        if (notification == null) {
            return null;
        }
        
        return new NotificationDTO(
                notification.getUuid(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getMediaUrl(),
                notification.getCreatedAt(),
                notification.getDeviceUuid()
        );
    }

    public List<NotificationDTO> toDto(List<Notification> notifications) {
        if (notifications == null) {
            return null;
        }
        return notifications.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Notification toEntity(NotificationDTO notificationDTO) {
        if (notificationDTO == null) {
            return null;
        }
        
        Notification notification = new Notification();
        notification.setUuid(notificationDTO.getUuid());
        notification.setTitle(notificationDTO.getTitle());
        notification.setMessage(notificationDTO.getMessage());
        notification.setType(notificationDTO.getType());
        notification.setMediaUrl(notificationDTO.getMediaUrl());
        notification.setCreatedAt(notificationDTO.getCreatedAt());
        notification.setDeviceUuid(notificationDTO.getDeviceUuid());
        
        return notification;
    }

    public List<Notification> toEntity(List<NotificationDTO> notificationDTOs) {
        if (notificationDTOs == null) {
            return null;
        }
        return notificationDTOs.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
} 