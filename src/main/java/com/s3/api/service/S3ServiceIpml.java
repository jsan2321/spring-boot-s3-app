package com.s3.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Service implementation for interacting with AWS S3.
 * This class provides methods for creating buckets, checking bucket existence,
 * listing buckets, uploading files, and downloading files.
 */
@Service
public class S3ServiceIpml implements IS3Service{

    // Injects the destination folder for downloaded files from the application properties file.
    @Value("${spring.destination.folder}")
    private String destinationFolder;

    // Autowires the S3Client bean for synchronous operations.
    @Autowired
    private S3Client s3Client;

    // Autowires the S3AsyncClient bean for asynchronous operations
    //@Autowired
    //private S3AsyncClient s3AsyncClient;

    /**
     * Creates a new S3 bucket with the specified name.
     *
     * @param bucketName The name of the bucket to create.
     * @return A message indicating the location of the created bucket.
     */
    @Override
    public String createBucket(String bucketName) {
        CreateBucketResponse response = this.s3Client.createBucket(bucketBuilder -> bucketBuilder.bucket(bucketName));
        return "Bucket created in location: " + response.location();
    }

    /**
     * Checks if a bucket with the specified name exists.
     *
     * @param bucketName The name of the bucket to check.
     * @return A message indicating whether the bucket exists or not.
     */
    @Override
    public String checkIfBucketExists(String bucketName) {
        try {
            this.s3Client.headBucket(headBucketBuilder -> headBucketBuilder.bucket(bucketName));
            return "Bucket does exist: " + bucketName;
        } catch (S3Exception exception) {
            return "Bucket does not exist: " + bucketName;
        }
    }

    /**
     * Retrieves a list of all bucket names in the S3 account.
     *
     * @return A list of bucket names, or an empty list if no buckets exist.
     */
    @Override
    public List<String> getAllBuckets() {
        ListBucketsResponse bucketsResponse = this.s3Client.listBuckets();
        if(bucketsResponse.hasBuckets()) {
            return bucketsResponse.buckets()
                                  .stream()
                                  .map(Bucket::name)
                                  .toList();
                                  //.collect(Collectors.toList()); Previous to Java 17
        } else {
            return List.of();
            //return Collections.emptyList();
        }
    }

    /**
     * Uploads a file to the specified S3 bucket.
     *
     * @param bucketName   The name of the bucket to upload the file to.
     * @param key          The key (path) under which the file will be stored in the bucket.
     * @param fileLocation The path to the file on the local filesystem.
     * @return A boolean indicating whether the upload was successful.
     */
    @Override
    public Boolean uploadFile(String bucketName, String key, Path fileLocation) {

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                                                            .bucket(bucketName)
                                                            .key(key)
                                                            .build();
        PutObjectResponse putObjectResponse = this.s3Client.putObject(putObjectRequest, fileLocation);
        //return putObjectResponse.sdkHttpResponse().statusCode() == 200;
        return putObjectResponse.sdkHttpResponse().isSuccessful();
    }

    /**
     * Downloads a file from the specified S3 bucket.
     *
     * @param bucketName The name of the bucket from which to download the file.
     * @param key        The key (path) of the file in the bucket.
     * @throws IOException If an error occurs while writing the file to the local filesystem.
     */
    @Override
    public void downloadFile(String bucketName, String key) throws IOException {
        // Build the request to download the file.
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                                                            .bucket(bucketName)
                                                            .key(key)
                                                            .build();

        // Retrieve the file as a byte array.
        ResponseBytes<GetObjectResponse> objectBytes = this.s3Client.getObjectAsBytes(getObjectRequest);

        // Extract the file name from the key.
        String fileName;
        if(key.contains("/")) {
            fileName = key.substring(key.lastIndexOf('/'));
        } else {
            fileName = key;
        }

        // Construct the full file path for saving the downloaded file.
        //String filePath = destinationFolder + File.separator + fileName;
        //String filePath = Paths.get(destinationFolder, fileName).toFile().getAbsolutePath();
        String filePath = Paths.get(destinationFolder, fileName).toString();

        // Ensure the parent directory exists.
        File file = new File(filePath);
        file.getParentFile().mkdir();

        // Write the file to the local filesystem.
        try(FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(objectBytes.asByteArray());
        } catch (IOException e) {
            throw new IOException("Could not download file: " + e.getCause());
        }
    }
}
