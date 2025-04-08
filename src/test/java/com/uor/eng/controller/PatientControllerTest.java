package com.uor.eng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uor.eng.payload.patient.CreatePatientRequest;
import com.uor.eng.payload.patient.PatientResponse;
import com.uor.eng.service.IPatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
public class PatientControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Mock
  private IPatientService patientService;

  private CreatePatientRequest createPatientRequest;
  private PatientResponse patientResponse;
  private List<PatientResponse> patientResponseList;

  @BeforeEach
  void setUp() {
    // Setup test data
    createPatientRequest = new CreatePatientRequest();
    createPatientRequest.setName("John Doe");
    createPatientRequest.setEmail("john@example.com");
    createPatientRequest.setNic("123456789V");
    createPatientRequest.setContactNumbers(Arrays.asList("1234567890"));

    patientResponse = PatientResponse.builder()
            .id(1L)
            .name("John Doe")
            .email("john@example.com")
            .nic("123456789V")
            .contactNumbers(Arrays.asList("1234567890"))
            .logs(Arrays.asList())
            .build();

    patientResponseList = Arrays.asList(patientResponse);
  }

  @Test
  void testCreatePatient() throws Exception {
    when(patientService.createPatient(any(CreatePatientRequest.class))).thenReturn(patientResponse);

    mockMvc.perform(post("/api/patients/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createPatientRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(patientResponse.getId()))
            .andExpect(jsonPath("$.name").value(patientResponse.getName()))
            .andExpect(jsonPath("$.email").value(patientResponse.getEmail()))
            .andExpect(jsonPath("$.nic").value(patientResponse.getNic()))
            .andExpect(jsonPath("$.contactNumbers", hasSize(1)))
            .andExpect(jsonPath("$.contactNumbers[0]").value(patientResponse.getContactNumbers().get(0)));

    verify(patientService, times(1)).createPatient(any(CreatePatientRequest.class));
  }

  @Test
  void testGetPatientById() throws Exception {
    Long patientId = 1L;
    when(patientService.getPatientById(patientId)).thenReturn(patientResponse);

    mockMvc.perform(get("/api/patients/{patientId}", patientId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(patientResponse.getId()))
            .andExpect(jsonPath("$.name").value(patientResponse.getName()))
            .andExpect(jsonPath("$.email").value(patientResponse.getEmail()))
            .andExpect(jsonPath("$.nic").value(patientResponse.getNic()));

    verify(patientService, times(1)).getPatientById(patientId);
  }

  @Test
  void testGetAllPatients() throws Exception {
    when(patientService.getAllPatients()).thenReturn(patientResponseList);

    mockMvc.perform(get("/api/patients/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(patientResponse.getId()))
            .andExpect(jsonPath("$[0].name").value(patientResponse.getName()))
            .andExpect(jsonPath("$[0].email").value(patientResponse.getEmail()))
            .andExpect(jsonPath("$[0].nic").value(patientResponse.getNic()));

    verify(patientService, times(1)).getAllPatients();
  }

  @Test
  void testUpdatePatient() throws Exception {
    Long patientId = 1L;
    when(patientService.updatePatient(eq(patientId), any(CreatePatientRequest.class))).thenReturn(patientResponse);

    mockMvc.perform(put("/api/patients/{patientId}", patientId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createPatientRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(patientResponse.getId()))
            .andExpect(jsonPath("$.name").value(patientResponse.getName()))
            .andExpect(jsonPath("$.email").value(patientResponse.getEmail()))
            .andExpect(jsonPath("$.nic").value(patientResponse.getNic()));

    verify(patientService, times(1)).updatePatient(eq(patientId), any(CreatePatientRequest.class));
  }

  @Test
  void testDeletePatient() throws Exception {
    Long patientId = 1L;
    doNothing().when(patientService).deletePatient(patientId);

    mockMvc.perform(delete("/api/patients/{patientId}", patientId))
            .andExpect(status().isNoContent());

    verify(patientService, times(1)).deletePatient(patientId);
  }
}