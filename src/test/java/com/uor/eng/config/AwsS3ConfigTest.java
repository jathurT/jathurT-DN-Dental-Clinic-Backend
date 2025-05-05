package com.uor.eng.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AwsS3ConfigTest {

  @InjectMocks
  private AwsS3Config awsS3Config;

  @Test
  public void testS3ClientCreation() {
    // Arrange
    String accessKeyId = "test-access-key";
    String secretKey = "test-secret-key";
    String region = "us-east-1";

    ReflectionTestUtils.setField(awsS3Config, "accessKeyId", accessKeyId);
    ReflectionTestUtils.setField(awsS3Config, "secretKey", secretKey);
    ReflectionTestUtils.setField(awsS3Config, "region", region);

    // Act
    S3Client s3Client = awsS3Config.s3Client();

    // Assert
    assertNotNull(s3Client, "S3Client should not be null");
  }

  @Test
  public void testS3ClientWithMockedBuilder() {
    // Arrange
    String accessKeyId = "test-access-key";
    String secretKey = "test-secret-key";
    String region = "us-east-1";

    ReflectionTestUtils.setField(awsS3Config, "accessKeyId", accessKeyId);
    ReflectionTestUtils.setField(awsS3Config, "secretKey", secretKey);
    ReflectionTestUtils.setField(awsS3Config, "region", region);

    S3ClientBuilder mockBuilder = mock(S3ClientBuilder.class);
    S3Client mockClient = mock(S3Client.class);

    try (MockedStatic<S3Client> mockedStatic = Mockito.mockStatic(S3Client.class)) {
      mockedStatic.when(S3Client::builder).thenReturn(mockBuilder);
      when(mockBuilder.credentialsProvider(any())).thenReturn(mockBuilder);
      when(mockBuilder.region(any())).thenReturn(mockBuilder);
      when(mockBuilder.build()).thenReturn(mockClient);

      // Act
      S3Client result = awsS3Config.s3Client();

      // Assert
      assertSame(mockClient, result, "Should return the mocked S3Client");

      // Verify interactions
      mockedStatic.verify(S3Client::builder);
      verify(mockBuilder).credentialsProvider(any(StaticCredentialsProvider.class));
      verify(mockBuilder).region(eq(Region.of(region)));
      verify(mockBuilder).build();
    }
  }

  @Test
  public void testAwsCredentialsCreation() {
    // Arrange
    String accessKeyId = "test-access-key";
    String secretKey = "test-secret-key";
    String region = "us-east-1";

    ReflectionTestUtils.setField(awsS3Config, "accessKeyId", accessKeyId);
    ReflectionTestUtils.setField(awsS3Config, "secretKey", secretKey);
    ReflectionTestUtils.setField(awsS3Config, "region", region);

    try (MockedStatic<AwsBasicCredentials> mockedCredentials = Mockito.mockStatic(AwsBasicCredentials.class);
         MockedStatic<StaticCredentialsProvider> mockedProvider = Mockito.mockStatic(StaticCredentialsProvider.class);
         MockedStatic<S3Client> mockedS3Client = Mockito.mockStatic(S3Client.class)) {

      // Setup mocks
      AwsBasicCredentials mockCreds = mock(AwsBasicCredentials.class);
      StaticCredentialsProvider mockCredProvider = mock(StaticCredentialsProvider.class);
      S3ClientBuilder mockBuilder = mock(S3ClientBuilder.class);
      S3Client mockClient = mock(S3Client.class);

      mockedCredentials.when(() -> AwsBasicCredentials.create(accessKeyId, secretKey)).thenReturn(mockCreds);
      mockedProvider.when(() -> StaticCredentialsProvider.create(mockCreds)).thenReturn(mockCredProvider);
      mockedS3Client.when(S3Client::builder).thenReturn(mockBuilder);

      when(mockBuilder.credentialsProvider(mockCredProvider)).thenReturn(mockBuilder);
      when(mockBuilder.region(Region.of(region))).thenReturn(mockBuilder);
      when(mockBuilder.build()).thenReturn(mockClient);

      // Act
      S3Client result = awsS3Config.s3Client();

      // Assert
      assertSame(mockClient, result, "Should return the mocked S3Client");

      // Verify interactions
      mockedCredentials.verify(() -> AwsBasicCredentials.create(accessKeyId, secretKey));
      mockedProvider.verify(() -> StaticCredentialsProvider.create(mockCreds));
      verify(mockBuilder).credentialsProvider(mockCredProvider);
      verify(mockBuilder).region(Region.of(region));
    }
  }

  @Test
  public void testRegionSelection() {
    // Arrange
    String accessKeyId = "test-access-key";
    String secretKey = "test-secret-key";
    String[] testRegions = {"us-east-1", "us-west-1", "eu-west-1", "ap-southeast-1"};

    ReflectionTestUtils.setField(awsS3Config, "accessKeyId", accessKeyId);
    ReflectionTestUtils.setField(awsS3Config, "secretKey", secretKey);

    for (String regionName : testRegions) {
      // Setup test for each region
      ReflectionTestUtils.setField(awsS3Config, "region", regionName);

      try (MockedStatic<S3Client> mockedStatic = Mockito.mockStatic(S3Client.class)) {
        S3ClientBuilder mockBuilder = mock(S3ClientBuilder.class);
        S3Client mockClient = mock(S3Client.class);

        mockedStatic.when(S3Client::builder).thenReturn(mockBuilder);
        when(mockBuilder.credentialsProvider(any())).thenReturn(mockBuilder);
        when(mockBuilder.region(any())).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockClient);

        // Act
        awsS3Config.s3Client();

        // Verify correct region was used
        verify(mockBuilder).region(eq(Region.of(regionName)));
      }
    }
  }

  @Test
  public void testNullPropertyHandling() {
    // Arrange - set properties to null
    ReflectionTestUtils.setField(awsS3Config, "accessKeyId", null);
    ReflectionTestUtils.setField(awsS3Config, "secretKey", null);
    ReflectionTestUtils.setField(awsS3Config, "region", null);

    // Act & Assert
    Exception exception = assertThrows(
            NullPointerException.class,
            () -> awsS3Config.s3Client(),
            "Should throw NullPointerException when properties are null"
    );

    assertNotNull(exception.getMessage(), "Exception message should not be null");
  }

  @Test
  public void testEmptyPropertyHandling() {
    // Arrange - set properties to empty strings
    ReflectionTestUtils.setField(awsS3Config, "accessKeyId", "");
    ReflectionTestUtils.setField(awsS3Config, "secretKey", "");
    ReflectionTestUtils.setField(awsS3Config, "region", "");

    // AWS SDK validates that credentials are not blank
    Exception exception = assertThrows(
            NullPointerException.class,
            () -> awsS3Config.s3Client(),
            "Should throw NullPointerException when access key is blank"
    );

    assertTrue(exception.getMessage().contains("Access key ID cannot be blank"),
            "Exception message should mention that access key ID cannot be blank");
  }

  @Test
  public void testInvalidRegionHandling() {
    // Arrange
    String accessKeyId = "test-access-key";
    String secretKey = "test-secret-key";
    String invalidRegion = "invalid-region";

    ReflectionTestUtils.setField(awsS3Config, "accessKeyId", accessKeyId);
    ReflectionTestUtils.setField(awsS3Config, "secretKey", secretKey);
    ReflectionTestUtils.setField(awsS3Config, "region", invalidRegion);

    // It seems AWS SDK doesn't validate region names at construction time
    // but only when making actual requests. Let's test that a client is created
    // with the specified region, invalid or not.
    S3Client client = awsS3Config.s3Client();
    assertNotNull(client, "Should create a client even with an invalid region");
  }
}