package com.uor.eng.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uor.eng.payload.dentist.CreateDentistDTO;
import com.uor.eng.payload.dentist.DentistResponseDTO;
import com.uor.eng.payload.dentist.UpdateDentistRequest;
import com.uor.eng.service.IDentistService;

/**
 * Test class for DentistController using pure Mockito approach
 * This avoids using @MockBean which is deprecated in Spring Boot 3.4.0
 */
@ExtendWith(MockitoExtension.class)
public class DentistControllerTest {

  private MockMvc mockMvc;

  @Mock
  private IDentistService dentistService;

  @InjectMocks
  private DentistController dentistController;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private CreateDentistDTO createDentistDTO;
  private DentistResponseDTO dentistResponseDTO;
  private UpdateDentistRequest updateDentistRequest;
  private List<DentistResponseDTO> dentistResponseList;

  @BeforeEach
  void setUp() {
    // Setup MockMvc
    mockMvc = MockMvcBuilders.standaloneSetup(dentistController).build();

    // Configure ObjectMapper
    objectMapper.findAndRegisterModules();

    // Setup test data for CreateDentistDTO
    createDentistDTO = new CreateDentistDTO();
    createDentistDTO.setUserName("dentist1");
    createDentistDTO.setEmail("dentist1@example.com");
    createDentistDTO.setPassword("Password1@");
    createDentistDTO.setFirstName("John");
    createDentistDTO.setGender("Male");
    createDentistDTO.setNic("123456789V");
    createDentistDTO.setPhoneNumber("1234567890");
    createDentistDTO.setSpecialization("Orthodontics");
    createDentistDTO.setLicenseNumber("DEN12345");

    // Setup test data for UpdateDentistRequest
    updateDentistRequest = new UpdateDentistRequest();
    updateDentistRequest.setUserName("dentist1");
    updateDentistRequest.setEmail("dentist1@example.com");
    updateDentistRequest.setFirstName("John");
    updateDentistRequest.setGender("Male");
    updateDentistRequest.setNic("123456789V");
    updateDentistRequest.setPhoneNumber("1234567890");
    updateDentistRequest.setSpecialization("Orthodontics");
    updateDentistRequest.setLicenseNumber("DEN12345");

    // Setup test data for DentistResponseDTO
    Set<String> roles = new HashSet<>();
    roles.add("ROLE_DENTIST");

    dentistResponseDTO = DentistResponseDTO.builder()
            .id(1L)
            .userName("dentist1")
            .email("dentist1@example.com")
            .firstName("John")
            .gender("Male")
            .nic("123456789V")
            .phoneNumber("1234567890")
            .specialization("Orthodontics")
            .licenseNumber("DEN12345")
            .roles(roles)
            .build();

    dentistResponseList = Arrays.asList(dentistResponseDTO);
  }

  @Test
  void testCreateDentist() throws Exception {
    when(dentistService.createDentist(any(CreateDentistDTO.class))).thenReturn(dentistResponseDTO);

    mockMvc.perform(post("/api/dentist/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDentistDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(dentistResponseDTO.getId()))
            .andExpect(jsonPath("$.userName").value(dentistResponseDTO.getUserName()))
            .andExpect(jsonPath("$.email").value(dentistResponseDTO.getEmail()))
            .andExpect(jsonPath("$.firstName").value(dentistResponseDTO.getFirstName()));

    verify(dentistService, times(1)).createDentist(any(CreateDentistDTO.class));
  }

  @Test
  void testGetAllDentists() throws Exception {
    when(dentistService.getAllDentists()).thenReturn(dentistResponseList);

    mockMvc.perform(get("/api/dentist/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", Matchers.hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(dentistResponseDTO.getId()))
            .andExpect(jsonPath("$[0].userName").value(dentistResponseDTO.getUserName()));

    verify(dentistService, times(1)).getAllDentists();
  }

  @Test
  void testGetDentistById() throws Exception {
    Long dentistId = 1L;
    when(dentistService.getDentistById(dentistId)).thenReturn(dentistResponseDTO);

    mockMvc.perform(get("/api/dentist/{id}", dentistId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(dentistResponseDTO.getId()))
            .andExpect(jsonPath("$.userName").value(dentistResponseDTO.getUserName()))
            .andExpect(jsonPath("$.email").value(dentistResponseDTO.getEmail()));

    verify(dentistService, times(1)).getDentistById(dentistId);
  }

  @Test
  void testUpdateDentist() throws Exception {
    Long dentistId = 1L;
    when(dentistService.updateDentist(eq(dentistId), any(CreateDentistDTO.class))).thenReturn(dentistResponseDTO);

    mockMvc.perform(put("/api/dentist/{id}", dentistId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createDentistDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(dentistResponseDTO.getId()))
            .andExpect(jsonPath("$.userName").value(dentistResponseDTO.getUserName()));

    verify(dentistService, times(1)).updateDentist(eq(dentistId), any(CreateDentistDTO.class));
  }

  @Test
  void testEditDentist() throws Exception {
    Long dentistId = 1L;
    when(dentistService.editDentist(eq(dentistId), any(UpdateDentistRequest.class))).thenReturn(dentistResponseDTO);

    mockMvc.perform(put("/api/dentist/edit/{id}", dentistId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDentistRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(dentistResponseDTO.getId()))
            .andExpect(jsonPath("$.userName").value(dentistResponseDTO.getUserName()));

    verify(dentistService, times(1)).editDentist(eq(dentistId), any(UpdateDentistRequest.class));
  }

  @Test
  void testDeleteDentist() throws Exception {
    Long dentistId = 1L;
    doNothing().when(dentistService).deleteDentist(dentistId);

    mockMvc.perform(delete("/api/dentist/{id}", dentistId))
            .andExpect(status().isNoContent());

    verify(dentistService, times(1)).deleteDentist(dentistId);
  }
}