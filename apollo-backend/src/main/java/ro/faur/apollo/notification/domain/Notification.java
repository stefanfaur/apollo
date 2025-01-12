package ro.faur.apollo.notification.domain;

import jakarta.persistence.*;
import ro.faur.apollo.device.domain.Device;
import ro.faur.apollo.libs.persistence.domain.BaseEntity;
import ro.faur.apollo.notification.domain.types.NotificationEventType;

@Entity
public class Notification extends BaseEntity {

        @Column(nullable = false)
        private String title;

        @Column(nullable = false)
        private String message;

        @Enumerated(value = EnumType.STRING)
        NotificationEventType type;

        @Column(name = "image_url")
        private String imageUrl;


    /**
     * The device that emitted the notification,
     * will be assigned using hwid of the device
     * included in the MQTT payload
     */
    @ManyToOne
        private Device emitter;

        public Notification(String title, String message) {
            this.title = title;
            this.message = message;
        }

        public Notification() {
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
}
