package com.uor.eng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.payload.patient.logs.*;
import com.uor.eng.service.PatientLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
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

public class PatientLogControllerTest {

  private MockMvc mockMvc;

  @Mock
  private PatientLogService patientLogService;

  @InjectMocks
  private PatientLogController patientLogController;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    // Set up MockMvc with TestExceptionHandler
    mockMvc = MockMvcBuilders.standaloneSetup(patientLogController)
            .setControllerAdvice(new TestExceptionHandler())
            .build();

    // Register JavaTimeModule for LocalDateTime serialization
    objectMapper.registerModule(new JavaTimeModule());
  }

  @Test
  public void testCreatePatientLog_Success() throws Exception {
    // Arrange
    PatientLogRequestNoPhotos request = new PatientLogRequestNoPhotos();
    request.setActionType("Initial Checkup");
    request.setDescription("Patient's first visit");
    request.setDentistId(1L);

    PatientLogResponse response = new PatientLogResponse();
    response.setId(1L);
    response.setActionType("Initial Checkup");
    response.setDescription("Patient's first visit");
    response.setTimestamp(LocalDateTime.now());
    response.setDentistName("Dr. Smith");
    response.setPhotos(Collections.emptyList()); // Initialize photos to prevent NPE

    when(patientLogService.createPatientLog(anyLong(), any(PatientLogRequestNoPhotos.class))).thenReturn(response);

    // Act & Assert
    mockMvc.perform(post("/api/patients/1/logs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.actionType", is("Initial Checkup")))
            .andExpect(jsonPath("$.description", is("Patient's first visit")))
            .andExpect(jsonPath("$.dentistName", is("Dr. Smith")))
            .andExpect(jsonPath("$.photos", hasSize(0)));
  }

  @Test
  public void testGetPatientLogs_Success() throws Exception {
    // Arrange
    PatientLogResponse log1 = new PatientLogResponse();
    log1.setId(1L);
    log1.setActionType("Initial Checkup");
    log1.setDescription("Patient's first visit");
    log1.setTimestamp(LocalDateTime.now());
    log1.setDentistName("Dr. Smith");
    log1.setPhotos(Collections.emptyList()); // Initialize photos to prevent NPE

    PatientLogResponse log2 = new PatientLogResponse();
    log2.setId(2L);
    log2.setActionType("Followup");
    log2.setDescription("Patient's followup visit");
    log2.setTimestamp(LocalDateTime.now());
    log2.setDentistName("Dr. Jones");
    log2.setPhotos(Collections.emptyList()); // Initialize photos to prevent NPE

    List<PatientLogResponse> logs = Arrays.asList(log1, log2);

    when(patientLogService.getPatientLogs(1L)).thenReturn(logs);

    // Act & Assert
    mockMvc.perform(get("/api/patients/1/logs"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].actionType", is("Initial Checkup")))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].actionType", is("Followup")));
  }

  @Test
  public void testGetPatientLog_Success() throws Exception {
    // Arrange
    PatientLogResponse log = new PatientLogResponse();
    log.setId(1L);
    log.setActionType("Initial Checkup");
    log.setDescription("Patient's first visit");
    log.setTimestamp(LocalDateTime.now());
    log.setDentistName("Dr. Smith");
    log.setPhotos(Collections.emptyList()); // Initialize photos to prevent NPE

    when(patientLogService.getPatientLog(1L, 1L)).thenReturn(log);

    // Act & Assert
    mockMvc.perform(get("/api/patients/1/logs/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.actionType", is("Initial Checkup")))
            .andExpect(jsonPath("$.description", is("Patient's first visit")))
            .andExpect(jsonPath("$.dentistName", is("Dr. Smith")));
  }

  @Test
  public void testGetPatientLog_NotFound() throws Exception {
    // Arrange
    when(patientLogService.getPatientLog(1L, 999L))
            .thenThrow(new ResourceNotFoundException("Patient log not found"));

    // Act & Assert
    mockMvc.perform(get("/api/patients/1/logs/999"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.details.error").value("Patient log not found"));
  }

  @Test
  public void testUpdatePatientLog_Success() throws Exception {
    // Arrange
    PatientLogUpdateRequest request = new PatientLogUpdateRequest();
    request.setActionType("Updated Checkup");
    request.setDescription("Updated description");

    PatientLogResponse response = new PatientLogResponse();
    response.setId(1L);
    response.setActionType("Updated Checkup");
    response.setDescription("Updated description");
    response.setTimestamp(LocalDateTime.now());
    response.setDentistName("Dr. Smith");
    response.setPhotos(Collections.emptyList()); // Initialize photos to prevent NPE

    when(patientLogService.updatePatientLog(anyLong(), anyLong(), any(PatientLogUpdateRequest.class))).thenReturn(response);

    // Act & Assert
    mockMvc.perform(put("/api/patients/1/logs/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.actionType", is("Updated Checkup")))
            .andExpect(jsonPath("$.description", is("Updated description")));
  }

  @Test
  public void testDeletePatientLog_Success() throws Exception {
    // Arrange
    doNothing().when(patientLogService).deletePatientLog(1L, 1L);

    // Act & Assert
    mockMvc.perform(delete("/api/patients/1/logs/1"))
            .andDo(print())
            .andExpect(status().isNoContent());
  }

  @Test
  public void testDeletePatientLog_NotFound() throws Exception {
    // Arrange
    doThrow(new ResourceNotFoundException("Patient log not found"))
            .when(patientLogService).deletePatientLog(1L, 999L);

    // Act & Assert
    mockMvc.perform(delete("/api/patients/1/logs/999"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.details.error").value("Patient log not found"));
  }

  @Test
  public void testAssociatePhotos_Success() throws Exception {
    // Arrange
    AssociatePhotosRequest request = new AssociatePhotosRequest();
    request.setS3Keys(Arrays.asList("key1", "key2"));
    request.setDescriptions(Arrays.asList("Photo 1", "Photo 2"));

    PatientLogPhotoResponse photo1 = new PatientLogPhotoResponse();
    photo1.setId(1L);
    photo1.setUrl("http://example.com/key1");
    photo1.setDescription("Photo 1");
    photo1.setTimestamp(LocalDateTime.now());

    PatientLogPhotoResponse photo2 = new PatientLogPhotoResponse();
    photo2.setId(2L);
    photo2.setUrl("http://example.com/key2");
    photo2.setDescription("Photo 2");
    photo2.setTimestamp(LocalDateTime.now());

    List<PatientLogPhotoResponse> photos = Arrays.asList(photo1, photo2);

    when(patientLogService.associatePhotosWithLog(anyLong(), anyLong(), any(AssociatePhotosRequest.class))).thenReturn(photos);

    // Act & Assert
    mockMvc.perform(post("/api/patients/1/logs/1/photos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].url", is("http://example.com/key1")))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].url", is("http://example.com/key2")));
  }

  @Test
  public void testGetPhotos_Success() throws Exception {
    // Arrange
    PatientLogPhotoResponse photo1 = new PatientLogPhotoResponse();
    photo1.setId(1L);
    photo1.setUrl("http://example.com/key1");
    photo1.setDescription("Photo 1");
    photo1.setTimestamp(LocalDateTime.now());

    PatientLogPhotoResponse photo2 = new PatientLogPhotoResponse();
    photo2.setId(2L);
    photo2.setUrl("http://example.com/key2");
    photo2.setDescription("Photo 2");
    photo2.setTimestamp(LocalDateTime.now());

    List<PatientLogPhotoResponse> photos = Arrays.asList(photo1, photo2);

    when(patientLogService.getPhotos(1L, 1L)).thenReturn(photos);

    // Act & Assert
    mockMvc.perform(get("/api/patients/1/logs/1/photos"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].url", is("http://example.com/key1")))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].url", is("http://example.com/key2")));
  }
}