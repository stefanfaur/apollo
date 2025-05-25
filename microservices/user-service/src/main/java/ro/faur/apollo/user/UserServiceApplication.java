package ro.faur.apollo.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@EntityScan(basePackages = {"ro.faur.apollo.user.domain", "ro.faur.apollo.shared.domain"})
@ComponentScan(basePackages = {
    "ro.faur.apollo.user",
    "ro.faur.apollo.shared.security"
})
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
} 