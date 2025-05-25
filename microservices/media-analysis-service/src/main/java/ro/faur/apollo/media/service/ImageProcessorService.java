package ro.faur.apollo.media.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
public class ImageProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(ImageProcessorService.class);
    private final RestTemplate restTemplate;

    public ImageProcessorService() {
        this.restTemplate = new RestTemplate();
    }

    public boolean isImage(String url) {
        return url.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|bmp|webp).*");
    }

    public boolean isVideo(String url) {
        return url.toLowerCase().matches(".*\\.(mp4|avi|mov|wmv|flv|webm|mkv).*");
    }

    public String downloadAndConvertToBase64(String url) {
        try {
            byte[] mediaBytes = restTemplate.getForObject(url, byte[].class);
            if (mediaBytes != null) {
                return Base64.getEncoder().encodeToString(mediaBytes);
            }
            return null;
        } catch (Exception e) {
            logger.error("Error downloading media from URL: {}", url, e);
            return null;
        }
    }
} 