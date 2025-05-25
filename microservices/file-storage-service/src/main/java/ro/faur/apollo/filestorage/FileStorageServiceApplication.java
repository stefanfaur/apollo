package ro.faur.apollo.filestorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@EnableFeignClients
@EntityScan(basePackages = {"ro.faur.apollo.filestorage.domain", "ro.faur.apollo.shared.domain"})
@ComponentScan(basePackages = {
        "ro.faur.apollo.filestorage",
        "ro.faur.apollo.shared.security"
})
public class FileStorageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileStorageServiceApplication.class, args);
    }
} 