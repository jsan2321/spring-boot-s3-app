package com.s3.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${aws.access.key}")
    private String accessKeyId;

    @Value("${aws.secret.key}")
    private String secretAccessKey;

    @Value("${aws.region}")
    private String region;

    /**
     *  S3 Synchronous Client
     */
    public S3Client getS3Client() {
        AwsCredentials basicCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        return S3Client.builder()
                       //.region(Region.US_EAST_1)
                       .region(Region.of(region)).endpointOverride(URI.create("https://s3.us-east-1.amazonaws.com"))
                       .credentialsProvider(StaticCredentialsProvider.create(basicCredentials))
                       .build();
    }

    /**
     * S3 Asynchronous Client
     */
    public S3AsyncClient getS3AsyncClient() {
        AwsCredentials basicCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        return S3AsyncClient.builder()
                            .region(Region.of(region))
                            .endpointOverride(URI.create("https://s3.us-east-1.amazonaws.com"))
                            .credentialsProvider(StaticCredentialsProvider.create(basicCredentials))
                            .build();
    }
}
