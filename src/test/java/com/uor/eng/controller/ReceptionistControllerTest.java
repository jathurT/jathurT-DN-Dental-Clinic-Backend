package com.uor.eng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.payload.receiptionist.CreateReceptionistDTO;
import com.uor.eng.payload.receiptionist.ReceptionistResponseDTO;
import com.uor.eng.service.IReceptionistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ReceptionistControllerTest {

  private MockMvc mockMvc;

  @Mock
  private IReceptionistService receptionistService;

  @InjectMocks
  private ReceptionistController receptionistController;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(receptionistController)
            .setControllerAdvice(new TestExceptionHandler())
            .build();
  }

  @Test
  public void testCreateReceptionist_Success() throws Exception {
    // Arrange
    CreateReceptionistDTO request = new CreateReceptionistDTO();
    request.setUserName("receptionist1");
    request.setEmail("receptionist1@example.com");
    request.setFirstName("John");
    request.setGender("MALE");
    request.setPassword("Password123@");
    request.setNic("123456789V");
    request.setPhoneNumber("0771234567");

    Set<String> roles = new HashSet<>();
    roles.add("ROLE_RECEPTIONIST");

    ReceptionistResponseDTO response = new ReceptionistResponseDTO();
    response.setId(1L);
    response.setUserName("receptionist1");
    response.setEmail("receptionist1@example.com");
    response.setFirstName("John");
    response.setGender("MALE");
    response.setPhoneNumber("0771234567");
    response.setNic("123456789V");
    response.setRoles(roles);

    when(receptionistService.createReceptionist(any(CreateReceptionistDTO.class))).thenReturn(response);

    // Act & Assert
    mockMvc.perform(post("/api/receptionist/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.userName", is("receptionist1")))
            .andExpect(jsonPath("$.email", is("receptionist1@example.com")))
            .andExpect(jsonPath("$.firstName", is("John")))
            .andExpect(jsonPath("$.phoneNumber", is("0771234567")))
            .andExpect(jsonPath("$.shiftTiming", is("Morning")));
  }

  @Test
  public void testCreateReceptionist_BadRequest_UsernameExists() throws Exception {
    // Arrange
    CreateReceptionistDTO request = new CreateReceptionistDTO();
    request.setUserName("existingUser");
    request.setEmail("receptionist1@example.com");
    request.setFirstName("John");
    request.setGender("MALE");
    request.setPassword("Password123@");
    request.setNic("123456789V");
    request.setPhoneNumber("0771234567");

    when(receptionistService.createReceptionist(any(CreateReceptionistDTO.class)))
            .thenThrow(new BadRequestException("Username is already taken!"));

    // Act & Assert
    mockMvc.perform(post("/api/receptionist/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.error").value("Username is already taken!"));
  }

  @Test
  public void testCreateReceptionist_BadRequest_EmailExists() throws Exception {
    // Arrange
    CreateReceptionistDTO request = new CreateReceptionistDTO();
    request.setUserName("receptionist1");
    request.setEmail("existing@example.com");
    request.setFirstName("John");
    request.setGender("MALE");
    request.setPassword("Password123@");
    request.setNic("123456789V");
    request.setPhoneNumber("0771234567");

    when(receptionistService.createReceptionist(any(CreateReceptionistDTO.class)))
            .thenThrow(new BadRequestException("Email is already in use!"));

    // Act & Assert
    mockMvc.perform(post("/api/receptionist/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.error").value("Email is already in use!"));
  }

  @Test
  public void testCreateReceptionist_BadRequest_NicExists() throws Exception {
    // Arrange
    CreateReceptionistDTO request = new CreateReceptionistDTO();
    request.setUserName("receptionist1");
    request.setEmail("receptionist1@example.com");
    request.setFirstName("John");
    request.setGender("MALE");
    request.setPassword("Password123@");
    request.setNic("987654321V");
    request.setPhoneNumber("0771234567");

    when(receptionistService.createReceptionist(any(CreateReceptionistDTO.class)))
            .thenThrow(new BadRequestException("NIC is already in use!"));

    // Act & Assert
    mockMvc.perform(post("/api/receptionist/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.error").value("NIC is already in use!"));
  }

  @Test
  public void testCreateReceptionist_BadRequest_InvalidData() throws Exception {
    // Arrange
    CreateReceptionistDTO request = new CreateReceptionistDTO();
    // Missing required fields
    request.setUserName("");
    request.setEmail("invalid-email");

    // Act & Assert
    mockMvc.perform(post("/api/receptionist/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest());
    // Note: We're just checking the status code, not the specific error message
    // since Bean Validation errors come from Spring's validation framework
    // and have a different format than our custom exceptions
  }

  @Test
  public void testGetAllReceptionists_Success() throws Exception {
    // Arrange
    List<ReceptionistResponseDTO> receptionists = getReceptionistResponseDTOS();

    when(receptionistService.getAllReceptionists()).thenReturn(receptionists);

    // Act & Assert
    mockMvc.perform(get("/api/receptionist/all"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].userName", is("receptionist1")))
            .andExpect(jsonPath("$[0].firstName", is("John")))
            .andExpect(jsonPath("$[0].gender", is("MALE")))
            .andExpect(jsonPath("$[0].shiftTiming", is("Morning")))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].userName", is("receptionist2")))
            .andExpect(jsonPath("$[1].firstName", is("Jane")))
            .andExpect(jsonPath("$[1].gender", is("FEMALE")))
            .andExpect(jsonPath("$[1].shiftTiming", is("Evening")));
  }

  private static List<ReceptionistResponseDTO> getReceptionistResponseDTOS() {
    Set<String> roles = new HashSet<>();
    roles.add("ROLE_RECEPTIONIST");

    ReceptionistResponseDTO receptionist1 = new ReceptionistResponseDTO();
    receptionist1.setId(1L);
    receptionist1.setUserName("receptionist1");
    receptionist1.setEmail("receptionist1@example.com");
    receptionist1.setFirstName("John");
    receptionist1.setGender("MALE");
    receptionist1.setPhoneNumber("0771234567");
    receptionist1.setNic("123456789V");
    receptionist1.setRoles(roles);

    ReceptionistResponseDTO receptionist2 = new ReceptionistResponseDTO();
    receptionist2.setId(2L);
    receptionist2.setUserName("receptionist2");
    receptionist2.setEmail("receptionist2@example.com");
    receptionist2.setFirstName("Jane");
    receptionist2.setGender("FEMALE");
    receptionist2.setPhoneNumber("0777654321");
    receptionist2.setNic("987654321V");
    receptionist2.setRoles(roles);

    return Arrays.asList(receptionist1, receptionist2);
  }

  @Test
  public void testGetAllReceptionists_NoReceptionists() throws Exception {
    // Arrange
    when(receptionistService.getAllReceptionists())
            .thenThrow(new ResourceNotFoundException("No receptionists found."));

    // Act & Assert
    mockMvc.perform(get("/api/receptionist/all"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.details.error").value("No receptionists found."));
  }

  @Test
  public void testGetReceptionistById_Success() throws Exception {
    // Arrange
    ReceptionistResponseDTO receptionist = getReceptionistResponseDTO();

    when(receptionistService.getReceptionistById(1L)).thenReturn(receptionist);

    // Act & Assert
    mockMvc.perform(get("/api/receptionist/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.userName", is("receptionist1")))
            .andExpect(jsonPath("$.email", is("receptionist1@example.com")))
            .andExpect(jsonPath("$.firstName", is("John")))
            .andExpect(jsonPath("$.gender", is("MALE")))
            .andExpect(jsonPath("$.phoneNumber", is("0771234567")))
            .andExpect(jsonPath("$.nic", is("123456789V")))
            .andExpect(jsonPath("$.shiftTiming", is("Morning")));
  }

  private static ReceptionistResponseDTO getReceptionistResponseDTO() {
    Set<String> roles = new HashSet<>();
    roles.add("ROLE_RECEPTIONIST");

    ReceptionistResponseDTO receptionist = new ReceptionistResponseDTO();
    receptionist.setId(1L);
    receptionist.setUserName("receptionist1");
    receptionist.setEmail("receptionist1@example.com");
    receptionist.setFirstName("John");
    receptionist.setGender("MALE");
    receptionist.setPhoneNumber("0771234567");
    receptionist.setNic("123456789V");
    receptionist.setRoles(roles);
    return receptionist;
  }

  @Test
  public void testGetReceptionistById_NotFound() throws Exception {
    // Arrange
    when(receptionistService.getReceptionistById(999L))
            .thenThrow(new ResourceNotFoundException("Receptionist not found with id: 999"));

    // Act & Assert
    mockMvc.perform(get("/api/receptionist/999"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.details.error").value("Receptionist not found with id: 999"));
  }

  @Test
  public void testUpdateReceptionist_Success() throws Exception {
    // Arrange
    CreateReceptionistDTO request = new CreateReceptionistDTO();
    request.setUserName("receptionist1Updated");
    request.setEmail("receptionist1updated@example.com");
    request.setFirstName("John Updated");
    request.setGender("MALE");
    request.setPassword("Password123@");
    request.setNic("123456789V");
    request.setPhoneNumber("0771234567");

    ReceptionistResponseDTO response = getResponseDTO();

    when(receptionistService.updateReceptionist(anyLong(), any(CreateReceptionistDTO.class))).thenReturn(response);

    // Act & Assert
    mockMvc.perform(put("/api/receptionist/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.userName", is("receptionist1Updated")))
            .andExpect(jsonPath("$.email", is("receptionist1updated@example.com")))
            .andExpect(jsonPath("$.firstName", is("John Updated")))
            .andExpect(jsonPath("$.shiftTiming", is("Afternoon")));
  }

  private static ReceptionistResponseDTO getResponseDTO() {
    Set<String> roles = new HashSet<>();
    roles.add("ROLE_RECEPTIONIST");

    ReceptionistResponseDTO response = new ReceptionistResponseDTO();
    response.setId(1L);
    response.setUserName("receptionist1Updated");
    response.setEmail("receptionist1updated@example.com");
    response.setFirstName("John Updated");
    response.setGender("MALE");
    response.setPhoneNumber("0771234567");
    response.setNic("123456789V");
    response.setRoles(roles);
    return response;
  }

  @Test
  public void testUpdateReceptionist_BadRequest_UsernameExists() throws Exception {
    // Arrange
    CreateReceptionistDTO request = new CreateReceptionistDTO();
    request.setUserName("existingUser");
    request.setEmail("receptionist1@example.com");
    request.setFirstName("John");
    request.setGender("MALE");
    request.setPassword("Password123@");
    request.setNic("123456789V");
    request.setPhoneNumber("0771234567");

    when(receptionistService.updateReceptionist(anyLong(), any(CreateReceptionistDTO.class)))
            .thenThrow(new BadRequestException("Username is already taken!"));

    // Act & Assert
    mockMvc.perform(put("/api/receptionist/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.error").value("Username is already taken!"));
  }

  @Test
  public void testUpdateReceptionist_BadRequest_EmailExists() throws Exception {
    // Arrange
    CreateReceptionistDTO request = new CreateReceptionistDTO();
    request.setUserName("receptionist1");
    request.setEmail("existing@example.com");
    request.setFirstName("John");
    request.setGender("MALE");
    request.setPassword("Password123@");
    request.setNic("123456789V");
    request.setPhoneNumber("0771234567");

    when(receptionistService.updateReceptionist(anyLong(), any(CreateReceptionistDTO.class)))
            .thenThrow(new BadRequestException("Email is already in use!"));

    // Act & Assert
    mockMvc.perform(put("/api/receptionist/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.error").value("Email is already in use!"));
  }

  @Test
  public void testUpdateReceptionist_BadRequest_NicExists() throws Exception {
    // Arrange
    CreateReceptionistDTO request = new CreateReceptionistDTO();
    request.setUserName("receptionist1");
    request.setEmail("receptionist1@example.com");
    request.setFirstName("John");
    request.setGender("MALE");
    request.setPassword("Password123@");
    request.setNic("987654321V");
    request.setPhoneNumber("0771234567");

    when(receptionistService.updateReceptionist(anyLong(), any(CreateReceptionistDTO.class)))
            .thenThrow(new BadRequestException("NIC is already in use!"));

    // Act & Assert
    mockMvc.perform(put("/api/receptionist/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.error").value("NIC is already in use!"));
  }

  @Test
  public void testUpdateReceptionist_NotFound() throws Exception {
    // Arrange
    CreateReceptionistDTO request = new CreateReceptionistDTO();
    request.setUserName("receptionist1Updated");
    request.setEmail("receptionist1updated@example.com");
    request.setPassword("Password123@");
    request.setFirstName("John");
    request.setGender("MALE");
    request.setNic("123456789V");
    request.setPhoneNumber("0771234567");

    when(receptionistService.updateReceptionist(anyLong(), any(CreateReceptionistDTO.class)))
            .thenThrow(new ResourceNotFoundException("Receptionist not found with id: 999"));

    // Act & Assert
    mockMvc.perform(put("/api/receptionist/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.details.error").value("Receptionist not found with id: 999"));
  }

  @Test
  public void testDeleteReceptionist_Success() throws Exception {
    // Arrange
    doNothing().when(receptionistService).deleteReceptionist(1L);

    // Act & Assert
    mockMvc.perform(delete("/api/receptionist/1"))
            .andDo(print())
            .andExpect(status().isNoContent());
  }

  @Test
  public void testDeleteReceptionist_NotFound() throws Exception {
    // Arrange
    doThrow(new ResourceNotFoundException("Receptionist not found with id: 999 to delete."))
            .when(receptionistService).deleteReceptionist(999L);

    // Act & Assert
    mockMvc.perform(delete("/api/receptionist/999"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.details.error").value("Receptionist not found with id: 999 to delete."));
  }
}