package ro.faur.apollo.filestorage.controller;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ro.faur.apollo.filestorage.service.ImageProcessorService;
import ro.faur.apollo.filestorage.service.MinioService;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileStorageController {

    private final MinioService minioService;
    private final ImageProcessorService imageProcessorService;

    public FileStorageController(MinioService minioService, ImageProcessorService imageProcessorService) {
        this.minioService = minioService;
        this.imageProcessorService = imageProcessorService;
    }

    /**
     * Generate pre-signed URL for file upload
     */
    @GetMapping("/presigned-url")
    public ResponseEntity<Map<String, String>> getPresignedUploadUrl(@RequestParam("filename") String filename) {
        String objectName = "uploads/" + filename;
        String presignedUrl = minioService.generatePresignedUploadUrl(objectName, 3600);

        Map<String, String> response = new HashMap<>();
        response.put("url", presignedUrl);
        response.put("objectName", objectName);
        return ResponseEntity.ok(response);
    }

    /**
     * Generate pre-signed URL for file download
     */
    @GetMapping("/presigned-download-url")
    public ResponseEntity<Map<String, String>> getPresignedDownloadUrl(@RequestParam("objectName") String objectName) {
        String presignedUrl = minioService.generatePresignedDownloadUrl(objectName, 3600);

        Map<String, String> response = new HashMap<>();
        response.put("url", presignedUrl);
        response.put("objectName", objectName);
        return ResponseEntity.ok(response);
    }

    /**
     * Upload file directly to service
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            String objectName = "uploads/" + UUID.randomUUID() + "_" + filename;
            
            minioService.uploadFile(objectName, file.getInputStream(), file.getContentType());

            Map<String, String> response = new HashMap<>();
            response.put("objectName", objectName);
            response.put("url", "http://localhost:9000/apollo-bucket/" + objectName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Download file directly from service
     */
    @GetMapping("/download/{objectName}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String objectName) {
        try {
            InputStream inputStream = minioService.downloadFile(objectName);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + objectName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(inputStream));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete file from storage
     */
    @DeleteMapping("/{objectName}")
    public ResponseEntity<?> deleteFile(@PathVariable String objectName) {
        try {
            minioService.deleteFile(objectName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Check if file exists
     */
    @GetMapping("/exists/{objectName}")
    public ResponseEntity<Map<String, Boolean>> fileExists(@PathVariable String objectName) {
        boolean exists = minioService.fileExists(objectName);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    /**
     * Convert media URL to base64
     */
    @PostMapping("/convert-to-base64")
    public ResponseEntity<Map<String, String>> convertToBase64(@RequestBody Map<String, String> request) {
        String mediaUrl = request.get("mediaUrl");
        if (mediaUrl == null) {
            return ResponseEntity.badRequest().build();
        }

        String base64 = imageProcessorService.downloadAndConvertToBase64(mediaUrl);
        Map<String, String> response = new HashMap<>();
        response.put("base64", base64);
        response.put("isImage", String.valueOf(imageProcessorService.isImage(mediaUrl)));
        response.put("isVideo", String.valueOf(imageProcessorService.isVideo(mediaUrl)));
        return ResponseEntity.ok(response);
    }
} 