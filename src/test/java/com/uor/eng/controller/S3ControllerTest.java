package com.uor.eng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uor.eng.exceptions.FileStorageException;
import com.uor.eng.payload.patient.logs.PresignedUrlRequest;
import com.uor.eng.payload.patient.logs.PresignedUrlResponse;
import com.uor.eng.util.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class S3ControllerTest {

  private MockMvc mockMvc;

  @Mock
  private S3Service s3Service;

  @InjectMocks
  private S3Controller s3Controller;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(s3Controller)
            .setControllerAdvice(new TestExceptionHandler())
            .build();
  }

  @Test
  public void testGeneratePresignedUrl_Success() throws Exception {
    // Arrange
    PresignedUrlRequest request = new PresignedUrlRequest();
    request.setFileName("test.jpg");
    request.setContentType("image/jpeg");

    PresignedUrlResponse response = new PresignedUrlResponse();
    response.setUrl("https://s3-bucket.amazonaws.com/test.jpg");
    response.setKey("12345_test.jpg");

    when(s3Service.generatePresignedUrl(any(PresignedUrlRequest.class))).thenReturn(response);

    // Act & Assert
    mockMvc.perform(post("/api/s3/generate-presigned-url")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.url", is("https://s3-bucket.amazonaws.com/test.jpg")))
            .andExpect(jsonPath("$.key", is("12345_test.jpg")));
  }

  // Note: Since the MockMvc doesn't easily handle multipart file uploads in the standard way,
  // this test is just a placeholder. In a real application, you'd need to use the MockMvcRequestBuilders.multipart() method
  // and configure it correctly for your specific implementation.
  @Test
  public void testUploadFile_Success() throws Exception {
    // Arrange
    MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
    );

    when(s3Service.uploadFile(any())).thenReturn("12345_test.jpg");

    // This is a simplified test - in a real scenario you might need a different approach depending on your implementation
    mockMvc.perform(multipart("/api/s3/upload")
                    .file(file))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string("12345_test.jpg"));
  }

  @Test
  public void testDeleteFile_Success() throws Exception {
    // Arrange
    String fileKey = "12345_test.jpg";

    // Mock the service method to do nothing (void return type)
    doNothing().when(s3Service).deleteFile(fileKey);

    // Act & Assert
    mockMvc.perform(delete("/api/s3/delete/{key}", fileKey))
            .andDo(print())
            .andExpect(status().isNoContent());

    // Verify that the service method was called once with the correct parameter
    verify(s3Service, times(1)).deleteFile(fileKey);
  }

  @Test
  public void testDeleteFile_Error() throws Exception {
    // Arrange
    String fileKey = "nonexistent_file.jpg";

    // Mock the service to throw FileStorageException when trying to delete a non-existent file
    doThrow(new FileStorageException("Failed to delete file from S3"))
            .when(s3Service).deleteFile(fileKey);

    // Act & Assert
    mockMvc.perform(delete("/api/s3/delete/{key}", fileKey))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.details.error").value("An unexpected error occurred: Failed to delete file from S3"));

    // Verify that the service method was called once with the correct parameter
    verify(s3Service, times(1)).deleteFile(fileKey);
  }
}