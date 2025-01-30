package com.uor.eng.service.impl;

import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.AppRole;
import com.uor.eng.model.Dentist;
import com.uor.eng.model.Role;
import com.uor.eng.payload.dentist.CreateDentistDTO;
import com.uor.eng.payload.dentist.DentistResponseDTO;
import com.uor.eng.payload.dentist.UpdateDentistRequest;
import com.uor.eng.repository.DentistRepository;
import com.uor.eng.repository.RoleRepository;
import com.uor.eng.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DentistServiceImplTest {

  @InjectMocks
  private DentistServiceImpl dentistService;

  @Mock
  private DentistRepository dentistRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private ModelMapper modelMapper;

  @Mock
  private PasswordEncoder passwordEncoder;

  private Dentist dentist;
  private CreateDentistDTO createDentistDTO;
  private UpdateDentistRequest updateDentistRequest;
  private DentistResponseDTO dentistResponseDTO;
  private CreateDentistDTO updateDentistDTO;
  private Role role;

  @BeforeEach
  void setUp() {
    dentist = Dentist.dentistBuilder()
        .userName("johndoe")
        .email("john.doe@example.com")
        .password("encodedPassword")
        .firstName("John")
        .nic("123456789V")
        .phoneNumber("0771234567")
        .specialization("Orthodontics")
        .licenseNumber("DENT1234")
        .roles(Set.of())
        .build();

    createDentistDTO = new CreateDentistDTO(
        "johndoe",
        "john.doe@example.com",
        "Male",
        "Password@123",
        "John",
        "Orthodontics",
        "DENT1234",
        "123456789V",
        "0771234567"
    );

    updateDentistRequest = new UpdateDentistRequest(
        "johndoe",
        "john.doe@example.com",
        "Male",
        "John",
        "Orthodontics",
        "DENT1234",
        "123456789V",
        "0771234567"
    );

    dentistResponseDTO = DentistResponseDTO.builder()
        .id(1L)
        .userName("johndoe")
        .email("john.doe@example.com")
        .gender("Male")
        .firstName("John")
        .specialization("Orthodontics")
        .licenseNumber("DENT1234")
        .nic("123456789V")
        .phoneNumber("0771234567")
        .roles(Set.of(AppRole.ROLE_DENTIST.name()))
        .build();

    updateDentistDTO = new CreateDentistDTO(
        "existingUsername",
        "updated.email@example.com",
        "Female",
        "NewPassword@123",
        "Jane",
        "Periodontics",
        "DENT5678",
        "987654321V",
        "0777654321"
    );
    role = new Role();
    role.setRoleName(AppRole.ROLE_DENTIST);
  }

  @AfterEach
  void tearDown() {
    reset(dentistRepository, userRepository, roleRepository, modelMapper, passwordEncoder);
  }

  @Test
  @DisplayName("Create Dentist - Success")
  @Order(1)
  void createDentist_ShouldSaveAndReturnDentistResponse() {
    when(userRepository.existsByUserName(createDentistDTO.getUserName())).thenReturn(false);
    when(userRepository.existsByEmail(createDentistDTO.getEmail())).thenReturn(false);
    when(roleRepository.findByRoleName(AppRole.ROLE_DENTIST)).thenReturn(Optional.of(role));
    when(modelMapper.map(createDentistDTO, Dentist.class)).thenReturn(dentist);
    when(passwordEncoder.encode(createDentistDTO.getPassword())).thenReturn("encodedPassword");
    when(dentistRepository.save(any(Dentist.class))).thenReturn(dentist);
    when(modelMapper.map(dentist, DentistResponseDTO.class)).thenReturn(dentistResponseDTO);

    DentistResponseDTO result = dentistService.createDentist(createDentistDTO);

    assertNotNull(result);
    assertEquals(createDentistDTO.getUserName(), result.getUserName());
    verify(dentistRepository, times(1)).save(any(Dentist.class));
  }

  @Test
  @DisplayName("Create Dentist - Username Exists - BadRequest")
  @Order(2)
  void createDentist_ShouldThrowBadRequest_WhenUsernameExists() {
    when(userRepository.existsByUserName(createDentistDTO.getUserName())).thenReturn(true);

    assertThrows(BadRequestException.class, () -> dentistService.createDentist(createDentistDTO));
    verify(dentistRepository, never()).save(any(Dentist.class));
  }

  @Test
  @DisplayName("Create Dentist - Email Exists - BadRequest")
  @Order(3)
  void createDentist_ShouldThrowBadRequest_WhenEmailExists() {
    when(userRepository.existsByEmail(createDentistDTO.getEmail())).thenReturn(true);

    assertThrows(BadRequestException.class, () -> dentistService.createDentist(createDentistDTO));
    verify(dentistRepository, never()).save(any(Dentist.class));
  }

  @Test
  @DisplayName("Get All Dentists - Success")
  @Order(4)
  void getAllDentists_ShouldReturnListOfDentistResponseDTO() {
    when(dentistRepository.findAll()).thenReturn(Arrays.asList(dentist));
    when(modelMapper.map(dentist, DentistResponseDTO.class)).thenReturn(dentistResponseDTO);

    var result = dentistService.getAllDentists();

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(dentistRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Get All Dentists - Empty - ResourceNotFound")
  @Order(5)
  void getAllDentists_ShouldThrowResourceNotFoundException_WhenNoDentistsExist() {
    when(dentistRepository.findAll()).thenReturn(Collections.emptyList());

    assertThrows(ResourceNotFoundException.class, () -> dentistService.getAllDentists());
    verify(dentistRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Get Dentist By ID - Success")
  @Order(6)
  void getDentistById_ShouldReturnDentistResponseDTO() {
    when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
    when(modelMapper.map(dentist, DentistResponseDTO.class)).thenReturn(dentistResponseDTO);

    DentistResponseDTO result = dentistService.getDentistById(1L);

    assertNotNull(result);
    assertEquals(dentistResponseDTO.getId(), result.getId());
    verify(dentistRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Get Dentist By ID - Not Found - ResourceNotFound")
  @Order(7)
  void getDentistById_ShouldThrowResourceNotFoundException_WhenDentistDoesNotExist() {
    when(dentistRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> dentistService.getDentistById(1L));
  }

  @Test
  @DisplayName("Delete Dentist - Success")
  @Order(8)
  void deleteDentist_ShouldDeleteDentist_WhenDentistExists() {
    when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));

    dentistService.deleteDentist(1L);

    verify(dentistRepository, times(1)).delete(dentist);
  }

  @Test
  @DisplayName("Delete Dentist - Not Found - ResourceNotFound")
  @Order(9)
  void deleteDentist_ShouldThrowResourceNotFoundException_WhenDentistDoesNotExist() {
    when(dentistRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> dentistService.deleteDentist(1L));
    verify(dentistRepository, never()).delete(any(Dentist.class));
  }

  @Test
  @DisplayName("Edit Dentist - Success")
  @Order(10)
  void editDentist_ShouldUpdateAndReturnDentistResponseDTO() {
    when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
    when(userRepository.existsByUserName(updateDentistRequest.getUserName())).thenReturn(false);
    when(userRepository.existsByEmail(updateDentistRequest.getEmail())).thenReturn(false);
    when(dentistRepository.save(any(Dentist.class))).thenReturn(dentist);
    when(modelMapper.map(dentist, DentistResponseDTO.class)).thenReturn(dentistResponseDTO);

    DentistResponseDTO result = dentistService.editDentist(1L, updateDentistRequest);

    assertNotNull(result);
    assertEquals(updateDentistRequest.getUserName(), result.getUserName());
    verify(dentistRepository, times(1)).save(any(Dentist.class));
  }

  @Test
  @DisplayName("Update Dentist - Success")
  @Order(11)
  void updateDentist_ShouldUpdateAndReturnDentistResponseDTO() {
    // Arrange
    Long dentistId = 1L;

    Dentist updatedDentist = Dentist.dentistBuilder()
        .userName(updateDentistDTO.getUserName())
        .email(updateDentistDTO.getEmail())
        .password(passwordEncoder.encode(updateDentistDTO.getPassword()))
        .firstName(updateDentistDTO.getFirstName())
        .nic(updateDentistDTO.getNic())
        .phoneNumber(updateDentistDTO.getPhoneNumber())
        .specialization(updateDentistDTO.getSpecialization())
        .licenseNumber(updateDentistDTO.getLicenseNumber())
        .roles(Set.of())
        .build();

    DentistResponseDTO updatedDentistResponseDTO = DentistResponseDTO.builder()
        .id(dentistId)
        .userName(updateDentistDTO.getUserName())
        .email(updateDentistDTO.getEmail())
        .gender(updateDentistDTO.getGender())
        .firstName(updateDentistDTO.getFirstName())
        .specialization(updateDentistDTO.getSpecialization())
        .licenseNumber(updateDentistDTO.getLicenseNumber())
        .nic(updateDentistDTO.getNic())
        .phoneNumber(updateDentistDTO.getPhoneNumber())
        .roles(Set.of(AppRole.ROLE_DENTIST.name()))
        .build();

    when(dentistRepository.findById(dentistId)).thenReturn(Optional.of(dentist));
    when(userRepository.existsByUserName(updateDentistDTO.getUserName())).thenReturn(false);
    when(userRepository.existsByEmail(updateDentistDTO.getEmail())).thenReturn(false);
    when(passwordEncoder.encode(updateDentistDTO.getPassword())).thenReturn("newEncodedPassword");
    when(dentistRepository.save(any(Dentist.class))).thenReturn(updatedDentist);
    when(modelMapper.map(updatedDentist, DentistResponseDTO.class)).thenReturn(updatedDentistResponseDTO);

    // Act
    DentistResponseDTO result = dentistService.updateDentist(dentistId, updateDentistDTO);

    // Assert
    assertNotNull(result);
    assertEquals(updateDentistDTO.getUserName(), result.getUserName());
    assertEquals(updateDentistDTO.getEmail(), result.getEmail());
    assertEquals(updateDentistDTO.getFirstName(), result.getFirstName());
    assertEquals(updateDentistDTO.getSpecialization(), result.getSpecialization());
    assertEquals(updateDentistDTO.getLicenseNumber(), result.getLicenseNumber());
    assertEquals(updateDentistDTO.getNic(), result.getNic());
    assertEquals(updateDentistDTO.getPhoneNumber(), result.getPhoneNumber());
    verify(dentistRepository, times(1)).save(any(Dentist.class));
  }

  @Test
  @DisplayName("Update Dentist - Username Exists - BadRequest")
  @Order(12)
  void updateDentist_ShouldThrowBadRequest_WhenUsernameExists() {
    Long dentistId = 1L;

    when(dentistRepository.findById(dentistId)).thenReturn(Optional.of(dentist));
    when(userRepository.existsByUserName(updateDentistDTO.getUserName())).thenReturn(true);

    assertThrows(BadRequestException.class, () -> dentistService.updateDentist(dentistId, updateDentistDTO));
    verify(dentistRepository, never()).save(any(Dentist.class));
  }

  @Test
  @DisplayName("Update Dentist - Email Exists - BadRequest")
  @Order(13)
  void updateDentist_ShouldThrowBadRequest_WhenEmailExists() {
    Long dentistId = 1L;

    when(dentistRepository.findById(dentistId)).thenReturn(Optional.of(dentist));
    when(userRepository.existsByEmail(updateDentistDTO.getEmail())).thenReturn(true);

    assertThrows(BadRequestException.class, () -> dentistService.updateDentist(dentistId, updateDentistDTO));
    verify(dentistRepository, never()).save(any(Dentist.class));
  }
}
