package com.s3.api.service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public interface IS3Service {
    // Create a bucket in S3
    String createBucket(String bucketName);

    // Check existence of a bucket
    String checkIfBucketExists(String bucketName);

    // List buckets
    List<String> getAllBuckets();

    // Upload a file in a bucket
    Boolean uploadFile(String bucketName, String key, Path fileLocation);

    // Download a file from a bucket
    void downloadFile(String bucketName, String key) throws IOException;

    // Generate presigned URL to upload files
    String generatePresignedUploadUrl(String bucketName, String key, Duration expiration);

    // Generate presigned URL to download files
    String generatePresignedDownloadUrl(String bucketName, String key, Duration expiration);
}
