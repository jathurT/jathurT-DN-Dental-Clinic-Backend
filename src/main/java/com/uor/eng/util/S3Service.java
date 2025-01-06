package com.uor.eng.util;
import com.uor.eng.exceptions.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
public class S3Service {

  private final S3Client s3Client;

  @Value("${aws.s3.bucket}")
  private String bucketName;

  @Value("${aws.region}")
  private String region;

  public S3Service(S3Client s3Client) {
    this.s3Client = s3Client;
  }

  public String uploadFile(MultipartFile file) {
    String key = generateUniqueKey(Objects.requireNonNull(file.getOriginalFilename()));

    try {
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
//          .acl(ObjectCannedACL.PUBLIC_READ)
          .contentType(file.getContentType())
          .build();

      s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));
    } catch (S3Exception e) {
      throw new FileStorageException("Failed to upload file to S3: " + e.awsErrorDetails().errorMessage(), e);
    } catch (IOException e) {
      throw new FileStorageException("Failed to read file data: " + e.getMessage(), e);
    }

    return key;
  }

  public void deleteFile(String key) {
    try {
      DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
          .bucket(bucketName)
          .key(key)
          .build();
      s3Client.deleteObject(deleteObjectRequest);
    } catch (S3Exception e) {
      throw new FileStorageException("Failed to delete file from S3: " + e.awsErrorDetails().errorMessage(), e);
    }
  }

  public String getFileUrl(String key) {
    return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
  }

  private String generateUniqueKey(String originalFilename) {
    String uuid = UUID.randomUUID().toString();
    return uuid + "_" + originalFilename.replace(" ", "_");
  }
}
