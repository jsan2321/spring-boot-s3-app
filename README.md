# Spring Boot S3 Application

This is a Spring Boot application that integrates with AWS S3 to perform various operations such as creating buckets, uploading/downloading files, and generating presigned URLs for temporary access to S3 objects.

## Features

- **Bucket Management**:
    - Create a new S3 bucket.
    - Check if a bucket exists.
    - List all buckets in the S3 account.

- **File Operations**:
    - Upload files to an S3 bucket.
    - Download files from an S3 bucket.
    - Generate presigned URLs for uploading and downloading files.

- **Configuration**:
    - Configure AWS credentials and region via `application.properties`.
    - Set file upload limits and destination folders.

## Prerequisites

- Java 21
- Maven 3.x
- AWS account with S3 access
- AWS access key and secret key

### **AWS IAM Setup**

To interact with AWS S3, create an IAM user with the necessary permissions:

1. **Create an IAM User**:
    - Go to the AWS Management Console → IAM → Users → **Create user**.
    - Enter a username (e.g., `s3-access-user`) and click **Next**.

2. **Attach Permissions**:
    - Select **Attach policies directly**.
    - Attach the **AmazonS3FullAccess** policy (for testing) or create a custom policy with minimal permissions:
    ```json
      {
          "Version": "2012-10-17",
          "Statement": [
              {
                  "Effect": "Allow",
                  "Action": [
                      "s3:*",
                      "s3-object-lambda:*"
                  ],
                  "Resource": "*"
              }
          ]
      }
    ```
    - Click **Next** → **Create user**.

3. **Generate Access Keys**:
    - Go to the user’s **Security credentials** tab → **Create access key**.
    - Select **Application running outside AWS** → **Create access key**.
    - Copy the **Access key** and **Secret access key**.

4. **Update `application.properties`**:
   Replace placeholders with your AWS credentials:
   ```properties
   aws.access.key=YOUR_ACCESS_KEY
   aws.secret.key=YOUR_SECRET_KEY
   aws.region=us-east-1
   ```
   
## Configuration

Add the following to your `application.properties` file:

```properties
spring.application.name=spring-boot-s3-app

# AWS S3
aws.access.key=${ACCESS_KEY}
aws.secret.key=${SECRET_KEY}
aws.region=us-east-1

# FILES CONFIG
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=1MB
spring.servlet.multipart.max-request-size=10MB
spring.destination.folder=src/main/resources/static

# LOGGING
logging.level.software.amazon.awssdk=DEBUG
```

Replace `${ACCESS_KEY}` and `${SECRET_KEY}` with your actual AWS credentials.

## Running the Application

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/spring-boot-s3-app.git
   cd spring-boot-s3-app
   ```

2. Build the project using Maven:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The application will start on port `8080` by default.

## API Endpoints

### Bucket Management

- **Create Bucket**:
  ```
  POST /s3/create?bucketName={bucketName}
  ```

- **Check Bucket Existence**:
  ```
  GET /s3/check/{bucketName}
  ```

- **List Buckets**:
  ```
  GET /s3/list
  ```

### File Operations

- **Upload File**:
  ```
  POST /s3/upload?bucketName={bucketName}&key={key}
  ```
  **Note**: The file should be sent as a multipart form data with the key `file`.

- **Download File**:
  ```
  POST /s3/download?bucketName={bucketName}&key={key}
  ```

- **Generate Presigned Upload URL**:
  ```
  POST /s3/upload/presigned?bucketName={bucketName}&key={key}&expiration={expiration}
  ```

- **Generate Presigned Download URL**:
  ```
  POST /s3/download/presigned?bucketName={bucketName}&key={key}&expiration={expiration}
  ```

## Dependencies

- **Spring Boot Starter Web**: For building web applications, including RESTful services.
- **AWS SDK for Java (S3)**: For interacting with AWS S3.