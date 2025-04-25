package com.uor.eng.controller;

import com.uor.eng.payload.patient.logs.PresignedUrlRequest;
import com.uor.eng.payload.patient.logs.PresignedUrlResponse;
import com.uor.eng.util.S3Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/s3")
public class S3Controller {

  private final S3Service s3Service;

  public S3Controller(S3Service s3Service) {
    this.s3Service = s3Service;
  }

  @PostMapping("/generate-presigned-url")
  public ResponseEntity<PresignedUrlResponse> generatePresignedUrl(@RequestBody PresignedUrlRequest request) {
    PresignedUrlResponse response = s3Service.generatePresignedUrl(request);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @PostMapping("/upload")
  public ResponseEntity<String> uploadFile(@RequestBody MultipartFile file) {
    String fileKey = s3Service.uploadFile(file);
    return ResponseEntity.ok(fileKey);
  }

  @DeleteMapping("/delete/{key}")
  public ResponseEntity<Void> deleteFile(@PathVariable String key) {
    s3Service.deleteFile(key);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
