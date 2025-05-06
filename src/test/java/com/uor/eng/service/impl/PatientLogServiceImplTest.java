package com.uor.eng.service.impl;

import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.exceptions.UnauthorizedAccessException;
import com.uor.eng.model.Dentist;
import com.uor.eng.model.Patient;
import com.uor.eng.model.PatientLog;
import com.uor.eng.model.PatientLogPhoto;
import com.uor.eng.payload.patient.logs.*;
import com.uor.eng.repository.DentistRepository;
import com.uor.eng.repository.PatientLogPhotoRepository;
import com.uor.eng.repository.PatientLogRepository;
import com.uor.eng.repository.PatientRepository;
import com.uor.eng.util.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PatientLogServiceImplTest {

  @Mock
  private PatientRepository patientRepository;

  @Mock
  private PatientLogRepository patientLogRepository;

  @Mock
  private PatientLogPhotoRepository patientLogPhotoRepository;

  @Mock
  private DentistRepository dentistRepository;

  @Mock
  private S3Service s3Service;

  private PatientLogServiceImpl patientLogService;

  private Patient testPatient;
  private Dentist testDentist;
  private PatientLog testPatientLog;
  private PatientLogPhoto testPhoto1;
  private PatientLogPhoto testPhoto2;
  private List<PatientLogPhoto> testPhotos;

  @BeforeEach
  void setUp() {
    patientLogService = new PatientLogServiceImpl(
            patientRepository,
            patientLogRepository,
            patientLogPhotoRepository,
            dentistRepository,
            s3Service
    );

    // Initialize test data
    testPatient = new Patient();
    testPatient.setId(1L);
    testPatient.setName("Test Patient");
    testPatient.setEmail("patient@test.com");
    testPatient.setNic("123456789V");
    testPatient.setContactNumbers(List.of("1234567890"));

    testDentist = new Dentist();
    testDentist.setUserId(1L);
    testDentist.setFirstName("Test Dentist");
    testDentist.setEmail("dentist@test.com");

    testPatientLog = new PatientLog();
    testPatientLog.setId(1L);
    testPatientLog.setPatient(testPatient);
    testPatientLog.setDentist(testDentist);
    testPatientLog.setActionType("Checkup");
    testPatientLog.setDescription("Routine dental checkup");
    testPatientLog.setTimestamp(LocalDateTime.now());

    testPhoto1 = new PatientLogPhoto();
    testPhoto1.setId(1L);
    testPhoto1.setPatientLog(testPatientLog);
    testPhoto1.setS3Key("test-photo-1.jpg");
    testPhoto1.setDescription("Test photo 1");
    testPhoto1.setTimestamp(LocalDateTime.now());

    testPhoto2 = new PatientLogPhoto();
    testPhoto2.setId(2L);
    testPhoto2.setPatientLog(testPatientLog);
    testPhoto2.setS3Key("test-photo-2.jpg");
    testPhoto2.setDescription("Test photo 2");
    testPhoto2.setTimestamp(LocalDateTime.now());

    testPhotos = new ArrayList<>(Arrays.asList(testPhoto1, testPhoto2));
    testPatientLog.setPatientLogPhotos(testPhotos);
  }

  @Test
  void createPatientLog_Success() {
    // Given
    PatientLogRequestNoPhotos request = new PatientLogRequestNoPhotos();
    request.setActionType("Checkup");
    request.setDescription("Routine dental checkup");
    request.setDentistId(1L);

    when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
    when(dentistRepository.findById(1L)).thenReturn(Optional.of(testDentist));
    when(patientLogRepository.save(any(PatientLog.class))).thenReturn(testPatientLog);

    // When
    PatientLogResponse response = patientLogService.createPatientLog(1L, request);

    // Then
    assertNotNull(response);
    assertEquals("Checkup", response.getActionType());
    assertEquals("Routine dental checkup", response.getDescription());
    assertEquals("Test Dentist", response.getDentistName());
    verify(patientRepository).findById(1L);
    verify(dentistRepository).findById(1L);
    verify(patientLogRepository).save(any(PatientLog.class));
  }

  @Test
  void createPatientLog_PatientNotFound() {
    // Given
    PatientLogRequestNoPhotos request = new PatientLogRequestNoPhotos();
    request.setActionType("Checkup");
    request.setDescription("Routine dental checkup");
    request.setDentistId(1L);

    when(patientRepository.findById(1L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class,
            () -> patientLogService.createPatientLog(1L, request));
    verify(patientRepository).findById(1L);
    verify(dentistRepository, never()).findById(anyLong());
    verify(patientLogRepository, never()).save(any(PatientLog.class));
  }

  @Test
  void createPatientLog_DentistNotFound() {
    // Given
    PatientLogRequestNoPhotos request = new PatientLogRequestNoPhotos();
    request.setActionType("Checkup");
    request.setDescription("Routine dental checkup");
    request.setDentistId(1L);

    when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
    when(dentistRepository.findById(1L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class,
            () -> patientLogService.createPatientLog(1L, request));
    verify(patientRepository).findById(1L);
    verify(dentistRepository).findById(1L);
    verify(patientLogRepository, never()).save(any(PatientLog.class));
  }

  @Test
  void getPatientLogs_Success() {
    // Given
    List<PatientLog> patientLogs = List.of(testPatientLog);

    when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
    when(patientLogRepository.findByPatientId(1L)).thenReturn(patientLogs);
    when(s3Service.getFileUrl(anyString())).thenReturn("https://test-url.com/photo.jpg");

    // When
    List<PatientLogResponse> responses = patientLogService.getPatientLogs(1L);

    // Then
    assertNotNull(responses);
    assertEquals(1, responses.size());
    PatientLogResponse response = responses.get(0);
    assertEquals("Checkup", response.getActionType());
    assertEquals("Routine dental checkup", response.getDescription());
    assertEquals("Test Dentist", response.getDentistName());
    assertEquals(2, response.getPhotos().size());
    verify(patientRepository).findById(1L);
    verify(patientLogRepository).findByPatientId(1L);
  }

  @Test
  void getPatientLogs_PatientNotFound() {
    // Given
    when(patientRepository.findById(1L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class,
            () -> patientLogService.getPatientLogs(1L));
    verify(patientRepository).findById(1L);
    verify(patientLogRepository, never()).findByPatientId(anyLong());
  }

  @Test
  void getPatientLog_Success() {
    // Given
    when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.of(testPatientLog));
    when(s3Service.getFileUrl(anyString())).thenReturn("https://test-url.com/photo.jpg");

    // When
    PatientLogResponse response = patientLogService.getPatientLog(1L, 1L);

    // Then
    assertNotNull(response);
    assertEquals("Checkup", response.getActionType());
    assertEquals("Routine dental checkup", response.getDescription());
    assertEquals("Test Dentist", response.getDentistName());
    assertEquals(2, response.getPhotos().size());
    verify(patientRepository).findById(1L);
    verify(patientLogRepository).findByIdAndPatientId(1L, 1L);
  }

  @Test
  void getPatientLog_PatientNotFound() {
    // Given
    when(patientRepository.findById(1L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class,
            () -> patientLogService.getPatientLog(1L, 1L));
    verify(patientRepository).findById(1L);
    verify(patientLogRepository, never()).findByIdAndPatientId(anyLong(), anyLong());
  }

  @Test
  void getPatientLog_LogNotFound() {
    // Given
    when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class,
            () -> patientLogService.getPatientLog(1L, 1L));
    verify(patientRepository).findById(1L);
    verify(patientLogRepository).findByIdAndPatientId(1L, 1L);
  }

  @Test
  void deletePatientLog_Success() {
    // Given
    when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.of(testPatientLog));
    when(patientLogPhotoRepository.findByPatientLogId(1L)).thenReturn(testPhotos);
    doNothing().when(s3Service).deleteFile(anyString());
    doNothing().when(patientLogPhotoRepository).deleteAll(anyList());
    doNothing().when(patientLogRepository).delete(any(PatientLog.class));

    // When
    patientLogService.deletePatientLog(1L, 1L);

    // Then
    verify(patientRepository).findById(1L);
    verify(patientLogRepository).findByIdAndPatientId(1L, 1L);
    verify(patientLogPhotoRepository).findByPatientLogId(1L);
    verify(s3Service, times(2)).deleteFile(anyString());
    verify(patientLogPhotoRepository).deleteAll(testPhotos);
    verify(patientLogRepository).delete(testPatientLog);
  }

  @Test
  void deletePatientLog_PatientNotFound() {
    // Given
    when(patientRepository.findById(1L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class,
            () -> patientLogService.deletePatientLog(1L, 1L));
    verify(patientRepository).findById(1L);
    verify(patientLogRepository, never()).findByIdAndPatientId(anyLong(), anyLong());
    verify(patientLogPhotoRepository, never()).findByPatientLogId(anyLong());
    verify(s3Service, never()).deleteFile(anyString());
    verify(patientLogPhotoRepository, never()).deleteAll(anyList());
    verify(patientLogRepository, never()).delete(any(PatientLog.class));
  }

  @Test
  void deletePatientLog_LogNotFound() {
    // Given
    when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class,
            () -> patientLogService.deletePatientLog(1L, 1L));
    verify(patientRepository).findById(1L);
    verify(patientLogRepository).findByIdAndPatientId(1L, 1L);
    verify(patientLogPhotoRepository, never()).findByPatientLogId(anyLong());
    verify(s3Service, never()).deleteFile(anyString());
    verify(patientLogPhotoRepository, never()).deleteAll(anyList());
    verify(patientLogRepository, never()).delete(any(PatientLog.class));
  }

  @Test
  void updatePatientLog_Success_WithBasicUpdate() {
    // Given
    PatientLogUpdateRequest request = new PatientLogUpdateRequest();
    request.setActionType("Treatment");
    request.setDescription("Updated description");

    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.of(testPatientLog));
    when(patientLogRepository.save(any(PatientLog.class))).thenReturn(testPatientLog);
    when(s3Service.getFileUrl(anyString())).thenReturn("https://test-url.com/photo.jpg");

    // When
    PatientLogResponse response = patientLogService.updatePatientLog(1L, 1L, request);

    // Then
    assertNotNull(response);
    assertEquals("Treatment", testPatientLog.getActionType());
    assertEquals("Updated description", testPatientLog.getDescription());
    verify(patientLogRepository).findByIdAndPatientId(1L, 1L);
    verify(patientLogRepository).save(testPatientLog);
  }

  @Test
  void updatePatientLog_Success_WithPhotoDelete() {
    // Given
    PatientLogUpdateRequest request = new PatientLogUpdateRequest();
    request.setActionType("Treatment");
    request.setDescription("Updated description");
    request.setPhotosToDelete(List.of(1L));

    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.of(testPatientLog));
    when(patientLogPhotoRepository.findById(1L)).thenReturn(Optional.of(testPhoto1));
    doNothing().when(s3Service).deleteFile(anyString());
    doNothing().when(patientLogPhotoRepository).delete(any(PatientLogPhoto.class));
    when(patientLogRepository.save(any(PatientLog.class))).thenReturn(testPatientLog);
    when(s3Service.getFileUrl(anyString())).thenReturn("https://test-url.com/photo.jpg");

    // When
    PatientLogResponse response = patientLogService.updatePatientLog(1L, 1L, request);

    // Then
    assertNotNull(response);
    assertEquals("Treatment", testPatientLog.getActionType());
    assertEquals("Updated description", testPatientLog.getDescription());
    verify(patientLogRepository).findByIdAndPatientId(1L, 1L);
    verify(patientLogPhotoRepository).findById(1L);
    verify(s3Service).deleteFile(testPhoto1.getS3Key());
    verify(patientLogPhotoRepository).delete(testPhoto1);
    verify(patientLogRepository).save(testPatientLog);
  }

  @Test
  void updatePatientLog_Success_WithNewPhotos() {
    // Given
    PatientLogUpdateRequest request = new PatientLogUpdateRequest();
    request.setActionType("Treatment");
    request.setDescription("Updated description");
    request.setNewPhotoKeys(List.of("new-photo.jpg"));

    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.of(testPatientLog));
    when(patientLogPhotoRepository.save(any(PatientLogPhoto.class))).thenReturn(new PatientLogPhoto());
    when(patientLogRepository.save(any(PatientLog.class))).thenReturn(testPatientLog);
    when(s3Service.getFileUrl(anyString())).thenReturn("https://test-url.com/photo.jpg");

    // When
    PatientLogResponse response = patientLogService.updatePatientLog(1L, 1L, request);

    // Then
    assertNotNull(response);
    assertEquals("Treatment", testPatientLog.getActionType());
    assertEquals("Updated description", testPatientLog.getDescription());
    verify(patientLogRepository).findByIdAndPatientId(1L, 1L);
    verify(patientLogPhotoRepository).save(any(PatientLogPhoto.class));
    verify(patientLogRepository).save(testPatientLog);
  }

  @Test
  void updatePatientLog_LogNotFound() {
    // Given
    PatientLogUpdateRequest request = new PatientLogUpdateRequest();
    request.setActionType("Treatment");
    request.setDescription("Updated description");

    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class,
            () -> patientLogService.updatePatientLog(1L, 1L, request));
    verify(patientLogRepository).findByIdAndPatientId(1L, 1L);
    verify(patientLogRepository, never()).save(any(PatientLog.class));
  }

  @Test
  void updatePatientLog_PhotoNotFound() {
    // Given
    PatientLogUpdateRequest request = new PatientLogUpdateRequest();
    request.setActionType("Treatment");
    request.setDescription("Updated description");
    request.setPhotosToDelete(List.of(3L)); // Non-existent photo ID

    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.of(testPatientLog));
    when(patientLogPhotoRepository.findById(3L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class,
            () -> patientLogService.updatePatientLog(1L, 1L, request));
    verify(patientLogRepository).findByIdAndPatientId(1L, 1L);
    verify(patientLogPhotoRepository).findById(3L);
    verify(patientLogRepository, never()).save(any(PatientLog.class));
  }

  @Test
  void associatePhotosWithLog_Success() {
    // Given
    AssociatePhotosRequest request = new AssociatePhotosRequest();
    request.setS3Keys(List.of("new-photo-1.jpg", "new-photo-2.jpg"));
    request.setDescriptions(List.of("Description 1", "Description 2"));

    when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.of(testPatientLog));
    when(patientLogPhotoRepository.save(any(PatientLogPhoto.class))).thenAnswer(invocation -> {
      PatientLogPhoto photo = invocation.getArgument(0);
      photo.setId(3L); // Assign some ID
      return photo;
    });
    when(patientLogRepository.save(any(PatientLog.class))).thenReturn(testPatientLog);
    when(s3Service.getFileUrl(anyString())).thenReturn("https://test-url.com/photo.jpg");

    // When
    List<PatientLogPhotoResponse> responses = patientLogService.associatePhotosWithLog(1L, 1L, request);

    // Then
    assertNotNull(responses);
    assertEquals(2, responses.size());
    verify(patientRepository).findById(1L);
    verify(patientLogRepository).findByIdAndPatientId(1L, 1L);
    verify(patientLogPhotoRepository, times(2)).save(any(PatientLogPhoto.class));
    verify(patientLogRepository).save(testPatientLog);
    verify(s3Service, times(2)).getFileUrl(anyString());
  }

  @Test
  void associatePhotosWithLog_PatientNotFound() {
    // Given
    AssociatePhotosRequest request = new AssociatePhotosRequest();
    request.setS3Keys(List.of("new-photo.jpg"));

    when(patientRepository.findById(1L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class,
            () -> patientLogService.associatePhotosWithLog(1L, 1L, request));
    verify(patientRepository).findById(1L);
    verify(patientLogRepository, never()).findByIdAndPatientId(anyLong(), anyLong());
    verify(patientLogPhotoRepository, never()).save(any(PatientLogPhoto.class));
  }

  @Test
  void associatePhotosWithLog_LogNotFound() {
    // Given
    AssociatePhotosRequest request = new AssociatePhotosRequest();
    request.setS3Keys(List.of("new-photo.jpg"));

    when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class,
            () -> patientLogService.associatePhotosWithLog(1L, 1L, request));
    verify(patientRepository).findById(1L);
    verify(patientLogRepository).findByIdAndPatientId(1L, 1L);
    verify(patientLogPhotoRepository, never()).save(any(PatientLogPhoto.class));
  }

  @Test
  void associatePhotosWithLog_NoS3Keys() {
    // Given
    AssociatePhotosRequest request = new AssociatePhotosRequest();
    request.setS3Keys(List.of());

    when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.of(testPatientLog));

    // When & Then
    assertThrows(IllegalArgumentException.class,
            () -> patientLogService.associatePhotosWithLog(1L, 1L, request));
    verify(patientRepository).findById(1L);
    verify(patientLogRepository).findByIdAndPatientId(1L, 1L);
    verify(patientLogPhotoRepository, never()).save(any(PatientLogPhoto.class));
  }

  @Test
  void getPhotos_Success() {
    // Given
    when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.of(testPatientLog));
    when(patientLogPhotoRepository.findByPatientLogId(1L)).thenReturn(testPhotos);
    when(s3Service.getFileUrl(anyString())).thenReturn("https://test-url.com/photo.jpg");

    // When
    List<PatientLogPhotoResponse> responses = patientLogService.getPhotos(1L, 1L);

    // Then
    assertNotNull(responses);
    assertEquals(2, responses.size());
    verify(patientRepository).findById(1L);
    verify(patientLogRepository).findByIdAndPatientId(1L, 1L);
    verify(patientLogPhotoRepository).findByPatientLogId(1L);
    verify(s3Service, times(2)).getFileUrl(anyString());
  }

  @Test
  void getPhotos_PatientNotFound() {
    // Given
    when(patientRepository.findById(1L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class,
            () -> patientLogService.getPhotos(1L, 1L));
    verify(patientRepository).findById(1L);
    verify(patientLogRepository, never()).findByIdAndPatientId(anyLong(), anyLong());
    verify(patientLogPhotoRepository, never()).findByPatientLogId(anyLong());
  }

  @Test
  void getPhotos_LogNotFound() {
    // Given
    when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class,
            () -> patientLogService.getPhotos(1L, 1L));
    verify(patientRepository).findById(1L);
    verify(patientLogRepository).findByIdAndPatientId(1L, 1L);
    verify(patientLogPhotoRepository, never()).findByPatientLogId(anyLong());
  }

  @Test
  void deletePhoto_Success() {
    // Given
    when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.of(testPatientLog));
    when(patientLogPhotoRepository.findById(1L)).thenReturn(Optional.of(testPhoto1));
    doNothing().when(s3Service).deleteFile(anyString());
    doNothing().when(patientLogPhotoRepository).delete(any(PatientLogPhoto.class));
    when(patientLogRepository.save(any(PatientLog.class))).thenReturn(testPatientLog);

    // When
    patientLogService.deletePhoto(1L, 1L, 1L);

    // Then
    verify(patientRepository).findById(1L);
    verify(patientLogRepository).findByIdAndPatientId(1L, 1L);
    verify(patientLogPhotoRepository).findById(1L);
    verify(s3Service).deleteFile(testPhoto1.getS3Key());
    verify(patientLogPhotoRepository).delete(testPhoto1);
    verify(patientLogRepository).save(testPatientLog);
  }

  @Test
  void deletePhoto_PatientNotFound() {
    // Given
    when(patientRepository.findById(1L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class,
            () -> patientLogService.deletePhoto(1L, 1L, 1L));
    verify(patientRepository).findById(1L);
    verify(patientLogRepository, never()).findByIdAndPatientId(anyLong(), anyLong());
    verify(patientLogPhotoRepository, never()).findById(anyLong());
    verify(s3Service, never()).deleteFile(anyString());
    verify(patientLogPhotoRepository, never()).delete(any(PatientLogPhoto.class));
  }

  @Test
  void deletePhoto_LogNotFound() {
    // Given
    when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class,
            () -> patientLogService.deletePhoto(1L, 1L, 1L));
    verify(patientRepository).findById(1L);
    verify(patientLogRepository).findByIdAndPatientId(1L, 1L);
    verify(patientLogPhotoRepository, never()).findById(anyLong());
    verify(s3Service, never()).deleteFile(anyString());
    verify(patientLogPhotoRepository, never()).delete(any(PatientLogPhoto.class));
  }

  @Test
  void deletePhoto_PhotoNotFound() {
    // Given
    when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.of(testPatientLog));
    when(patientLogPhotoRepository.findById(3L)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class,
            () -> patientLogService.deletePhoto(1L, 1L, 3L));
    verify(patientRepository).findById(1L);
    verify(patientLogRepository).findByIdAndPatientId(1L, 1L);
    verify(patientLogPhotoRepository).findById(3L);
    verify(s3Service, never()).deleteFile(anyString());
    verify(patientLogPhotoRepository, never()).delete(any(PatientLogPhoto.class));
  }

  @Test
  void deletePhoto_UnauthorizedAccess() {
    // Given
    // Create a different log to simulate unauthorized access
    PatientLog differentLog = new PatientLog();
    differentLog.setId(2L);

    testPhoto1.setPatientLog(differentLog); // Set different log for the photo

    when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.of(testPatientLog));
    when(patientLogPhotoRepository.findById(1L)).thenReturn(Optional.of(testPhoto1));

    // When & Then
    assertThrows(UnauthorizedAccessException.class,
            () -> patientLogService.deletePhoto(1L, 1L, 1L));
    verify(patientRepository).findById(1L);
    verify(patientLogRepository).findByIdAndPatientId(1L, 1L);
    verify(patientLogPhotoRepository).findById(1L);
    verify(s3Service, never()).deleteFile(anyString());
    verify(patientLogPhotoRepository, never()).delete(any(PatientLogPhoto.class));
  }
}