package ro.faur.apollo.shared.logging;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to add request logging and correlation IDs for better observability
 */
@Component
@Order(1)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);
    
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String USER_ID_HEADER = "X-User-ID";

    @Autowired(required = false)
    private Tracer tracer;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Set up correlation tracking
            setupCorrelationTracking(request, response);
            
            // Log incoming request
            logIncomingRequest(request);
            
            // Continue with the filter chain
            filterChain.doFilter(request, response);
            
        } finally {
            // Log outgoing response
            logOutgoingResponse(request, response, startTime);
            
            // Clean up MDC
            clearMDC();
        }
    }

    private void setupCorrelationTracking(HttpServletRequest request, HttpServletResponse response) {
        // Get or generate correlation ID
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.trim().isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        // Get or generate request ID
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.trim().isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }
        
        // Get user ID if available
        String userId = request.getHeader(USER_ID_HEADER);
        
        // Extract trace and span IDs from tracing context
        if (tracer != null) {
            Span currentSpan = tracer.currentSpan();
            if (currentSpan != null) {
                String traceId = currentSpan.context().traceId();
                String spanId = currentSpan.context().spanId();
                MDC.put("traceId", traceId);
                MDC.put("spanId", spanId);
            }
        }
        
        // Add to MDC for logging
        MDC.put("correlationId", correlationId);
        MDC.put("requestId", requestId);
        MDC.put("method", request.getMethod());
        MDC.put("uri", request.getRequestURI());
        
        if (userId != null && !userId.trim().isEmpty()) {
            MDC.put("userId", userId);
        }
        
        // Add to response headers
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
    }

    private void logIncomingRequest(HttpServletRequest request) {
        if (shouldLogRequest(request)) {
            logger.info("Incoming request: {} {} from {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    getClientIpAddress(request));
        }
    }

    private void logOutgoingResponse(HttpServletRequest request, HttpServletResponse response, long startTime) {
        if (shouldLogRequest(request)) {
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("status", String.valueOf(response.getStatus()));
            MDC.put("duration", String.valueOf(duration));
            
            logger.info("Outgoing response: {} {} - Status: {} - Duration: {}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration);
        }
    }

    private boolean shouldLogRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // Skip logging for health checks and static resources
        return !uri.startsWith("/actuator/health") &&
               !uri.startsWith("/actuator/prometheus") &&
               !uri.startsWith("/favicon.ico") &&
               !uri.startsWith("/static/") &&
               !uri.startsWith("/css/") &&
               !uri.startsWith("/js/") &&
               !uri.startsWith("/images/");
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private void clearMDC() {
        MDC.remove("correlationId");
        MDC.remove("requestId");
        MDC.remove("userId");
        MDC.remove("method");
        MDC.remove("uri");
        MDC.remove("status");
        MDC.remove("duration");
        MDC.remove("traceId");
        MDC.remove("spanId");
    }
} 