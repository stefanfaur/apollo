package ro.faur.apollo.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import ro.faur.apollo.shared.config.FeignOkHttpConfig;

@SpringBootApplication
@EnableFeignClients(defaultConfiguration = FeignOkHttpConfig.class)
@EntityScan(basePackages = {"ro.faur.apollo.notification.domain", "ro.faur.apollo.shared.domain"})
@ComponentScan(basePackages = {
        "ro.faur.apollo.notification",
        "ro.faur.apollo.shared.security"
})
@EnableCaching
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
} 