package com.uor.eng.service.impl;

import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.Patient;
import com.uor.eng.model.PatientLog;
import com.uor.eng.model.PatientLogPhoto;
import com.uor.eng.payload.patient.CreatePatientRequest;
import com.uor.eng.payload.patient.PatientResponse;
import com.uor.eng.repository.PatientRepository;
import com.uor.eng.util.S3Service;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PatientServiceImplTest {

  @InjectMocks
  private PatientServiceImpl patientService;

  @Mock
  private ModelMapper modelMapper;

  @Mock
  private PatientRepository patientRepository;

  @Mock
  private S3Service s3Service;

  private Patient patient;
  private PatientLog patientLog;
  private PatientLogPhoto patientLogPhoto;

  @BeforeEach
  void setUp() {
    patient = new Patient();
    patient.setId(1L);
    patient.setName("John Doe");
    patient.setEmail("john@example.com");
    patient.setNic("123456789V");
    patient.setContactNumbers(Collections.singletonList("1234567890"));

    patientLog = new PatientLog();
    patientLog.setId(1L);
    patientLog.setActionType("Checkup");
    patientLog.setDescription("Routine Checkup");

    patientLogPhoto = new PatientLogPhoto();
    patientLogPhoto.setId(1L);
    patientLogPhoto.setS3Key("photo-key");
    patientLogPhoto.setDescription("Before treatment");
  }

  @Test
  @DisplayName("Test create patient - Success")
  @Order(1)
  void createPatient_ShouldCreatePatient_WhenValidRequest() {
    CreatePatientRequest request = new CreatePatientRequest("John Doe", "john@example.com", "123456789V", List.of("1234567890"));

    when(patientRepository.existsByEmail(request.getEmail())).thenReturn(false);
    when(patientRepository.existsByNic(request.getNic())).thenReturn(false);
    when(patientRepository.save(any(Patient.class))).thenReturn(patient);

    PatientResponse response = patientService.createPatient(request);

    assertNotNull(response);
    assertEquals("John Doe", response.getName());
    verify(patientRepository, times(1)).save(any(Patient.class));
  }

  @Test
  @DisplayName("Test create patient - Email already in use")
  @Order(2)
  void createPatient_ShouldThrowBadRequestException_WhenEmailExists() {
    CreatePatientRequest request = new CreatePatientRequest("John Doe", "john@example.com", "123456789V", List.of("1234567890"));

    when(patientRepository.existsByEmail(request.getEmail())).thenReturn(true);

    assertThrows(BadRequestException.class, () -> patientService.createPatient(request));
    verify(patientRepository, never()).save(any(Patient.class));
  }

  @Test
  @DisplayName("Test get all patients - Success")
  @Order(3)
  void getAllPatients_ShouldReturnListOfPatients() {
    when(patientRepository.findAll()).thenReturn(Collections.singletonList(patient));

    List<PatientResponse> responses = patientService.getAllPatients();

    assertNotNull(responses);
    assertEquals(1, responses.size());
    assertEquals("John Doe", responses.get(0).getName());
    verify(patientRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Test get patient by ID - Success")
  @Order(4)
  void getPatientById_ShouldReturnPatient_WhenIdExists() {
    when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

    PatientResponse response = patientService.getPatientById(1L);

    assertNotNull(response);
    assertEquals("John Doe", response.getName());
    verify(patientRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Test get patient by ID - Patient Not Found")
  @Order(5)
  void getPatientById_ShouldThrowResourceNotFoundException_WhenPatientDoesNotExist() {
    when(patientRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> patientService.getPatientById(1L));
  }

  @Test
  @DisplayName("Test update patient - Success")
  @Order(6)
  void updatePatient_ShouldUpdatePatient_WhenValidData() {
    CreatePatientRequest request = new CreatePatientRequest("Updated Name", "updated@example.com", "987654321V", List.of("9876543210"));

    when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
    when(patientRepository.existsByEmail(request.getEmail())).thenReturn(false);
    when(patientRepository.existsByNic(request.getNic())).thenReturn(false);
    when(patientRepository.save(any(Patient.class))).thenReturn(patient);

    PatientResponse response = patientService.updatePatient(1L, request);

    assertNotNull(response);
    assertEquals("Updated Name", response.getName());
    verify(patientRepository, times(1)).save(any(Patient.class));
  }

  @Test
  @DisplayName("Test update patient - Email already in use")
  void updatePatient_ShouldThrowBadRequestException_WhenEmailExists() {
    CreatePatientRequest request = new CreatePatientRequest(
        "Updated Name",
        "different@example.com",
        "123456789V",
        List.of("9876543210")
    );

    // Mock existing patient
    Patient existingPatient = new Patient();
    existingPatient.setId(1L);
    existingPatient.setEmail("john@example.com");

    when(patientRepository.findById(1L)).thenReturn(Optional.of(existingPatient));
    when(patientRepository.existsByEmail(request.getEmail())).thenReturn(true);

    assertThrows(BadRequestException.class, () -> patientService.updatePatient(1L, request));
    verify(patientRepository, never()).save(any(Patient.class));
  }

  @Test
  @DisplayName("Test delete patient - Success")
  @Order(8)
  void deletePatient_ShouldDeletePatient_WhenValidId() {
    when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

    patientService.deletePatient(1L);

    verify(patientRepository, times(1)).delete(patient);
  }

  @Test
  @DisplayName("Test delete patient - Patient Not Found")
  @Order(9)
  void deletePatient_ShouldThrowResourceNotFoundException_WhenPatientDoesNotExist() {
    when(patientRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> patientService.deletePatient(1L));
    verify(patientRepository, never()).delete(any(Patient.class));
  }
}
