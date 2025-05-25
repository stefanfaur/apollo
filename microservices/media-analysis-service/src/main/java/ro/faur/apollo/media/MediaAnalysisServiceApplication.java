package ro.faur.apollo.media;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
@EnableFeignClients
@ComponentScan(basePackages = {
        "ro.faur.apollo.media",
        "ro.faur.apollo.shared.security"
})
public class MediaAnalysisServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediaAnalysisServiceApplication.class, args);
    }
} 