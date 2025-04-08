package com.uor.eng.controller;

import com.uor.eng.payload.patient.logs.PresidedUrlRequest;
import com.uor.eng.payload.patient.logs.PresidedUrlResponse;
import com.uor.eng.util.S3Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/s3")
public class S3Controller {

  private final S3Service s3Service;

  public S3Controller(S3Service s3Service) {
    this.s3Service = s3Service;
  }

  @PostMapping("/generate-presigned-url")
  public ResponseEntity<PresidedUrlResponse> generatePresignedUrl(@RequestBody PresidedUrlRequest request) {
    PresidedUrlResponse response = s3Service.generatePresignedUrl(request);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @PostMapping("/upload")
  public ResponseEntity<String> uploadFile(@RequestBody MultipartFile file) {
    String fileKey = s3Service.uploadFile(file);
    return ResponseEntity.ok(fileKey);
  }
}
