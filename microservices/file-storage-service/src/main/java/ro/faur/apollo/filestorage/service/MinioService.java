package ro.faur.apollo.filestorage.service;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    public MinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public String generatePresignedUploadUrl(String objectName, int expirySeconds) {
        try {
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expirySeconds)
                    .build();

            return minioClient.getPresignedObjectUrl(args);
        } catch (Exception e) {
            throw new RuntimeException("Error generating pre-signed upload URL", e);
        }
    }

    public String generatePresignedDownloadUrl(String objectName, int expirySeconds) {
        try {
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(expirySeconds)
                    .build();

            return minioClient.getPresignedObjectUrl(args);
        } catch (Exception e) {
            throw new RuntimeException("Error generating pre-signed download URL", e);
        }
    }

    public void uploadFile(String objectName, InputStream inputStream, String contentType) {
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, -1, 10485760) // 10MB part size
                    .contentType(contentType)
                    .build();

            minioClient.putObject(args);
        } catch (Exception e) {
            throw new RuntimeException("Error uploading file to Minio", e);
        }
    }

    public InputStream downloadFile(String objectName) {
        try {
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            return minioClient.getObject(args);
        } catch (Exception e) {
            throw new RuntimeException("Error downloading file from Minio", e);
        }
    }

    public void deleteFile(String objectName) {
        try {
            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            minioClient.removeObject(args);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting file from Minio", e);
        }
    }

    public boolean fileExists(String objectName) {
        try {
            StatObjectArgs args = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            minioClient.statObject(args);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
} 