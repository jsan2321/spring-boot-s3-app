package com.s3.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * Configuration class for setting up AWS S3 clients.
 * This class defines beans for both synchronous and asynchronous S3 clients.
 */
@Configuration
public class S3Config {

    // Injects the AWS access key from the application properties file.
    @Value("${aws.access.key}")
    private String accessKeyId;

    // Injects the AWS secret key from the application properties file.
    @Value("${aws.secret.key}")
    private String secretAccessKey;

    // Injects the AWS region from the application properties file.
    @Value("${aws.region}")
    private String region;

    /**
     * Creates and configures a synchronous S3 client.
     * This client is used for blocking operations with AWS S3.
     *
     * @return A configured instance of S3Client.
     */
    @Bean
    public S3Client getS3Client() {
        // Create AWS credentials using the access key and secret key.
        AwsCredentials basicCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        // Build and return the S3Client with the specified region, endpoint, and credentials.
        return S3Client.builder()
                       //.region(Region.US_EAST_1) // Example of hardcoding a region (commented out).
                       .region(Region.of(region)) // Use the region specified in the properties file.
                       .endpointOverride(URI.create("https://s3.us-east-1.amazonaws.com")) // Override the default endpoint.
                       .credentialsProvider(StaticCredentialsProvider.create(basicCredentials)) // Set the credentials provider.
                       .build();
    }

    /**
     * Creates and configures an asynchronous S3 client.
     * This client is used for non-blocking operations with AWS S3.
     *
     * @return A configured instance of S3AsyncClient.
     */
    @Bean
    public S3AsyncClient getS3AsyncClient() {
        // Create AWS credentials using the access key and secret key.
        AwsCredentials basicCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        // Build and return the S3AsyncClient with the specified region, endpoint, and credentials.
        return S3AsyncClient.builder()
                            .region(Region.of(region)) // Use the region specified in the properties file.
                            .endpointOverride(URI.create("https://s3.us-east-1.amazonaws.com")) // Override the default endpoint.
                            .credentialsProvider(StaticCredentialsProvider.create(basicCredentials)) // Set the credentials provider.
                            .build();
    }
}
