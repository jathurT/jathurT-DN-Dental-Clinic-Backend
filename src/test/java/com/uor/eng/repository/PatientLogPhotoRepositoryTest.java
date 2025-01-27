package com.uor.eng.repository;

import com.uor.eng.model.Dentist;
import com.uor.eng.model.Patient;
import com.uor.eng.model.PatientLog;
import com.uor.eng.model.PatientLogPhoto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PatientLogPhotoRepositoryTest {

  @Autowired
  private PatientLogPhotoRepository patientLogPhotoRepository;

  @Autowired
  private PatientLogRepository patientLogRepository;

  @Autowired
  private PatientRepository patientRepository;

  @Autowired
  private DentistRepository dentistRepository;

  private Patient testPatient;
  private PatientLog testPatientLog;

  @BeforeEach
  void setUp() {
    Dentist testDentist = Dentist.dentistBuilder()
        .email("ktmjathur2001@gmail.com")
        .userName("jathur")
        .firstName("John")
        .password("securePassword@123")
        .nic("200132504295")
        .phoneNumber("0779797254")
        .gender("Male")
        .specialization("Orthodontist")
        .licenseNumber("LIC12345")
        .schedules(Collections.emptyList())
        .build();
    dentistRepository.save(testDentist);

    testPatient = Patient.builder()
        .name("John Doe")
        .email("johndoe@example.com")
        .nic("123456789V")
        .contactNumbers(List.of("0712345678"))
        .build();
    testPatient = patientRepository.save(testPatient);

    testPatientLog = PatientLog.builder()
        .patient(testPatient)
        .description("Patient log description")
        .timestamp(LocalDateTime.now())
        .actionType("ACTION")
        .dentist(testDentist)
        .build();
    testPatientLog = patientLogRepository.save(testPatientLog);

    PatientLogPhoto photo1 = PatientLogPhoto.builder()
        .patientLog(testPatientLog)
        .s3Key("photo1_key")
        .description("Front view photo.")
        .timestamp(LocalDateTime.now())
        .build();

    PatientLogPhoto photo2 = PatientLogPhoto.builder()
        .patientLog(testPatientLog)
        .s3Key("photo2_key")
        .description("Side view photo.")
        .timestamp(LocalDateTime.now())
        .build();

    patientLogPhotoRepository.save(photo1);
    patientLogPhotoRepository.save(photo2);
  }

  @AfterEach
  void tearDown() {
    patientLogPhotoRepository.deleteAll();
    patientLogRepository.deleteAll();
    patientRepository.deleteAll();
    dentistRepository.deleteAll();
  }

  @Test
  @DisplayName("Test find by patient ID")
  @Order(1)
  void testFindByPatientId() {
    List<PatientLogPhoto> photos = patientLogPhotoRepository.findByPatientId(testPatient.getId());

    assertThat(photos).hasSize(2);
    assertThat(photos.get(0).getPatientLog().getPatient().getId()).isEqualTo(testPatient.getId());
    assertThat(photos.get(1).getPatientLog().getPatient().getId()).isEqualTo(testPatient.getId());
  }

  @Test
  @DisplayName("Test find by patient log ID")
  @Order(2)
  void testFindByPatientLogId() {
    List<PatientLogPhoto> photos = patientLogPhotoRepository.findByPatientLogId(testPatientLog.getId());

    assertThat(photos).hasSize(2);
    assertThat(photos.get(0).getPatientLog().getId()).isEqualTo(testPatientLog.getId());
    assertThat(photos.get(1).getPatientLog().getId()).isEqualTo(testPatientLog.getId());
  }
}
