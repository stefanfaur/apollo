package ro.faur.apollo.media.service;

public interface MediaAnalysisService {
    String getDescriptionFromImage(String base64Image);
    String getDescriptionFromVideo(String base64Video);
} 