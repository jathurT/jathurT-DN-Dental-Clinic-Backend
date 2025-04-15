package com.uor.eng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.payload.patient.CreatePatientRequest;
import com.uor.eng.payload.patient.PatientResponse;
import com.uor.eng.service.IPatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PatientControllerTest {

  private MockMvc mockMvc;

  @Mock
  private IPatientService patientService;

  @InjectMocks
  private PatientController patientController;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    // Set up mock MVC with our test exception handler
    mockMvc = MockMvcBuilders
            .standaloneSetup(patientController)
            .setControllerAdvice(new TestExceptionHandler())
            .build();
  }

  @Test
  public void testCreatePatient_Success() throws Exception {
    // Arrange
    CreatePatientRequest request = new CreatePatientRequest();
    request.setName("John Doe");
    request.setEmail("john@example.com");
    request.setNic("123456789V");
    request.setContactNumbers(List.of("0771234567"));

    PatientResponse response = new PatientResponse();
    response.setId(1L);
    response.setName("John Doe");
    response.setEmail("john@example.com");
    response.setNic("123456789V");
    response.setContactNumbers(List.of("0771234567"));
    response.setLogs(Collections.emptyList()); // Initialize logs to avoid NPE

    when(patientService.createPatient(any(CreatePatientRequest.class))).thenReturn(response);

    // Act & Assert
    mockMvc.perform(post("/api/patients/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("John Doe")))
            .andExpect(jsonPath("$.email", is("john@example.com")))
            .andExpect(jsonPath("$.nic", is("123456789V")))
            .andExpect(jsonPath("$.contactNumbers", hasSize(1)))
            .andExpect(jsonPath("$.contactNumbers[0]", is("0771234567")));
  }

  @Test
  public void testCreatePatient_BadRequest() throws Exception {
    // Arrange
    CreatePatientRequest request = new CreatePatientRequest();
    request.setName("John Doe");
    request.setEmail("john@example.com");
    request.setNic("123456789V");
    request.setContactNumbers(List.of("0771234567"));

    when(patientService.createPatient(any(CreatePatientRequest.class)))
            .thenThrow(new BadRequestException("Email is already in use!"));

    // Act & Assert
    mockMvc.perform(post("/api/patients/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.error").value("Email is already in use!"));
  }

  @Test
  public void testGetPatientById_Success() throws Exception {
    // Arrange
    PatientResponse response = new PatientResponse();
    response.setId(1L);
    response.setName("John Doe");
    response.setEmail("john@example.com");
    response.setNic("123456789V");
    response.setContactNumbers(List.of("0771234567"));
    response.setLogs(Collections.emptyList()); // Initialize logs to avoid NPE

    when(patientService.getPatientById(1L)).thenReturn(response);

    // Act & Assert
    mockMvc.perform(get("/api/patients/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("John Doe")))
            .andExpect(jsonPath("$.email", is("john@example.com")))
            .andExpect(jsonPath("$.nic", is("123456789V")))
            .andExpect(jsonPath("$.contactNumbers", hasSize(1)))
            .andExpect(jsonPath("$.contactNumbers[0]", is("0771234567")));
  }

  @Test
  public void testGetPatientById_NotFound() throws Exception {
    // Arrange
    when(patientService.getPatientById(999L))
            .thenThrow(new ResourceNotFoundException("Patient not found with id: 999"));

    // Act & Assert
    mockMvc.perform(get("/api/patients/999"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.details.error").value("Patient not found with id: 999"));
  }

  @Test
  public void testGetAllPatients_Success() throws Exception {
    // Arrange
    PatientResponse response1 = new PatientResponse();
    response1.setId(1L);
    response1.setName("John Doe");
    response1.setEmail("john@example.com");
    response1.setNic("123456789V");
    response1.setContactNumbers(List.of("0771234567"));
    response1.setLogs(Collections.emptyList()); // Initialize logs to avoid NPE

    PatientResponse response2 = new PatientResponse();
    response2.setId(2L);
    response2.setName("Jane Doe");
    response2.setEmail("jane@example.com");
    response2.setNic("987654321V");
    response2.setContactNumbers(List.of("0777654321"));
    response2.setLogs(Collections.emptyList()); // Initialize logs to avoid NPE

    List<PatientResponse> responses = Arrays.asList(response1, response2);

    when(patientService.getAllPatients()).thenReturn(responses);

    // Act & Assert
    mockMvc.perform(get("/api/patients/all"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].name", is("John Doe")))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].name", is("Jane Doe")));
  }

  @Test
  public void testUpdatePatient_Success() throws Exception {
    // Arrange
    CreatePatientRequest request = new CreatePatientRequest();
    request.setName("John Doe Updated");
    request.setEmail("johnupdated@example.com");
    request.setNic("123456789V");
    request.setContactNumbers(Arrays.asList("0771234567", "0771234568"));

    PatientResponse response = new PatientResponse();
    response.setId(1L);
    response.setName("John Doe Updated");
    response.setEmail("johnupdated@example.com");
    response.setNic("123456789V");
    response.setContactNumbers(Arrays.asList("0771234567", "0771234568"));
    response.setLogs(Collections.emptyList()); // Initialize logs to avoid NPE

    when(patientService.updatePatient(anyLong(), any(CreatePatientRequest.class))).thenReturn(response);

    // Act & Assert
    mockMvc.perform(put("/api/patients/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("John Doe Updated")))
            .andExpect(jsonPath("$.email", is("johnupdated@example.com")))
            .andExpect(jsonPath("$.contactNumbers", hasSize(2)));
  }

  @Test
  public void testUpdatePatient_NotFound() throws Exception {
    // Arrange
    CreatePatientRequest request = new CreatePatientRequest();
    request.setName("John Doe Updated");
    request.setEmail("johnupdated@example.com");
    request.setNic("123456789V");
    request.setContactNumbers(List.of("0771234567"));

    when(patientService.updatePatient(anyLong(), any(CreatePatientRequest.class)))
            .thenThrow(new ResourceNotFoundException("Patient not found with id: 999"));

    // Act & Assert
    mockMvc.perform(put("/api/patients/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.details.error").value("Patient not found with id: 999"));
  }

  @Test
  public void testDeletePatient_Success() throws Exception {
    // Arrange
    doNothing().when(patientService).deletePatient(1L);

    // Act & Assert
    mockMvc.perform(delete("/api/patients/1"))
            .andDo(print())
            .andExpect(status().isNoContent());
  }

  @Test
  public void testDeletePatient_NotFound() throws Exception {
    // Arrange
    doThrow(new ResourceNotFoundException("Patient not found with id: 999"))
            .when(patientService).deletePatient(999L);

    // Act & Assert
    mockMvc.perform(delete("/api/patients/999"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.details.error").value("Patient not found with id: 999"));
  }
}