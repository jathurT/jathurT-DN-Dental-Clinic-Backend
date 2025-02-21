package com.uor.eng.service.impl;

import com.uor.eng.exceptions.ResourceNotFoundException;
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
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PatientLogServiceImplTest {

  @InjectMocks
  private PatientLogServiceImpl patientLogService;

  @Mock
  private PatientRepository patientRepository;

  @Mock
  private DentistRepository dentistRepository;

  @Mock
  private PatientLogRepository patientLogRepository;

  @Mock
  private PatientLogPhotoRepository patientLogPhotoRepository;

  @Mock
  private S3Service s3Service;

  private Patient patient;
  private Dentist dentist;
  private PatientLog patientLog;
  private PatientLogPhoto patientLogPhoto;

  @BeforeEach
  void setUp() {
    patient = new Patient();
    patient.setId(1L);

    dentist = new Dentist();
    dentist.setUserId(1L);
    dentist.setFirstName("Dr. John");

    patientLog = new PatientLog();
    patientLog.setId(1L);
    patientLog.setPatient(patient);
    patientLog.setDentist(dentist);
    patientLog.setActionType("Checkup");
    patientLog.setDescription("Routine Checkup");
    patientLog.setTimestamp(LocalDateTime.now());

    patientLogPhoto = new PatientLogPhoto();
    patientLogPhoto.setId(1L);
    patientLogPhoto.setS3Key("photo-key");
    patientLogPhoto.setDescription("Before treatment");
    patientLogPhoto.setTimestamp(LocalDateTime.now());
  }

  @Test
  @DisplayName("Test create patient log - Success")
  @Order(1)
  void createPatientLog_ShouldCreateLog_WhenValidPatientAndDentistExist() {
    PatientLogRequestNoPhotos request = new PatientLogRequestNoPhotos("Checkup", "Routine Checkup", 1L);

    when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
    when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
    when(patientLogRepository.save(any(PatientLog.class))).thenReturn(patientLog);

    PatientLogResponse response = patientLogService.createPatientLog(1L, request);

    assertNotNull(response);
    assertEquals("Checkup", response.getActionType());
    verify(patientLogRepository, times(1)).save(any(PatientLog.class));
  }

  @Test
  @DisplayName("Test get patient logs - Success")
  @Order(2)
  void getPatientLogs_ShouldReturnLogs_WhenPatientExists() {
    when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
    when(patientLogRepository.findByPatientId(1L)).thenReturn(Collections.singletonList(patientLog));
    when(patientLogPhotoRepository.findByPatientId(1L)).thenReturn(Collections.singletonList(patientLogPhoto));
    when(s3Service.getFileUrl("photo-key")).thenReturn("https://s3/photo-key");

    List<PatientLogResponse> responses = patientLogService.getPatientLogs(1L);

    assertNotNull(responses);
    assertEquals(1, responses.size());
    verify(patientLogRepository, times(1)).findByPatientId(1L);
  }

  @Test
  @DisplayName("Test get patient logs - Patient Not Found")
  @Order(3)
  void getPatientLogs_ShouldThrowResourceNotFoundException_WhenPatientDoesNotExist() {
    when(patientRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> patientLogService.getPatientLogs(1L));
  }

  @Test
  @DisplayName("Test get patient log by ID - Success")
  @Order(4)
  void getPatientLog_ShouldReturnLog_WhenValidPatientAndLogId() {
    when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.of(patientLog));
    when(patientLogPhotoRepository.findByPatientId(1L)).thenReturn(Collections.singletonList(patientLogPhoto));
    when(s3Service.getFileUrl("photo-key")).thenReturn("https://s3/photo-key");

    PatientLogResponse response = patientLogService.getPatientLog(1L, 1L);

    assertNotNull(response);
    assertEquals("Checkup", response.getActionType());
    verify(patientLogRepository, times(1)).findByIdAndPatientId(1L, 1L);
  }

  @Test
  @DisplayName("Test delete patient log - Success")
  @Order(5)
  void deletePatientLog_ShouldDeleteLogAndPhotos_WhenValidPatientAndLogId() {
    when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.of(patientLog));
    when(patientLogPhotoRepository.findByPatientLogId(1L)).thenReturn(Collections.singletonList(patientLogPhoto));

    patientLogService.deletePatientLog(1L, 1L);

    verify(s3Service, times(1)).deleteFile("photo-key");
    verify(patientLogPhotoRepository, times(1)).deleteAll(any());
    verify(patientLogRepository, times(1)).delete(patientLog);
  }

  @Test
  @DisplayName("Test update patient log - Success")
  @Order(6)
  void updatePatientLog_ShouldUpdateLog_WhenValidData() {
    PatientLogUpdateRequest request = new PatientLogUpdateRequest("Surgery", "Wisdom tooth removal", null, null);

    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.of(patientLog));
    when(patientLogRepository.save(any(PatientLog.class))).thenReturn(patientLog);

    PatientLogResponse response = patientLogService.updatePatientLog(1L, 1L, request);

    assertNotNull(response);
    assertEquals("Surgery", response.getActionType());
    assertEquals("Wisdom tooth removal", response.getDescription());
    verify(patientLogRepository, times(1)).save(any(PatientLog.class));
  }

  @Test
  @DisplayName("Test associate photos with log - Success")
  @Order(7)
  void associatePhotosWithLog_ShouldAssociatePhotos_WhenValidPatientAndLogId() {
    AssociatePhotosRequest request = new AssociatePhotosRequest(List.of("photo-key"), List.of("Before surgery"));

    when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.of(patientLog));
    when(s3Service.getFileUrl("photo-key")).thenReturn("https://s3/photo-key");

    List<PatientLogPhotoResponse> responses = patientLogService.associatePhotosWithLog(1L, 1L, request);

    assertNotNull(responses);
    assertEquals(1, responses.size());
    assertEquals("Before surgery", responses.get(0).getDescription());
    verify(patientLogPhotoRepository, times(1)).save(any(PatientLogPhoto.class));
  }

  @Test
  @DisplayName("Test get photos - Success")
  @Order(8)
  void getPhotos_ShouldReturnPhotoList_WhenValidPatientAndLogId() {
    when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
    when(patientLogRepository.findByIdAndPatientId(1L, 1L)).thenReturn(Optional.of(patientLog));
    when(patientLogPhotoRepository.findByPatientLogId(1L)).thenReturn(Collections.singletonList(patientLogPhoto));
    when(s3Service.getFileUrl("photo-key")).thenReturn("https://s3/photo-key");

    List<PatientLogPhotoResponse> responses = patientLogService.getPhotos(1L, 1L);

    assertNotNull(responses);
    assertEquals(1, responses.size());
    assertEquals("Before treatment", responses.get(0).getDescription());
  }
}
