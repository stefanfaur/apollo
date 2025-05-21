package ro.faur.apollo.libs.images.analyzer.service;

public interface MediaAnalysisApiService {
    String getDescriptionFromImage(String base64Image);
    String getDescriptionFromVideo(String base64Video);
}
