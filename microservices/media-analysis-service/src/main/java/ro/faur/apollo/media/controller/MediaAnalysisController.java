package ro.faur.apollo.media.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ro.faur.apollo.media.service.ImageProcessorService;
import ro.faur.apollo.media.service.MediaAnalysisService;

@RestController
@RequestMapping("/api/media")
public class MediaAnalysisController {

    private final MediaAnalysisService mediaAnalysisService;
    private final ImageProcessorService imageProcessorService;

    public MediaAnalysisController(MediaAnalysisService mediaAnalysisService, 
                                 ImageProcessorService imageProcessorService) {
        this.mediaAnalysisService = mediaAnalysisService;
        this.imageProcessorService = imageProcessorService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<String> analyzeMedia(@RequestParam String mediaUrl) {
        try {
            String base64Media = imageProcessorService.downloadAndConvertToBase64(mediaUrl);
            if (base64Media == null) {
                return ResponseEntity.badRequest().body("Failed to download media");
            }

            String description;
            if (imageProcessorService.isImage(mediaUrl)) {
                description = mediaAnalysisService.getDescriptionFromImage(base64Media);
            } else if (imageProcessorService.isVideo(mediaUrl)) {
                description = mediaAnalysisService.getDescriptionFromVideo(base64Media);
            } else {
                return ResponseEntity.badRequest().body("Unsupported media type");
            }

            return ResponseEntity.ok(description);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error analyzing media: " + e.getMessage());
        }
    }

    @PostMapping("/analyze/image")
    public ResponseEntity<String> analyzeImage(@RequestBody String base64Image) {
        try {
            String description = mediaAnalysisService.getDescriptionFromImage(base64Image);
            return ResponseEntity.ok(description);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error analyzing image: " + e.getMessage());
        }
    }

    @PostMapping("/analyze/video")
    public ResponseEntity<String> analyzeVideo(@RequestBody String base64Video) {
        try {
            String description = mediaAnalysisService.getDescriptionFromVideo(base64Video);
            return ResponseEntity.ok(description);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error analyzing video: " + e.getMessage());
        }
    }
} 