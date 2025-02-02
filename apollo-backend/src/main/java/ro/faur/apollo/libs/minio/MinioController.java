package ro.faur.apollo.libs.minio;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/minio")
public class MinioController {

    private final MinioService minioService;

    public MinioController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<Map<String, String>> getPresignedUrl(@RequestParam("filename") String filename) {

        String objectName = "uploads/" + filename;
        String presignedUrl = minioService.generatePresignedUrl(objectName, 3600);

        Map<String, String> response = new HashMap<>();
        response.put("url", presignedUrl);
        response.put("objectName", objectName);
        return ResponseEntity.ok(response);
    }
}
