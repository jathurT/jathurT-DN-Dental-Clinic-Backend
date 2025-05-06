package com.uor.eng.service.impl;

import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.AppRole;
import com.uor.eng.model.Receptionist;
import com.uor.eng.model.Role;
import com.uor.eng.payload.receiptionist.CreateReceptionistDTO;
import com.uor.eng.payload.receiptionist.ReceptionistResponseDTO;
import com.uor.eng.payload.receiptionist.UpdateReceptionistRequest;
import com.uor.eng.repository.ReceptionistRepository;
import com.uor.eng.repository.RoleRepository;
import com.uor.eng.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReceptionistServiceImplTest {

  @InjectMocks
  private ReceptionistServiceImpl receptionistService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ReceptionistRepository receptionistRepository;

  @Mock
  private ModelMapper modelMapper;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private RoleRepository roleRepository;

  private Receptionist receptionist;
  private CreateReceptionistDTO createDTO;
  private ReceptionistResponseDTO responseDTO;
  private Role role;
  private UpdateReceptionistRequest updateRequest;

  @BeforeEach
  void setUp() {
    role = new Role();
    role.setRoleName(AppRole.ROLE_RECEPTIONIST);

    receptionist = new Receptionist();
    receptionist.setUserId(1L);
    receptionist.setUserName("johndoe");
    receptionist.setEmail("john@example.com");
    receptionist.setPassword("encodedPassword");
    receptionist.setGender("Male");
    receptionist.setFirstName("John");
    receptionist.setNic("123456789V");
    receptionist.setPhoneNumber("0771234567");
    receptionist.setRoles(Set.of(role));

    createDTO = CreateReceptionistDTO.builder()
            .userName("johndoe")
            .email("john@example.com")
            .gender("Male")
            .password("Password@123")
            .firstName("John")
            .nic("123456789V")
            .phoneNumber("0771234567")
            .build();

    updateRequest = new UpdateReceptionistRequest();
    updateRequest.setUserName("johndoe");
    updateRequest.setEmail("john@example.com");
    updateRequest.setGender("Male");
    updateRequest.setFirstName("John");
    updateRequest.setNic("123456789V");
    updateRequest.setPhoneNumber("0771234567");

    responseDTO = ReceptionistResponseDTO.builder()
            .id(1L)
            .userName("johndoe")
            .email("john@example.com")
            .gender("Male")
            .firstName("John")
            .nic("123456789V")
            .phoneNumber("0771234567")
            .roles(Set.of("ROLE_RECEPTIONIST"))
            .build();
  }

  @AfterEach
  void tearDown() {
    reset(userRepository, receptionistRepository, roleRepository, modelMapper, passwordEncoder);
  }

  @Test
  @DisplayName("Create Receptionist - Success")
  @Order(1)
  void createReceptionist_Success() {
    when(userRepository.existsByUserName(createDTO.getUserName())).thenReturn(false);
    when(userRepository.existsByEmail(createDTO.getEmail())).thenReturn(false);
    when(roleRepository.findByRoleName(AppRole.ROLE_RECEPTIONIST)).thenReturn(Optional.of(role));
    when(modelMapper.map(createDTO, Receptionist.class)).thenReturn(receptionist);
    when(passwordEncoder.encode(createDTO.getPassword())).thenReturn("encodedPassword");
    when(receptionistRepository.save(receptionist)).thenReturn(receptionist);
    when(modelMapper.map(receptionist, ReceptionistResponseDTO.class)).thenReturn(responseDTO);

    ReceptionistResponseDTO result = receptionistService.createReceptionist(createDTO);

    assertNotNull(result);
    assertEquals("johndoe", result.getUserName());
    assertEquals("john@example.com", result.getEmail());
    verify(receptionistRepository, times(1)).save(receptionist);
  }

  @Test
  @DisplayName("Create Receptionist - Existing Username")
  @Order(2)
  void createReceptionist_ExistingUsername() {
    when(userRepository.existsByUserName(createDTO.getUserName())).thenReturn(true);

    assertThrows(BadRequestException.class, () -> receptionistService.createReceptionist(createDTO));
    verify(receptionistRepository, never()).save(any());
  }

  @Test
  @DisplayName("Create Receptionist - Existing Email")
  @Order(3)
  void createReceptionist_ExistingEmail() {
    when(userRepository.existsByUserName(createDTO.getUserName())).thenReturn(false);
    when(userRepository.existsByEmail(createDTO.getEmail())).thenReturn(true);

    assertThrows(BadRequestException.class, () -> receptionistService.createReceptionist(createDTO));
    verify(receptionistRepository, never()).save(any());
  }

  @Test
  @DisplayName("Get All Receptionists - Success")
  @Order(4)
  void getAllReceptionists_Success() {
    when(receptionistRepository.findAll()).thenReturn(List.of(receptionist));
    when(modelMapper.map(receptionist, ReceptionistResponseDTO.class)).thenReturn(responseDTO);

    List<ReceptionistResponseDTO> result = receptionistService.getAllReceptionists();

    assertFalse(result.isEmpty());
    assertEquals(1, result.size());
    verify(receptionistRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Get All Receptionists - Empty List")
  @Order(5)
  void getAllReceptionists_Empty() {
    when(receptionistRepository.findAll()).thenReturn(Collections.emptyList());

    assertThrows(ResourceNotFoundException.class, () -> receptionistService.getAllReceptionists());
  }

  @Test
  @DisplayName("Get Receptionist By ID - Success")
  @Order(6)
  void getReceptionistById_Success() {
    when(receptionistRepository.findById(1L)).thenReturn(Optional.of(receptionist));
    when(modelMapper.map(receptionist, ReceptionistResponseDTO.class)).thenReturn(responseDTO);

    ReceptionistResponseDTO result = receptionistService.getReceptionistById(1L);

    assertNotNull(result);
    assertEquals(1L, result.getId());
    verify(receptionistRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Get Receptionist By ID - Not Found")
  @Order(7)
  void getReceptionistById_NotFound() {
    when(receptionistRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> receptionistService.getReceptionistById(1L));
  }

  @Test
  @DisplayName("Update Receptionist - Success")
  @Order(8)
  void updateReceptionist_Success() {
    CreateReceptionistDTO updateDTO = CreateReceptionistDTO.builder()
            .userName("newusername")
            .email("new@example.com")
            .gender("Female")
            .password("NewPassword@123")
            .firstName("Jane")
            .nic("987654321V")
            .phoneNumber("0777654321")
            .build();

    Receptionist updatedReceptionist = new Receptionist();
    updatedReceptionist.setUserName("newusername");

    when(receptionistRepository.findById(1L)).thenReturn(Optional.of(receptionist));
    when(userRepository.existsByUserName(updateDTO.getUserName())).thenReturn(false);
    when(userRepository.existsByEmail(updateDTO.getEmail())).thenReturn(false);
    when(passwordEncoder.encode(updateDTO.getPassword())).thenReturn("newEncodedPassword");
    when(receptionistRepository.save(any())).thenReturn(updatedReceptionist);
    when(modelMapper.map(any(), eq(ReceptionistResponseDTO.class))).thenReturn(responseDTO);

    ReceptionistResponseDTO result = receptionistService.updateReceptionist(1L, updateDTO);

    assertNotNull(result);
    verify(receptionistRepository, times(1)).save(any());
  }

  @Test
  @DisplayName("Update Receptionist - Existing Username")
  @Order(9)
  void updateReceptionist_ExistingUsername() {
    CreateReceptionistDTO updateDTO = CreateReceptionistDTO.builder()
            .userName("existinguser")
            .email("john@example.com")
            .build();

    when(receptionistRepository.findById(1L)).thenReturn(Optional.of(receptionist));
    when(userRepository.existsByUserName(updateDTO.getUserName())).thenReturn(true);

    assertThrows(BadRequestException.class, () -> receptionistService.updateReceptionist(1L, updateDTO));
  }

  @Test
  @DisplayName("Update Receptionist - Existing Email")
  @Order(10)
  void updateReceptionist_ExistingEmail() {
    CreateReceptionistDTO updateDTO = CreateReceptionistDTO.builder()
            .userName("johndoe")
            .email("existing@example.com")
            .build();

    when(receptionistRepository.findById(1L)).thenReturn(Optional.of(receptionist));
    when(userRepository.existsByEmail(updateDTO.getEmail())).thenReturn(true);

    assertThrows(BadRequestException.class, () -> receptionistService.updateReceptionist(1L, updateDTO));
  }

  @Test
  @DisplayName("Update Receptionist - Not Found")
  @Order(11)
  void updateReceptionist_NotFound() {
    when(receptionistRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () ->
            receptionistService.updateReceptionist(1L, createDTO));
  }

  @Test
  @DisplayName("Delete Receptionist - Success")
  @Order(12)
  void deleteReceptionist_Success() {
    when(receptionistRepository.findById(1L)).thenReturn(Optional.of(receptionist));

    assertDoesNotThrow(() -> receptionistService.deleteReceptionist(1L));
    verify(receptionistRepository, times(1)).delete(receptionist);
  }

  @Test
  @DisplayName("Delete Receptionist - Not Found")
  @Order(13)
  void deleteReceptionist_NotFound() {
    when(receptionistRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> receptionistService.deleteReceptionist(1L));
  }

  @Test
  @DisplayName("Edit Receptionist - Success")
  @Order(14)
  void editReceptionist_Success() {
    UpdateReceptionistRequest updateRequest = new UpdateReceptionistRequest();
    updateRequest.setUserName("newusername");
    updateRequest.setEmail("new@example.com");
    updateRequest.setGender("Female");
    updateRequest.setFirstName("Jane");
    updateRequest.setNic("987654321V");
    updateRequest.setPhoneNumber("0777654321");

    Receptionist updatedReceptionist = new Receptionist();
    updatedReceptionist.setUserName("newusername");

    when(receptionistRepository.findById(1L)).thenReturn(Optional.of(receptionist));
    when(userRepository.existsByUserName(updateRequest.getUserName())).thenReturn(false);
    when(userRepository.existsByEmail(updateRequest.getEmail())).thenReturn(false);
    when(receptionistRepository.save(any())).thenReturn(updatedReceptionist);
    when(modelMapper.map(any(), eq(ReceptionistResponseDTO.class))).thenReturn(responseDTO);

    ReceptionistResponseDTO result = receptionistService.editReceptionist(1L, updateRequest);

    assertNotNull(result);
    verify(receptionistRepository, times(1)).save(any());
  }

  @Test
  @DisplayName("Edit Receptionist - Existing Username")
  @Order(15)
  void editReceptionist_ExistingUsername() {
    UpdateReceptionistRequest updateRequest = new UpdateReceptionistRequest();
    updateRequest.setUserName("existinguser");
    updateRequest.setEmail("john@example.com");

    when(receptionistRepository.findById(1L)).thenReturn(Optional.of(receptionist));
    when(userRepository.existsByUserName(updateRequest.getUserName())).thenReturn(true);

    assertThrows(BadRequestException.class, () -> receptionistService.editReceptionist(1L, updateRequest));
  }

  @Test
  @DisplayName("Edit Receptionist - Existing Email")
  @Order(16)
  void editReceptionist_ExistingEmail() {
    UpdateReceptionistRequest updateRequest = new UpdateReceptionistRequest();
    updateRequest.setUserName("johndoe");
    updateRequest.setEmail("existing@example.com");

    when(receptionistRepository.findById(1L)).thenReturn(Optional.of(receptionist));
    when(userRepository.existsByEmail(updateRequest.getEmail())).thenReturn(true);

    assertThrows(BadRequestException.class, () -> receptionistService.editReceptionist(1L, updateRequest));
  }

  @Test
  @DisplayName("Edit Receptionist - Not Found")
  @Order(17)
  void editReceptionist_NotFound() {
    when(receptionistRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () ->
            receptionistService.editReceptionist(1L, updateRequest));
  }

  @Test
  @DisplayName("Update Receptionist - Same Username and Email")
  @Order(18)
  void updateReceptionist_SameUsernameAndEmail() {
    CreateReceptionistDTO updateDTO = CreateReceptionistDTO.builder()
            .userName("johndoe")  // same as existing
            .email("john@example.com")  // same as existing
            .gender("Male")
            .password("NewPassword@123")
            .firstName("John Updated")
            .nic("123456789V")
            .phoneNumber("0771234567")
            .build();

    Receptionist updatedReceptionist = new Receptionist();
    updatedReceptionist.setUserName("johndoe");
    updatedReceptionist.setFirstName("John Updated");

    when(receptionistRepository.findById(1L)).thenReturn(Optional.of(receptionist));
    when(receptionistRepository.save(any())).thenReturn(updatedReceptionist);
    when(modelMapper.map(any(), eq(ReceptionistResponseDTO.class))).thenReturn(responseDTO);
    when(passwordEncoder.encode(updateDTO.getPassword())).thenReturn("newEncodedPassword");

    ReceptionistResponseDTO result = receptionistService.updateReceptionist(1L, updateDTO);

    assertNotNull(result);
    verify(receptionistRepository, times(1)).save(any());
    verify(userRepository, never()).existsByUserName(anyString());
    verify(userRepository, never()).existsByEmail(anyString());
  }

  @Test
  @DisplayName("Edit Receptionist - Same Username and Email")
  @Order(19)
  void editReceptionist_SameUsernameAndEmail() {
    UpdateReceptionistRequest updateRequest = new UpdateReceptionistRequest();
    updateRequest.setUserName("johndoe");  // same as existing
    updateRequest.setEmail("john@example.com");  // same as existing
    updateRequest.setGender("Male");
    updateRequest.setFirstName("John Updated");
    updateRequest.setNic("123456789V");
    updateRequest.setPhoneNumber("0771234567");

    Receptionist updatedReceptionist = new Receptionist();
    updatedReceptionist.setUserName("johndoe");
    updatedReceptionist.setFirstName("John Updated");

    when(receptionistRepository.findById(1L)).thenReturn(Optional.of(receptionist));
    when(receptionistRepository.save(any())).thenReturn(updatedReceptionist);
    when(modelMapper.map(any(), eq(ReceptionistResponseDTO.class))).thenReturn(responseDTO);

    ReceptionistResponseDTO result = receptionistService.editReceptionist(1L, updateRequest);

    assertNotNull(result);
    verify(receptionistRepository, times(1)).save(any());
    verify(userRepository, never()).existsByUserName(anyString());
    verify(userRepository, never()).existsByEmail(anyString());
  }

  @Test
  @DisplayName("Update Receptionist - Null Password")
  @Order(20)
  void updateReceptionist_NullPassword() {
    CreateReceptionistDTO updateDTO = CreateReceptionistDTO.builder()
            .userName("johndoe")
            .email("john@example.com")
            .gender("Male")
            .password(null)  // null password
            .firstName("John")
            .nic("123456789V")
            .phoneNumber("0771234567")
            .build();

    when(receptionistRepository.findById(1L)).thenReturn(Optional.of(receptionist));
    when(receptionistRepository.save(any())).thenReturn(receptionist);
    when(modelMapper.map(any(), eq(ReceptionistResponseDTO.class))).thenReturn(responseDTO);

    ReceptionistResponseDTO result = receptionistService.updateReceptionist(1L, updateDTO);

    assertNotNull(result);
    verify(passwordEncoder, never()).encode(anyString());
    verify(receptionistRepository, times(1)).save(any());
  }

  @Test
  @DisplayName("Update Receptionist - Empty Password")
  @Order(21)
  void updateReceptionist_EmptyPassword() {
    CreateReceptionistDTO updateDTO = CreateReceptionistDTO.builder()
            .userName("johndoe")
            .email("john@example.com")
            .gender("Male")
            .password("")  // empty password
            .firstName("John")
            .nic("123456789V")
            .phoneNumber("0771234567")
            .build();

    when(receptionistRepository.findById(1L)).thenReturn(Optional.of(receptionist));
    when(receptionistRepository.save(any())).thenReturn(receptionist);
    when(modelMapper.map(any(), eq(ReceptionistResponseDTO.class))).thenReturn(responseDTO);

    ReceptionistResponseDTO result = receptionistService.updateReceptionist(1L, updateDTO);

    assertNotNull(result);
    verify(passwordEncoder, never()).encode(anyString());
    verify(receptionistRepository, times(1)).save(any());
  }
}