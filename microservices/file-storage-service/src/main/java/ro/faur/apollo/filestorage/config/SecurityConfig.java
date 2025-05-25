package ro.faur.apollo.filestorage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import ro.faur.apollo.shared.security.BaseSecurityConfig;
import ro.faur.apollo.shared.security.JwtTokenProvider;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends BaseSecurityConfig {

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        super(jwtTokenProvider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return createSecurityFilterChain(http);
    }
} 