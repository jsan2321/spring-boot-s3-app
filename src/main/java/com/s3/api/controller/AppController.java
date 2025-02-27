package com.s3.api.controller;

import com.s3.api.service.IS3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * REST controller for handling S3-related operations.
 * This controller provides endpoints for creating buckets, checking bucket existence,
 * listing buckets, uploading files, and downloading files.
 */
@RestController
@RequestMapping("s3") // Base path for all endpoints in this controller
public class AppController {

    // Injects the destination folder for temporary file storage from the application properties file.
    @Value("${spring.destination.folder}")
    private String destinationFolder;

    // Autowires the S3 service to handle business logic.
    @Autowired
    private IS3Service s3Service;

    /**
     * Endpoint to create a new S3 bucket.
     *
     * @param bucketName The name of the bucket to create.
     * @return A response indicating the result of the bucket creation.
     */
    @PostMapping("/create")
    public ResponseEntity<String> createBucket(@RequestParam String bucketName) {
        return ResponseEntity.ok(s3Service.createBucket(bucketName));
    }

    /**
     * Endpoint to check if a bucket exists.
     *
     * @param bucketName The name of the bucket to check.
     * @return A response indicating whether the bucket exists or not.
     */
    @GetMapping("/check/{bucketName}")
    public ResponseEntity<String> checkBucket(@PathVariable String bucketName) {
        return ResponseEntity.ok(s3Service.checkIfBucketExists(bucketName));
    }

    /**
     * Endpoint to list all buckets in the S3 account.
     *
     * @return A response containing a list of bucket names.
     */
    @GetMapping("/list")
    public ResponseEntity<List<String>> listBuckets() {
        return ResponseEntity.ok(s3Service.getAllBuckets());
    }

    /**
     * Endpoint to upload a file to an S3 bucket.
     *
     * @param bucketName The name of the bucket to upload the file to.
     * @param key        The key (path) under which the file will be stored in the bucket.
     * @param file       The file to upload.
     * @return A response indicating the result of the file upload.
     * @throws IOException If an error occurs while processing the file.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam String bucketName, @RequestParam String key, @RequestPart MultipartFile file) throws IOException {
        try {
            // Ensure the destination directory exists.
            Path staticDir = Paths.get(destinationFolder);
            if (!Files.exists(staticDir)) {
                Files.createDirectories(staticDir);
            }
            // Save the uploaded file to the local filesystem temporarily.
            Path filePath = staticDir.resolve(Objects.requireNonNull(file.getOriginalFilename()));
            Path finalPath = Files.write(filePath, file.getBytes());
            s3Service.uploadFile(bucketName, key, finalPath);
            // Upload the file to the S3 bucket.
            Boolean result = s3Service.uploadFile(bucketName, key, finalPath);

            // Delete the temporary file after upload.
            if(result) {
              Files.delete(finalPath);
              return ResponseEntity.ok("File uploaded successfully");
            } else {
                return ResponseEntity.internalServerError().body("File upload to bucket failed");
            }
        } catch (IOException e) {
            throw new IOException("Error while processing file... ", e);
        }
    }

    /**
     * Endpoint to download a file from an S3 bucket.
     *
     * @param bucketName The name of the bucket from which to download the file.
     * @param key        The key (path) of the file in the bucket.
     * @return A response indicating the result of the file download.
     * @throws IOException If an error occurs while downloading the file.
     */
    @PostMapping("/download")
    public ResponseEntity<String> downloadFile(@RequestParam String bucketName, @RequestParam String key) throws IOException {
        s3Service.downloadFile(bucketName, key);
        return ResponseEntity.ok("File downloaded successfully");
    }

    /**
     * Endpoint to generate a presigned URL for uploading a file to an S3 bucket.
     *
     * @param bucketName The name of the bucket where the file will be uploaded.
     * @param key        The key (path) under which the file will be stored in the bucket.
     * @param expiration The duration (in minutes) for which the presigned URL will be valid.
     * @return A response containing the presigned URL for uploading the file.
     */
    @PostMapping("/upload/presigned")
    public ResponseEntity<String> generatePresignedUploadUrl(
            @RequestParam String bucketName,
            @RequestParam String key,
            @RequestParam Long expiration) {
        // Convert the expiration time from minutes to a Duration object.
        Duration durationToLive = Duration.ofMinutes(expiration);

        // Generate and return the presigned URL.
        return ResponseEntity.ok(s3Service.generatePresignedUploadUrl(bucketName, key, durationToLive));
    }

    /**
     * Endpoint to generate a presigned URL for downloading a file from an S3 bucket.
     *
     * @param bucketName The name of the bucket from which the file will be downloaded.
     * @param key        The key (path) of the file in the bucket.
     * @param expiration The duration (in minutes) for which the presigned URL will be valid.
     * @return A response containing the presigned URL for downloading the file.
     */
    @PostMapping("/download/presigned")
    public ResponseEntity<String> generatePresignedDownloadUrl(
            @RequestParam String bucketName,
            @RequestParam String key,
            @RequestParam Long expiration) {
        // Convert the expiration time from minutes to a Duration object.
        Duration durationToLive = Duration.ofMinutes(expiration);

        // Generate and return the presigned URL.
        return ResponseEntity.ok(s3Service.generatePresignedDownloadUrl(bucketName, key, durationToLive));
    }
}
