package com.codingshuttle.hackathon.skillsyncai.service.impl;

import com.codingshuttle.hackathon.skillsyncai.service.StorageService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageServiceImpl implements StorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile file, String fileName) throws IOException {
        try {
            // Check if bucket exists
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Bucket '{}' created.", bucketName);
            }

            // Upload file
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            log.info("File '{}' uploaded successfully to bucket '{}'.", fileName, bucketName);
            return fileName;
        } catch (Exception e) {
            log.error("Error occurred while uploading file to MinIO", e);
            throw new IOException("Error uploading file to MinIO", e);
        }
    }

    @Override
    public Resource downloadFile(String fileName) throws IOException {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());

            return new ByteArrayResource(stream.readAllBytes());
        } catch (Exception e) {
            log.error("Error occurred while downloading file from MinIO", e);
            throw new IOException("Error downloading file from MinIO", e);
        }
    }

    @Override
    public void deleteFile(String fileName) throws IOException {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
            log.info("File '{}' deleted successfully from bucket '{}'.", fileName, bucketName);
        } catch (Exception e) {
            log.error("Error occurred while deleting file from MinIO", e);
            throw new IOException("Error deleting file from MinIO", e);
        }
    }

    @Override
    public String getFileUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .expiry(7, TimeUnit.DAYS)
                            .build());
        } catch (Exception e) {
            log.error("Error getting presigned URL", e);
            return null;
        }
    }
}
