package com.uor.eng.repository;

import com.uor.eng.model.Patient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PatientRepositoryTest {

  @Autowired
  private PatientRepository patientRepository;

  private Patient patient1;

  @BeforeEach
  void setUp() {
    patient1 = Patient.builder()
        .name("John Doe")
        .email("johndoe@example.com")
        .nic("123456789V")
        .contactNumbers(List.of("0123456789"))
        .build();

    Patient patient2 = Patient.builder()
            .name("Jane Doe")
            .email("janedoe@example.com")
            .nic("987654321V")
            .contactNumbers(List.of("0123456789"))
            .build();

    patientRepository.save(patient1);
    patientRepository.save(patient2);
  }

  @AfterEach
  void tearDown() {
    patientRepository.deleteAll();
  }

  @Test
  @DisplayName("Test find the patient by email")
  @Order(1)
  void testFindByEmail() {
    Optional<Patient> foundPatient = patientRepository.findByEmail("johndoe@example.com");
    assertThat(foundPatient).isPresent();
    assertThat(foundPatient.get().getName()).isEqualTo("John Doe");
  }

  @Test
  @DisplayName("Test user exists by email")
  @Order(2)
  void testExistsByEmail() {
    boolean exists = patientRepository.existsByEmail("janedoe@example.com");
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("Test find the patient by NIC")
  @Order(3)
  void testFindByNic() {
    Optional<Patient> foundPatient = patientRepository.findByNic("987654321V");
    assertThat(foundPatient).isPresent();
    assertThat(foundPatient.get().getName()).isEqualTo("Jane Doe");
  }

  @Test
  @DisplayName("Test user exists by NIC")
  @Order(4)
  void testExistsByNic() {
    boolean exists = patientRepository.existsByNic("123456789V");
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("Test find all patients")
  @Order(5)
  void testFindAll() {
    List<Patient> patients = patientRepository.findAll();
    assertThat(patients).hasSize(2);
  }

  @Test
  @DisplayName("Test delete patient")
  @Order(6)
  void testDeletePatient() {
    patientRepository.delete(patient1);
    Optional<Patient> deletedPatient = patientRepository.findByEmail("johndoe@example.com");
    assertThat(deletedPatient).isEmpty();
  }
}
