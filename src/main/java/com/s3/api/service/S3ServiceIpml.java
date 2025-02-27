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
import java.time.Duration;
import java.util.List;

@Service
public class S3ServiceIpml implements IS3Service{

    @Value("${spring.destination.folder}")
    private String destinationFolder;

    @Autowired
    private S3Client s3Client;

    //@Autowired
    //private S3AsyncClient s3AsyncClient;

    @Override
    public String createBucket(String bucketName) {
        CreateBucketResponse response = this.s3Client.createBucket(bucketBuilder -> bucketBuilder.bucket(bucketName));
        return "Bucket created in location: " + response.location();
    }

    @Override
    public String checkIfBucketExists(String bucketName) {
        try {
            this.s3Client.headBucket(headBucketBuilder -> headBucketBuilder.bucket(bucketName));
            return "Bucket does exist: " + bucketName;
        } catch (S3Exception exception) {
            return "Bucket does not exist: " + bucketName;
        }
    }

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

    @Override
    public void downloadFile(String bucketName, String key) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                                                            .bucket(bucketName)
                                                            .key(key)
                                                            .build();

        ResponseBytes<GetObjectResponse> objectBytes = this.s3Client.getObjectAsBytes(getObjectRequest);

        String fileName;
        if(key.contains("/")) {
            fileName = key.substring(key.lastIndexOf('/'));
        } else {
            fileName = key;
        }

        //String filePath = destinationFolder + File.separator + fileName;
        //String filePath = Paths.get(destinationFolder, fileName).toFile().getAbsolutePath();
        String filePath = Paths.get(destinationFolder, fileName).toString();

        File file = new File(filePath);
        file.getParentFile().mkdir();

        try(FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(objectBytes.asByteArray());
        } catch (IOException e) {
            throw new IOException("Could not download file: " + e.getCause());
        }
    }
}
