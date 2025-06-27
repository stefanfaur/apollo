package ro.faur.apollo.device;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import ro.faur.apollo.shared.config.FeignOkHttpConfig;

@SpringBootApplication
@EnableFeignClients(defaultConfiguration = FeignOkHttpConfig.class)
@EnableAsync
@EntityScan(basePackages = {"ro.faur.apollo.device.domain", "ro.faur.apollo.shared.domain"})
@ComponentScan(basePackages = {
        "ro.faur.apollo.device",
        "ro.faur.apollo.shared.security"
})
public class DeviceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeviceServiceApplication.class, args);
    }
} 