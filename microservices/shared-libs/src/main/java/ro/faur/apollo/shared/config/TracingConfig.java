package ro.faur.apollo.shared.config;

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for distributed tracing with OpenTelemetry and Tempo
 */
@Configuration
public class TracingConfig {

    @Value("${management.otlp.tracing.endpoint:http://tempo-tempo.monitoring.svc.cluster.local:4318}")
    private String otlpEndpoint;

    @Value("${otel.exporter.otlp.protocol:http/protobuf}")
    private String otlpProtocol;

    @Bean
    @ConditionalOnProperty(value = "management.tracing.enabled", havingValue = "true", matchIfMissing = true)
    public SpanExporter otlpSpanExporter() {
        // Use HTTP by default since it's more compatible with Spring Boot
        if ("grpc".equalsIgnoreCase(otlpProtocol)) {
            return OtlpGrpcSpanExporter.builder()
                    .setEndpoint(otlpEndpoint)
                    .build();
        } else {
            // Default to HTTP protocol (http/protobuf)
            return OtlpHttpSpanExporter.builder()
                    .setEndpoint(otlpEndpoint)
                    .build();
        }
    }
} 