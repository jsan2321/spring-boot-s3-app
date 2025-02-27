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
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("s3")
public class AppController {

    @Value("${spring.destination.folder}")
    private String destinationFolder;

    @Autowired
    private IS3Service s3Service;

    @PostMapping("/create")
    public ResponseEntity<String> createBucket(@RequestParam String bucketName) {
        return ResponseEntity.ok(s3Service.createBucket(bucketName));
    }

    @GetMapping("/check/{buckerName}")
    public ResponseEntity<String> cheackBucket(@RequestParam String bucketName) {
        return ResponseEntity.ok(s3Service.checkIfBucketExists(bucketName));
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> listBuckets() {
        return ResponseEntity.ok(s3Service.getAllBuckets());
    }

    public ResponseEntity<String> uploadFile(@RequestParam String bucketName, @RequestParam String key, @RequestPart MultipartFile file) throws IOException {
        try {
            Path staticDir = Paths.get(destinationFolder);
            if (!Files.exists(staticDir)) {
                Files.createDirectories(staticDir);
            }
            Path filePath = staticDir.resolve(Objects.requireNonNull(file.getOriginalFilename()));
            Path finalPath = Files.write(filePath, file.getBytes());
            s3Service.uploadFile(bucketName, key, finalPath);
            Boolean result = s3Service.uploadFile(bucketName, key, finalPath);

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

    @PostMapping("/download")
    public ResponseEntity<String> downloadFile(@RequestParam String bucketName, @RequestParam String key) throws IOException {
        s3Service.downloadFile(bucketName, key);
        return ResponseEntity.ok("File downloaded successfully");
    }
}
