package com.uor.eng.service.impl;

import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.AppRole;
import com.uor.eng.model.Dentist;
import com.uor.eng.model.Role;
import com.uor.eng.model.Schedule;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DentistServiceImplTest {

  @Mock
  private DentistRepository dentistRepository;

  @Mock
  private ModelMapper modelMapper;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private RoleRepository roleRepository;

  @InjectMocks
  private DentistServiceImpl dentistService;

  private Dentist dentist;
  private CreateDentistDTO createDentistDTO;
  private DentistResponseDTO dentistResponseDTO;
  private UpdateDentistRequest updateDentistRequest;
  private Role role;

  @BeforeEach
  void setUp() {
    dentist = Dentist.dentistBuilder()
            .userName("johndoe")
            .email("john@example.com")
            .firstName("John")
            .specialization("Orthodontics")
            .licenseNumber("DENT123")
            .nic("123456789V")
            .phoneNumber("1234567890")
            .roles(new HashSet<>())
            .schedules(new ArrayList<>())
            .build();

    createDentistDTO = new CreateDentistDTO();
    createDentistDTO.setUserName("johndoe");
    createDentistDTO.setEmail("john@example.com");
    createDentistDTO.setPassword("Password123!");
    createDentistDTO.setFirstName("John");
    createDentistDTO.setSpecialization("Orthodontics");
    createDentistDTO.setLicenseNumber("DENT123");
    createDentistDTO.setNic("123456789V");
    createDentistDTO.setPhoneNumber("1234567890");
    createDentistDTO.setGender("Male");

    dentistResponseDTO = new DentistResponseDTO();
    dentistResponseDTO.setId(1L);
    dentistResponseDTO.setUserName("johndoe");
    dentistResponseDTO.setEmail("john@example.com");
    dentistResponseDTO.setRoles(Set.of(AppRole.ROLE_DENTIST.name()));

    updateDentistRequest = new UpdateDentistRequest();
    updateDentistRequest.setUserName("johndoe-updated");
    updateDentistRequest.setEmail("john-updated@example.com");
    updateDentistRequest.setFirstName("John Updated");
    updateDentistRequest.setGender("Male");
    updateDentistRequest.setSpecialization("Pediatric Dentistry");
    updateDentistRequest.setLicenseNumber("DENT456");
    updateDentistRequest.setNic("987654321V");
    updateDentistRequest.setPhoneNumber("9876543210");

    role = new Role();
    role.setRoleName(AppRole.ROLE_DENTIST);
  }

  @Test
  @DisplayName("Create dentist - Success")
  @Order(1)
  void createDentist_Success() {
    // Arrange
    when(userRepository.existsByUserName(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(modelMapper.map(createDentistDTO, Dentist.class)).thenReturn(dentist);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(roleRepository.findByRoleName(AppRole.ROLE_DENTIST)).thenReturn(Optional.of(role));
    when(dentistRepository.save(any(Dentist.class))).thenReturn(dentist);
    when(modelMapper.map(dentist, DentistResponseDTO.class)).thenReturn(dentistResponseDTO);

    // Act
    DentistResponseDTO result = dentistService.createDentist(createDentistDTO);

    // Assert
    assertNotNull(result);
    assertEquals("johndoe", result.getUserName());
    verify(dentistRepository).save(dentist);
  }

  @Test
  @DisplayName("Create dentist - Username already exists")
  @Order(2)
  void createDentist_UsernameExists() {
    // Arrange
    when(userRepository.existsByUserName("johndoe")).thenReturn(true);

    // Act & Assert
    BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dentistService.createDentist(createDentistDTO));
    assertEquals("Username is already taken!", exception.getMessage());
  }

  @Test
  @DisplayName("Create dentist - Email already exists")
  @Order(3)
  void createDentist_EmailExists() {
    // Arrange
    when(userRepository.existsByUserName("johndoe")).thenReturn(false);
    when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

    // Act & Assert
    BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dentistService.createDentist(createDentistDTO));
    assertEquals("Email is already in use!", exception.getMessage());
  }

  @Test
  @DisplayName("Get all dentists - Success")
  @Order(4)
  void getAllDentists_Success() {
    // Arrange
    List<Dentist> dentists = Collections.singletonList(dentist);
    when(dentistRepository.findAll()).thenReturn(dentists);
    when(modelMapper.map(dentist, DentistResponseDTO.class)).thenReturn(dentistResponseDTO);

    // Act
    List<DentistResponseDTO> result = dentistService.getAllDentists();

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
  }

  @Test
  @DisplayName("Get all dentists - Empty")
  @Order(5)
  void getAllDentists_Empty() {
    // Arrange
    when(dentistRepository.findAll()).thenReturn(Collections.emptyList());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> dentistService.getAllDentists());
    assertEquals("No dentists found.", exception.getMessage());
  }

  @Test
  @DisplayName("Get dentist by ID - Success")
  @Order(6)
  void getDentistById_Success() {
    // Arrange
    when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
    when(modelMapper.map(dentist, DentistResponseDTO.class)).thenReturn(dentistResponseDTO);

    // Act
    DentistResponseDTO result = dentistService.getDentistById(1L);

    // Assert
    assertNotNull(result);
    assertEquals("johndoe", result.getUserName());
  }

  @Test
  @DisplayName("Get dentist by ID - Not found")
  @Order(7)
  void getDentistById_NotFound() {
    // Arrange
    when(dentistRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> dentistService.getDentistById(1L));
    assertTrue(exception.getMessage().contains("Dentist not found"));
  }

  @Test
  @DisplayName("Delete dentist - Success")
  @Order(8)
  void deleteDentist_Success() {
    // Arrange
    when(dentistRepository.existsById(1L)).thenReturn(true);
    when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));

    // Act
    dentistService.deleteDentist(1L);

    // Assert
    verify(dentistRepository).deleteById(1L);
  }

  @Test
  @DisplayName("Delete dentist - Not found")
  @Order(9)
  void deleteDentist_NotFound() {
    // Arrange
    when(dentistRepository.existsById(1L)).thenReturn(false);

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> dentistService.deleteDentist(1L));
    assertTrue(exception.getMessage().contains("Dentist not found"));
  }

  @Test
  @DisplayName("Delete dentist - Has schedules")
  @Order(10)
  void deleteDentist_HasSchedules() {
    // Arrange
    when(dentistRepository.existsById(1L)).thenReturn(true);
    List<Schedule> schedules = Collections.singletonList(new Schedule());
    dentist.setSchedules(schedules);
    when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));

    // Act & Assert
    BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dentistService.deleteDentist(1L));
    assertTrue(exception.getMessage().contains("Cannot delete dentist with existing schedules"));
  }

  @Test
  @DisplayName("Update dentist - Success")
  @Order(11)
  void updateDentist_Success() {
    // Arrange
    when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
    when(dentistRepository.save(dentist)).thenReturn(dentist);
    when(modelMapper.map(dentist, DentistResponseDTO.class)).thenReturn(dentistResponseDTO);
    when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPassword");

    // Act
    DentistResponseDTO result = dentistService.updateDentist(1L, createDentistDTO);

    // Assert
    assertNotNull(result);
    verify(dentistRepository).save(dentist);
  }

  @Test
  @DisplayName("Edit dentist - Success")
  @Order(12)
  void editDentist_Success() {
    // Arrange
    when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
    when(dentistRepository.save(dentist)).thenReturn(dentist);
    when(modelMapper.map(dentist, DentistResponseDTO.class)).thenReturn(dentistResponseDTO);

    // Act
    DentistResponseDTO result = dentistService.editDentist(1L, updateDentistRequest);

    // Assert
    assertNotNull(result);
    verify(dentistRepository).save(dentist);
    verify(modelMapper).map(dentist, DentistResponseDTO.class);
  }

  @Test
  @DisplayName("Edit dentist - Not found")
  @Order(13)
  void editDentist_NotFound() {
    // Arrange
    when(dentistRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> dentistService.editDentist(1L, updateDentistRequest));
    assertTrue(exception.getMessage().contains("Dentist not found"));
  }

  @Test
  @DisplayName("Edit dentist - Username taken")
  @Order(14)
  void editDentist_UsernameTaken() {
    // Arrange
    when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
    when(userRepository.existsByUserName(updateDentistRequest.getUserName())).thenReturn(true);

    // Act & Assert
    BadRequestException exception = assertThrows(BadRequestException.class,
            () -> dentistService.editDentist(1L, updateDentistRequest));
    assertEquals("Username is already taken!", exception.getMessage());
  }
}