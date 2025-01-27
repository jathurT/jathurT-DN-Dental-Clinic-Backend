package com.uor.eng.repository;

import com.uor.eng.model.Dentist;
import com.uor.eng.model.Patient;
import com.uor.eng.model.PatientLog;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PatientLogRepositoryTest {

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
        .actionType("Checkup")
        .description("Routine dental checkup.")
        .timestamp(LocalDateTime.now())
        .dentist(testDentist)
        .build();
    testPatientLog = patientLogRepository.save(testPatientLog);
  }

  @AfterEach
  void tearDown() {
    patientLogRepository.deleteAll();
    patientRepository.deleteAll();
    dentistRepository.deleteAll();
  }

  @Test
  @DisplayName("Test find by patient ID")
  @Order(1)
  void testFindByPatientId() {
    List<PatientLog> logs = patientLogRepository.findByPatientId(testPatient.getId());

    assertThat(logs).hasSize(1);
    assertThat(logs.get(0).getPatient().getId()).isEqualTo(testPatient.getId());
    assertThat(logs.get(0).getActionType()).isEqualTo("Checkup");
  }

  @Test
  @DisplayName("Test find by ID and patient ID")
  @Order(2)
  void testFindByIdAndPatientId() {
    var log = patientLogRepository.findByIdAndPatientId(testPatientLog.getId(), testPatient.getId());

    assertThat(log).isPresent();
    assertThat(log.get()).isEqualTo(testPatientLog);
    assertThat(((PatientLog) log.get()).getActionType()).isEqualTo("Checkup");
  }
}
