package ro.faur.apollo.libs.minio;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * Generates a pre-signed URL for a given object name with a given expiry time,
     * so devices can upload files directly to Minio without needing to go through the backend.
     * @param objectName
     * @param expirySeconds
     * @return
     */
    public String generatePresignedUrl(String objectName, int expirySeconds) {
        try {
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expirySeconds)
                    .build();

            return minioClient.getPresignedObjectUrl(args);
        } catch (Exception e) {
            throw new RuntimeException("Error generating pre-signed URL", e);
        }
    }
}
