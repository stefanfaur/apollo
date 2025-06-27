package ro.faur.apollo.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.faur.apollo.shared.exception.DeviceException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Used to handle inter-service communication errors.
 * Configured as a @Bean in FeignConfiguration.
 */
public class FeignErrorDecoder implements ErrorDecoder {

    private static final Logger logger = LoggerFactory.getLogger(FeignErrorDecoder.class);
    private final ErrorDecoder defaultErrorDecoder = new Default();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        String responseBody = extractResponseBody(response);
        
        logger.error("Feign error - Method: {}, Status: {}, Response: {}", 
                    methodKey, response.status(), responseBody);
        
        switch (response.status()) {
            case 400:
                return new IllegalArgumentException("Bad request: " + methodKey + " - " + responseBody);
            case 401:
                return new SecurityException("Unauthorized: " + methodKey);
            case 403:
                return new SecurityException("Forbidden: " + methodKey);
            case 404:
                if (isDeviceServiceError(responseBody, "DEVICE_NOT_REGISTERED")) {
                    return new DeviceException.DeviceNotRegisteredException(extractErrorMessage(responseBody));
                }
                return new IllegalArgumentException("Not found: " + methodKey + " - " + responseBody);
            case 409:
                if (isDeviceServiceError(responseBody, "DEVICE_ALREADY_LINKED")) {
                    return new DeviceException.DeviceAlreadyLinkedException(extractErrorMessage(responseBody));
                }
                return new IllegalArgumentException("Conflict: " + methodKey + " - " + responseBody);
            case 500:
                return new RuntimeException("Internal server error: " + methodKey + " - " + responseBody);
            case 503:
                return new RuntimeException("Service unavailable: " + methodKey);
            default:
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }

    private String extractResponseBody(Response response) {
        try {
            if (response.body() != null) {
                InputStream inputStream = response.body().asInputStream();
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            logger.warn("Failed to extract response body", e);
        }
        return "";
    }

    private boolean isDeviceServiceError(String responseBody, String errorCode) {
        try {
            if (responseBody != null && !responseBody.isEmpty()) {
                Map<String, Object> errorResponse = objectMapper.readValue(responseBody, Map.class);
                return errorCode.equals(errorResponse.get("error"));
            }
        } catch (Exception e) {
            logger.debug("Failed to parse error response body: {}", responseBody, e);
        }
        return false;
    }

    private String extractErrorMessage(String responseBody) {
        try {
            if (responseBody != null && !responseBody.isEmpty()) {
                Map<String, Object> errorResponse = objectMapper.readValue(responseBody, Map.class);
                return (String) errorResponse.get("message");
            }
        } catch (Exception e) {
            logger.debug("Failed to extract error message from response body: {}", responseBody, e);
        }
        return "Unknown error";
    }
} 