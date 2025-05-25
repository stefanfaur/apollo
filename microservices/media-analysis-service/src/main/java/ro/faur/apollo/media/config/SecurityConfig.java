package ro.faur.apollo.media.config;

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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/actuator/**").permitAll() // Health checks
                        .requestMatchers("/api/media/analyze").permitAll() // Allow inter-service communication
                        .anyRequest().authenticated() // All other require authentication
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }
} 