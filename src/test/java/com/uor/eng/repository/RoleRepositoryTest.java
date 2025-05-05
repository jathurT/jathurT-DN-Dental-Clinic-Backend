package com.uor.eng.repository;

import com.uor.eng.model.AppRole;
import com.uor.eng.model.Role;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RoleRepositoryTest {

  @Autowired
  private RoleRepository roleRepository;

  private Role adminRole;

  @BeforeEach
  void setUp() {
    Role dentistRole = new Role(AppRole.ROLE_DENTIST);
    adminRole = new Role(AppRole.ROLE_ADMIN);
    Role receptionistRole = new Role(AppRole.ROLE_RECEPTIONIST);

    roleRepository.save(dentistRole);
    roleRepository.save(adminRole);
    roleRepository.save(receptionistRole);
  }

  @AfterEach
  void tearDown() {
    roleRepository.deleteAll();
  }

  @Test
  @DisplayName("Test find by role name - Dentist")
  @Order(1)
  void testFindByRoleName_Dentist() {
    Optional<Role> foundRole = roleRepository.findByRoleName(AppRole.ROLE_DENTIST);
    assertThat(foundRole).isPresent();
    assertThat(foundRole.get().getRoleName()).isEqualTo(AppRole.ROLE_DENTIST);
  }

  @Test
  @DisplayName("Test find by role name - Admin")
  @Order(2)
  void testFindByRoleName_Admin() {
    Optional<Role> foundRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN);
    assertThat(foundRole).isPresent();
    assertThat(foundRole.get().getRoleName()).isEqualTo(AppRole.ROLE_ADMIN);
  }

  @Test
  @DisplayName("Test find by role name - Receptionist")
  @Order(3)
  void testFindByRoleName_Receptionist() {
    Optional<Role> foundRole = roleRepository.findByRoleName(AppRole.ROLE_RECEPTIONIST);
    assertThat(foundRole).isPresent();
    assertThat(foundRole.get().getRoleName()).isEqualTo(AppRole.ROLE_RECEPTIONIST);
  }

  @Test
  @DisplayName("Test find by role name - Not Found")
  @Order(4)
  void testFindByRoleName_NotFound() {
    Optional<Role> foundRole = roleRepository.findByRoleName(null);
    assertThat(foundRole).isEmpty();
  }

  @Test
  @DisplayName("Test save role")
  @Order(5)
  void testSaveRole() {
    Role newRole = new Role(AppRole.ROLE_RECEPTIONIST);
    Role savedRole = roleRepository.save(newRole);

    assertThat(savedRole).isNotNull();
    assertThat(savedRole.getRoleName()).isEqualTo(AppRole.ROLE_RECEPTIONIST);
  }

  @Test
  @DisplayName("Test delete role")
  @Order(6)
  void testDeleteRole() {
    roleRepository.delete(adminRole);
    Optional<Role> deletedRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN);
    assertThat(deletedRole).isEmpty();
  }
}
