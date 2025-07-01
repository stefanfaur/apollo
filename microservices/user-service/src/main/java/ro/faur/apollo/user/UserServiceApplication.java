package ro.faur.apollo.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@EntityScan(basePackages = {"ro.faur.apollo.user.domain", "ro.faur.apollo.shared.domain"})
@ComponentScan(basePackages = {
    "ro.faur.apollo.user",
    "ro.faur.apollo.shared.security"
})
public class UserServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(UserServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner configurationChecker(
            @Value("${google.ios.client-id:#{null}}") String googleIosClientId,
            @Value("${jwt.secret:#{null}}") String jwtSecret,
            @Value("${spring.datasource.url:#{null}}") String datasourceUrl) {
        
        return args -> {
            log.info("=== User Service Configuration Check ===");
            log.info("Google iOS Client ID configured: {}", googleIosClientId != null ? "✓ Present" : "✗ MISSING");
            log.info("JWT Secret configured: {}", jwtSecret != null ? "✓ Present" : "✗ MISSING");
            log.info("Database URL configured: {}", datasourceUrl != null ? "✓ Present" : "✗ MISSING");
            
            if (googleIosClientId == null) {
                log.error("CRITICAL: google.ios.client-id is not configured! Google OAuth will not work!");
            }
            if (jwtSecret == null) {
                log.error("CRITICAL: jwt.secret is not configured! JWT token generation will fail!");
            }
            if (datasourceUrl == null) {
                log.error("CRITICAL: spring.datasource.url is not configured! Database connection will fail!");
            }
            
            log.info("=== Configuration Check Complete ===");
        };
    }
} 