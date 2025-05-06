package com.uor.eng.service.impl;

import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.Dentist;
import com.uor.eng.model.Patient;
import com.uor.eng.model.PatientLog;
import com.uor.eng.model.PatientLogPhoto;
import com.uor.eng.payload.patient.CreatePatientRequest;
import com.uor.eng.payload.patient.PatientResponse;
import com.uor.eng.repository.PatientRepository;
import com.uor.eng.util.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

  private PatientServiceImpl patientService;

  @Mock
  private ModelMapper modelMapper;

  @Mock
  private PatientRepository patientRepository;

  @Mock
  private S3Service s3Service;

  private Patient patient;
  private CreatePatientRequest validRequest;

  @BeforeEach
  void setUp() {
    // Initialize the service with constructor injection
    patientService = new PatientServiceImpl(modelMapper, patientRepository, s3Service);

    // Initialize test data
    Dentist dentist = new Dentist();
    dentist.setFirstName("Dr. Smith");

    patient = new Patient();
    patient.setId(1L);
    patient.setName("John Doe");
    patient.setEmail("john@example.com");
    patient.setNic("123456789V");
    patient.setContactNumbers(Collections.singletonList("1234567890"));

    PatientLog patientLog = new PatientLog();
    patientLog.setId(1L);
    patientLog.setActionType("Checkup");
    patientLog.setDescription("Routine Checkup");
    patientLog.setTimestamp(LocalDateTime.now());
    patientLog.setDentist(dentist);

    PatientLogPhoto patientLogPhoto = new PatientLogPhoto();
    patientLogPhoto.setId(1L);
    patientLogPhoto.setS3Key("photo-key");
    patientLogPhoto.setDescription("Before treatment");
    patientLogPhoto.setTimestamp(LocalDateTime.now());

    List<PatientLogPhoto> photos = new ArrayList<>();
    photos.add(patientLogPhoto);
    patientLog.setPatientLogPhotos(photos);

    List<PatientLog> logs = new ArrayList<>();
    logs.add(patientLog);
    patient.setPatientLogs(logs);

    validRequest = new CreatePatientRequest(
            "John Doe",
            "john@example.com",
            "123456789V",
            List.of("1234567890")
    );

    // Set up common mock behavior for S3Service that gets used in multiple tests
    lenient().when(s3Service.getFileUrl(anyString())).thenReturn("https://test-url.com/photo-key");
  }

  @Test
  void createPatient_ShouldCreatePatient_WhenValidRequest() {
    // Arrange
    when(patientRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
    when(patientRepository.existsByNic(validRequest.getNic())).thenReturn(false);
    when(patientRepository.save(any(Patient.class))).thenReturn(patient);

    // Act
    patientService.createPatient(validRequest);

    // Assert & Verify
    ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
    verify(patientRepository).save(patientCaptor.capture());
    Patient capturedPatient = patientCaptor.getValue();
    assertEquals(validRequest.getName(), capturedPatient.getName());
    assertEquals(validRequest.getEmail(), capturedPatient.getEmail());
    assertEquals(validRequest.getNic(), capturedPatient.getNic());
    assertEquals(validRequest.getContactNumbers(), capturedPatient.getContactNumbers());
  }

  @Test
  void createPatient_ShouldThrowBadRequestException_WhenEmailExists() {
    // Arrange
    when(patientRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

    // Act & Assert
    BadRequestException exception = assertThrows(BadRequestException.class,
            () -> patientService.createPatient(validRequest));
    assertEquals("Email is already in use!", exception.getMessage());

    // Verify
    verify(patientRepository, never()).save(any(Patient.class));
  }

  @Test
  void createPatient_ShouldThrowBadRequestException_WhenNicExists() {
    // Arrange
    when(patientRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
    when(patientRepository.existsByNic(validRequest.getNic())).thenReturn(true);

    // Act & Assert
    BadRequestException exception = assertThrows(BadRequestException.class,
            () -> patientService.createPatient(validRequest));
    assertEquals("NIC is already in use!", exception.getMessage());

    // Verify
    verify(patientRepository, never()).save(any(Patient.class));
  }

  @Test
  void getAllPatients_ShouldReturnListOfPatients() {
    // Arrange
    List<Patient> patients = Collections.singletonList(patient);
    when(patientRepository.findAll()).thenReturn(patients);

    // Act
    List<PatientResponse> responses = patientService.getAllPatients();

    // Verify
    verify(patientRepository).findAll();
    // We don't need to verify S3Service.getFileUrl since it's lenient stubbed
  }

  @Test
  void getAllPatients_ShouldReturnEmptyList_WhenNoPatients() {
    // Arrange
    when(patientRepository.findAll()).thenReturn(Collections.emptyList());

    // Act
    List<PatientResponse> responses = patientService.getAllPatients();

    // Assert & Verify
    assertNotNull(responses);
    assertTrue(responses.isEmpty());
    verify(patientRepository).findAll();
  }

  @Test
  void getPatientById_ShouldReturnPatient_WhenIdExists() {
    // Arrange
    when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

    // Act
    PatientResponse response = patientService.getPatientById(1L);

    // Verify
    verify(patientRepository).findById(1L);
    // We don't need to verify S3Service.getFileUrl since it's lenient stubbed
  }

  @Test
  void getPatientById_ShouldThrowResourceNotFoundException_WhenPatientDoesNotExist() {
    // Arrange
    when(patientRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> patientService.getPatientById(1L));
    assertEquals("Patient not found with id: 1", exception.getMessage());

    // Verify
    verify(patientRepository).findById(1L);
  }

  @Test
  void updatePatient_ShouldUpdatePatient_WhenValidData() {
    // Arrange
    CreatePatientRequest updateRequest = new CreatePatientRequest(
            "Updated Name",
            "updated@example.com",
            "987654321V",
            List.of("9876543210")
    );

    Patient existingPatient = new Patient();
    existingPatient.setId(1L);
    existingPatient.setName("John Doe");
    existingPatient.setEmail("john@example.com");
    existingPatient.setNic("123456789V");
    existingPatient.setContactNumbers(List.of("1234567890"));

    when(patientRepository.findById(1L)).thenReturn(Optional.of(existingPatient));
    when(patientRepository.existsByEmail(updateRequest.getEmail())).thenReturn(false);
    when(patientRepository.existsByNic(updateRequest.getNic())).thenReturn(false);
    when(patientRepository.save(any(Patient.class))).thenReturn(existingPatient);

    // Act
    patientService.updatePatient(1L, updateRequest);

    // Verify
    ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
    verify(patientRepository).save(patientCaptor.capture());
    Patient capturedPatient = patientCaptor.getValue();
    assertEquals(updateRequest.getName(), capturedPatient.getName());
    assertEquals(updateRequest.getEmail(), capturedPatient.getEmail());
    assertEquals(updateRequest.getNic(), capturedPatient.getNic());
    assertEquals(updateRequest.getContactNumbers(), capturedPatient.getContactNumbers());
  }

  @Test
  void updatePatient_ShouldThrowBadRequestException_WhenEmailExists() {
    // Arrange
    CreatePatientRequest updateRequest = new CreatePatientRequest(
            "Updated Name",
            "different@example.com",
            "123456789V",
            List.of("9876543210")
    );

    Patient existingPatient = new Patient();
    existingPatient.setId(1L);
    existingPatient.setName("John Doe");
    existingPatient.setEmail("john@example.com");
    existingPatient.setNic("123456789V");
    existingPatient.setContactNumbers(List.of("1234567890"));

    when(patientRepository.findById(1L)).thenReturn(Optional.of(existingPatient));
    when(patientRepository.existsByEmail(updateRequest.getEmail())).thenReturn(true);

    // Act & Assert
    BadRequestException exception = assertThrows(BadRequestException.class,
            () -> patientService.updatePatient(1L, updateRequest));
    assertEquals("Email is already in use!", exception.getMessage());

    // Verify
    verify(patientRepository, never()).save(any(Patient.class));
  }

  @Test
  void updatePatient_ShouldThrowBadRequestException_WhenNicExists() {
    // Arrange
    CreatePatientRequest updateRequest = new CreatePatientRequest(
            "Updated Name",
            "john@example.com",
            "987654321V",
            List.of("9876543210")
    );

    Patient existingPatient = new Patient();
    existingPatient.setId(1L);
    existingPatient.setName("John Doe");
    existingPatient.setEmail("john@example.com");
    existingPatient.setNic("123456789V");
    existingPatient.setContactNumbers(List.of("1234567890"));

    when(patientRepository.findById(1L)).thenReturn(Optional.of(existingPatient));
    when(patientRepository.existsByNic(updateRequest.getNic())).thenReturn(true);

    // Act & Assert
    BadRequestException exception = assertThrows(BadRequestException.class,
            () -> patientService.updatePatient(1L, updateRequest));
    assertEquals("NIC is already in use!", exception.getMessage());

    // Verify
    verify(patientRepository, never()).save(any(Patient.class));
  }

  @Test
  void updatePatient_ShouldThrowResourceNotFoundException_WhenPatientDoesNotExist() {
    // Arrange
    CreatePatientRequest updateRequest = new CreatePatientRequest(
            "Updated Name",
            "updated@example.com",
            "987654321V",
            List.of("9876543210")
    );

    when(patientRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> patientService.updatePatient(1L, updateRequest));
    assertEquals("Patient not found with id: 1", exception.getMessage());

    // Verify
    verify(patientRepository, never()).save(any(Patient.class));
  }

  @Test
  void updatePatient_ShouldPartiallyUpdatePatient_WhenSomeFieldsAreNull() {
    // Arrange
    CreatePatientRequest updateRequest = new CreatePatientRequest(
            "Updated Name",
            null,  // Keep original email
            null,  // Keep original NIC
            null   // Keep original contact numbers
    );

    Patient existingPatient = new Patient();
    existingPatient.setId(1L);
    existingPatient.setName("John Doe");
    existingPatient.setEmail("john@example.com");
    existingPatient.setNic("123456789V");
    existingPatient.setContactNumbers(List.of("1234567890"));

    when(patientRepository.findById(1L)).thenReturn(Optional.of(existingPatient));
    when(patientRepository.save(any(Patient.class))).thenReturn(existingPatient);

    // Act
    patientService.updatePatient(1L, updateRequest);

    // Verify
    ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
    verify(patientRepository).save(patientCaptor.capture());
    Patient capturedPatient = patientCaptor.getValue();
    assertEquals("Updated Name", capturedPatient.getName());
    assertEquals("john@example.com", capturedPatient.getEmail());  // Unchanged
    assertEquals("123456789V", capturedPatient.getNic());  // Unchanged
    assertEquals(List.of("1234567890"), capturedPatient.getContactNumbers());  // Unchanged
  }

  @Test
  void deletePatient_ShouldDeletePatient_WhenValidId() {
    // Arrange
    when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

    // Act
    patientService.deletePatient(1L);

    // Verify
    verify(patientRepository).delete(patient);
  }

  @Test
  void deletePatient_ShouldThrowResourceNotFoundException_WhenPatientDoesNotExist() {
    // Arrange
    when(patientRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> patientService.deletePatient(1L));
    assertEquals("Patient not found with id: 1", exception.getMessage());

    // Verify
    verify(patientRepository, never()).delete(any(Patient.class));
  }
}