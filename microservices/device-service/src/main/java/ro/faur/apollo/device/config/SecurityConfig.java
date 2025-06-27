package ro.faur.apollo.device.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ro.faur.apollo.shared.security.BaseSecurityConfig;
import ro.faur.apollo.shared.security.JwtAuthenticationFilter;
import ro.faur.apollo.shared.security.JwtTokenProvider;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends BaseSecurityConfig {

    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        super(jwtTokenProvider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/actuator/**").permitAll() // Health checks
                        .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // OpenAPI docs
                        .requestMatchers("/api/devices/register").permitAll() // Device registration from MQTT
                        .requestMatchers("/api/devices/hardware/*/uuid").permitAll() // Device UUID lookup from MQTT
                        .requestMatchers("/api/devices/*/fingerprint/enroll/status").permitAll() // Enroll status updates from MQTT via Notification Service
                        .anyRequest().authenticated() // All other require authentication
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }
} 