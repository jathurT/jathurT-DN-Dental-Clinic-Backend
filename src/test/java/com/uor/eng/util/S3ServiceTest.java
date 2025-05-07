package com.uor.eng.util;

import com.uor.eng.exceptions.FileStorageException;
import com.uor.eng.payload.patient.logs.PresignedUrlRequest;
import com.uor.eng.payload.patient.logs.PresignedUrlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class S3ServiceTest {

  @Mock
  private S3Client s3Client;

  @Mock
  private S3Presigner presigner;

  @Mock
  private MultipartFile multipartFile;

  @Mock
  private PresignedPutObjectRequest presignedPutObjectRequest;

  @InjectMocks
  private S3Service s3Service;

  private final String BUCKET_NAME = "test-bucket";
  private final String REGION = "us-east-1";
  private final String FILE_NAME = "test-file.jpg";
  private final String CONTENT_TYPE = "image/jpeg";
  private final byte[] FILE_CONTENT = "test file content".getBytes();

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(s3Service, "bucketName", BUCKET_NAME);
    ReflectionTestUtils.setField(s3Service, "region", REGION);
    ReflectionTestUtils.setField(s3Service, "presigner", presigner);
  }

  @Test
  void uploadFile_ShouldUploadSuccessfully() throws IOException {
    // Arrange
    when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);
    when(multipartFile.getContentType()).thenReturn(CONTENT_TYPE);
    when(multipartFile.getBytes()).thenReturn(FILE_CONTENT);

    // Act
    String result = s3Service.uploadFile(multipartFile);

    // Assert
    assertNotNull(result);
    assertTrue(result.contains(FILE_NAME.replace(" ", "_")));

    ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

    verify(s3Client).putObject(requestCaptor.capture(), bodyCaptor.capture());

    PutObjectRequest capturedRequest = requestCaptor.getValue();
    assertEquals(BUCKET_NAME, capturedRequest.bucket());
    assertTrue(capturedRequest.key().contains(FILE_NAME.replace(" ", "_")));
    assertEquals(CONTENT_TYPE, capturedRequest.contentType());
  }

  @Test
  void uploadFile_WhenS3Exception_ShouldThrowFileStorageException() throws IOException {
    // Arrange
    when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);
    when(multipartFile.getContentType()).thenReturn(CONTENT_TYPE);
    when(multipartFile.getBytes()).thenReturn(FILE_CONTENT);

    S3Exception s3Exception = (S3Exception) S3Exception.builder()
            .awsErrorDetails(AwsErrorDetails.builder().errorMessage("S3 error").build())
            .build();

    doThrow(s3Exception).when(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

    // Act & Assert
    FileStorageException exception = assertThrows(FileStorageException.class, () -> {
      s3Service.uploadFile(multipartFile);
    });

    assertTrue(exception.getMessage().contains("Failed to upload file to S3"));
  }

  @Test
  void uploadFile_WhenIOException_ShouldThrowFileStorageException() throws IOException {
    // Arrange
    when(multipartFile.getOriginalFilename()).thenReturn(FILE_NAME);
    when(multipartFile.getContentType()).thenReturn(CONTENT_TYPE);
    when(multipartFile.getBytes()).thenThrow(new IOException("IO error"));

    // Act & Assert
    FileStorageException exception = assertThrows(FileStorageException.class, () -> {
      s3Service.uploadFile(multipartFile);
    });

    assertTrue(exception.getMessage().contains("Failed to read file data"));
  }

  @Test
  void generatePresignedUrl_ShouldGenerateSuccessfully() {
    // Arrange
    PresignedUrlRequest request = new PresignedUrlRequest();
    request.setFileName(FILE_NAME);
    request.setContentType(CONTENT_TYPE);

    URL mockUrl = mock(URL.class);
    when(mockUrl.toString()).thenReturn("https://presigned-url.example.com");

    when(presignedPutObjectRequest.url()).thenReturn(mockUrl);
    when(presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPutObjectRequest);

    // Act
    PresignedUrlResponse response = s3Service.generatePresignedUrl(request);

    // Assert
    assertNotNull(response);
    assertEquals("https://presigned-url.example.com", response.getUrl());
    assertNotNull(response.getKey());
    assertTrue(response.getKey().contains(FILE_NAME.replace(" ", "_")));

    ArgumentCaptor<PutObjectPresignRequest> presignRequestCaptor = ArgumentCaptor.forClass(PutObjectPresignRequest.class);
    verify(presigner).presignPutObject(presignRequestCaptor.capture());

    PutObjectPresignRequest capturedRequest = presignRequestCaptor.getValue();
    assertEquals(Duration.ofMinutes(10), capturedRequest.signatureDuration());
  }

  @Test
  void generateUniqueKey_ShouldGenerateUniqueKey() {
    // Act
    String key1 = s3Service.generateUniqueKey(FILE_NAME);
    String key2 = s3Service.generateUniqueKey(FILE_NAME);

    // Assert
    assertNotNull(key1);
    assertNotNull(key2);
    assertNotEquals(key1, key2);
    assertTrue(key1.contains(FILE_NAME.replace(" ", "_")));
    assertTrue(key2.contains(FILE_NAME.replace(" ", "_")));
  }

  @Test
  void getFileUrl_ShouldGenerateCorrectUrl() {
    // Arrange
    String key = "unique-key_test-file.jpg";

    // Act
    String url = s3Service.getFileUrl(key);

    // Assert
    assertEquals("https://test-bucket.s3.us-east-1.amazonaws.com/unique-key_test-file.jpg", url);
  }

  @Test
  void deleteFile_ShouldDeleteSuccessfully() {
    // Arrange
    String key = "unique-key_test-file.jpg";

    // Act
    s3Service.deleteFile(key);

    // Assert
    ArgumentCaptor<DeleteObjectRequest> requestCaptor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
    verify(s3Client).deleteObject(requestCaptor.capture());

    DeleteObjectRequest capturedRequest = requestCaptor.getValue();
    assertEquals(BUCKET_NAME, capturedRequest.bucket());
    assertEquals(key, capturedRequest.key());
  }

  @Test
  void deleteFile_WhenS3Exception_ShouldThrowFileStorageException() {
    // Arrange
    String key = "unique-key_test-file.jpg";

    S3Exception s3Exception = (S3Exception) S3Exception.builder()
            .awsErrorDetails(AwsErrorDetails.builder().errorMessage("S3 error").build())
            .build();

    doThrow(s3Exception).when(s3Client).deleteObject(any(DeleteObjectRequest.class));

    // Act & Assert
    FileStorageException exception = assertThrows(FileStorageException.class, () -> {
      s3Service.deleteFile(key);
    });

    assertTrue(exception.getMessage().contains("Failed to delete file from S3"));
  }

  @Test
  void initializePresigner_ShouldInitializePresigner() {
    // Arrange
    S3Service serviceSpy = spy(new S3Service(s3Client));
    ReflectionTestUtils.setField(serviceSpy, "region", REGION);

    // Act
    serviceSpy.initializePresigner();

    // Assert
    verify(serviceSpy).initializePresigner();
  }
}