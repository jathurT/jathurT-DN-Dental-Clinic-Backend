package com.uor.eng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.payload.dashboard.CancelledScheduleResponse;
import com.uor.eng.payload.dashboard.ScheduleHistoryResponse;
import com.uor.eng.payload.dashboard.UpcomingScheduleResponse;
import com.uor.eng.payload.schedule.CreateScheduleDTO;
import com.uor.eng.payload.schedule.ScheduleGetSevenCustomResponse;
import com.uor.eng.payload.schedule.ScheduleResponseDTO;
import com.uor.eng.service.IScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ScheduleControllerTest {

  private MockMvc mockMvc;

  @Mock
  private IScheduleService scheduleService;

  @InjectMocks
  private ScheduleController scheduleController;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(scheduleController)
            .setControllerAdvice(new TestExceptionHandler())
            .build();

    // Register JavaTimeModule for LocalDate/LocalTime serialization
    objectMapper.registerModule(new JavaTimeModule());
  }

  @Test
  public void testCreateSchedule_Success() throws Exception {
    // Arrange
    CreateScheduleDTO request = new CreateScheduleDTO();
    request.setDate(LocalDate.now().plusDays(1));
    request.setStatus("AVAILABLE");
    request.setStartTime(LocalTime.of(9, 0));
    request.setEndTime(LocalTime.of(17, 0));
    request.setDentistId(1L);
    request.setCapacity(10);

    ScheduleResponseDTO response = new ScheduleResponseDTO();
    response.setId(1L);
    response.setDate(LocalDate.now().plusDays(1));
    response.setDayOfWeek("Monday");
    response.setStatus("AVAILABLE");
    response.setStartTime(LocalTime.of(9, 0));
    response.setEndTime(LocalTime.of(17, 0));
    response.setDentistId(1L);
    response.setCapacity(10);
    response.setAvailableSlots(10);
    response.setCreatedAt(LocalDateTime.now());
    response.setBookings(Collections.emptyList()); // Initialize bookings to prevent NPE

    when(scheduleService.createSchedule(any(CreateScheduleDTO.class))).thenReturn(response);

    // Act & Assert
    mockMvc.perform(post("/api/schedules/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.status", is("AVAILABLE")))
            .andExpect(jsonPath("$.capacity", is(10)))
            .andExpect(jsonPath("$.availableSlots", is(10)));
  }

  @Test
  public void testCreateSchedule_BadRequest() throws Exception {
    // Arrange
    CreateScheduleDTO request = new CreateScheduleDTO();
    request.setDate(LocalDate.now().plusDays(1));
    request.setStatus("AVAILABLE");
    request.setStartTime(LocalTime.of(9, 0));
    request.setEndTime(LocalTime.of(17, 0));
    request.setDentistId(1L);
    request.setCapacity(10);

    when(scheduleService.createSchedule(any(CreateScheduleDTO.class)))
            .thenThrow(new BadRequestException("Dentist with ID 1 not found."));

    // Act & Assert
    mockMvc.perform(post("/api/schedules/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.error").value("Dentist with ID 1 not found."));
  }

  @Test
  public void testGetAllSchedules_Success() throws Exception {
    // Arrange
    ScheduleResponseDTO schedule1 = new ScheduleResponseDTO();
    schedule1.setId(1L);
    schedule1.setDate(LocalDate.now().plusDays(1));
    schedule1.setDayOfWeek("Monday");
    schedule1.setStatus("AVAILABLE");
    schedule1.setStartTime(LocalTime.of(9, 0));
    schedule1.setEndTime(LocalTime.of(17, 0));
    schedule1.setDentistId(1L);
    schedule1.setCapacity(10);
    schedule1.setAvailableSlots(10);
    schedule1.setCreatedAt(LocalDateTime.now());
    schedule1.setBookings(Collections.emptyList()); // Initialize bookings to prevent NPE

    ScheduleResponseDTO schedule2 = new ScheduleResponseDTO();
    schedule2.setId(2L);
    schedule2.setDate(LocalDate.now().plusDays(2));
    schedule2.setDayOfWeek("Tuesday");
    schedule2.setStatus("AVAILABLE");
    schedule2.setStartTime(LocalTime.of(9, 0));
    schedule2.setEndTime(LocalTime.of(17, 0));
    schedule2.setDentistId(1L);
    schedule2.setCapacity(10);
    schedule2.setAvailableSlots(10);
    schedule2.setCreatedAt(LocalDateTime.now());
    schedule2.setBookings(Collections.emptyList()); // Initialize bookings to prevent NPE

    List<ScheduleResponseDTO> schedules = Arrays.asList(schedule1, schedule2);

    when(scheduleService.getAllSchedules()).thenReturn(schedules);

    // Act & Assert
    mockMvc.perform(get("/api/schedules/all"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].status", is("AVAILABLE")))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].status", is("AVAILABLE")));
  }

  @Test
  public void testGetScheduleById_Success() throws Exception {
    // Arrange
    ScheduleResponseDTO schedule = new ScheduleResponseDTO();
    schedule.setId(1L);
    schedule.setDate(LocalDate.now().plusDays(1));
    schedule.setDayOfWeek("Monday");
    schedule.setStatus("AVAILABLE");
    schedule.setStartTime(LocalTime.of(9, 0));
    schedule.setEndTime(LocalTime.of(17, 0));
    schedule.setDentistId(1L);
    schedule.setCapacity(10);
    schedule.setAvailableSlots(10);
    schedule.setCreatedAt(LocalDateTime.now());
    schedule.setBookings(Collections.emptyList()); // Initialize bookings to prevent NPE

    when(scheduleService.getScheduleById(1L)).thenReturn(schedule);

    // Act & Assert
    mockMvc.perform(get("/api/schedules/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.status", is("AVAILABLE")))
            .andExpect(jsonPath("$.capacity", is(10)))
            .andExpect(jsonPath("$.availableSlots", is(10)));
  }

  @Test
  public void testGetScheduleById_NotFound() throws Exception {
    // Arrange
    when(scheduleService.getScheduleById(999L))
            .thenThrow(new ResourceNotFoundException("Schedule with ID 999 not found."));

    // Act & Assert
    mockMvc.perform(get("/api/schedules/999"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.details.error").value("Schedule with ID 999 not found."));
  }

  @Test
  public void testDeleteSchedule_Success() throws Exception {
    // Arrange
    doNothing().when(scheduleService).deleteSchedule(1L);

    // Act & Assert
    mockMvc.perform(delete("/api/schedules/1"))
            .andDo(print())
            .andExpect(status().isNoContent());
  }

  @Test
  public void testDeleteSchedule_NotFound() throws Exception {
    // Arrange
    doThrow(new ResourceNotFoundException("Schedule with ID 999 not found."))
            .when(scheduleService).deleteSchedule(999L);

    // Act & Assert
    mockMvc.perform(delete("/api/schedules/999"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.details.error").value("Schedule with ID 999 not found."));
  }

  @Test
  public void testUpdateSchedule_Success() throws Exception {
    // Arrange
    CreateScheduleDTO request = new CreateScheduleDTO();
    request.setDate(LocalDate.now().plusDays(2));
    request.setStatus("UNAVAILABLE");
    request.setStartTime(LocalTime.of(10, 0));
    request.setEndTime(LocalTime.of(18, 0));
    request.setDentistId(1L);
    request.setCapacity(5);

    ScheduleResponseDTO response = new ScheduleResponseDTO();
    response.setId(1L);
    response.setDate(LocalDate.now().plusDays(2));
    response.setDayOfWeek("Tuesday");
    response.setStatus("UNAVAILABLE");
    response.setStartTime(LocalTime.of(10, 0));
    response.setEndTime(LocalTime.of(18, 0));
    response.setDentistId(1L);
    response.setCapacity(5);
    response.setAvailableSlots(5);
    response.setCreatedAt(LocalDateTime.now());
    response.setBookings(Collections.emptyList()); // Initialize bookings to prevent NPE

    when(scheduleService.updateSchedule(anyLong(), any(CreateScheduleDTO.class))).thenReturn(response);

    // Act & Assert
    mockMvc.perform(put("/api/schedules/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.status", is("UNAVAILABLE")))
            .andExpect(jsonPath("$.capacity", is(5)))
            .andExpect(jsonPath("$.availableSlots", is(5)));
  }

  @Test
  public void testUpdateScheduleStatus_Success() throws Exception {
    // Arrange
    ScheduleResponseDTO response = new ScheduleResponseDTO();
    response.setId(1L);
    response.setDate(LocalDate.now().plusDays(1));
    response.setDayOfWeek("Monday");
    response.setStatus("CANCELLED");
    response.setStartTime(LocalTime.of(9, 0));
    response.setEndTime(LocalTime.of(17, 0));
    response.setDentistId(1L);
    response.setCapacity(10);
    response.setAvailableSlots(0);
    response.setCreatedAt(LocalDateTime.now());
    response.setBookings(Collections.emptyList()); // Initialize bookings to prevent NPE

    when(scheduleService.updateScheduleStatus(anyLong(), eq("CANCELLED"))).thenReturn(response);

    // Act & Assert
    mockMvc.perform(put("/api/schedules/updateStatus/1")
                    .param("status", "CANCELLED"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.status", is("CANCELLED")))
            .andExpect(jsonPath("$.availableSlots", is(0)));
  }

  @Test
  public void testUpdateScheduleStatus_BadRequest() throws Exception {
    // Arrange
    when(scheduleService.updateScheduleStatus(anyLong(), eq("INVALID_STATUS")))
            .thenThrow(new BadRequestException("Invalid schedule status: INVALID_STATUS"));

    // Act & Assert
    mockMvc.perform(put("/api/schedules/updateStatus/1")
                    .param("status", "INVALID_STATUS"))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.error").value("Invalid schedule status: INVALID_STATUS"));
  }

  @Test
  public void testGetNextSevenSchedules_Success() throws Exception {
    // Arrange
    ScheduleResponseDTO schedule1 = new ScheduleResponseDTO();
    schedule1.setId(1L);
    schedule1.setDate(LocalDate.now().plusDays(1));
    schedule1.setDayOfWeek("Monday");
    schedule1.setStatus("AVAILABLE");
    schedule1.setBookings(Collections.emptyList()); // Initialize bookings to prevent NPE

    ScheduleResponseDTO schedule2 = new ScheduleResponseDTO();
    schedule2.setId(2L);
    schedule2.setDate(LocalDate.now().plusDays(2));
    schedule2.setDayOfWeek("Tuesday");
    schedule2.setStatus("AVAILABLE");
    schedule2.setBookings(Collections.emptyList()); // Initialize bookings to prevent NPE

    List<ScheduleResponseDTO> schedules = Arrays.asList(schedule1, schedule2);

    when(scheduleService.getNextSevenSchedules()).thenReturn(schedules);

    // Act & Assert
    mockMvc.perform(get("/api/schedules/getSeven"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].dayOfWeek", is("Monday")))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].dayOfWeek", is("Tuesday")));
  }

  @Test
  public void testGetNextSevenSchedules_NoSchedules() throws Exception {
    // Arrange
    when(scheduleService.getNextSevenSchedules())
            .thenThrow(new ResourceNotFoundException("No schedules found for the next 7 days."));

    // Act & Assert
    mockMvc.perform(get("/api/schedules/getSeven"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.details.error").value("No schedules found for the next 7 days."));
  }

  @Test
  public void testGetNextSevenSchedulesCustom_Success() throws Exception {
    // Arrange
    ScheduleGetSevenCustomResponse schedule1 = new ScheduleGetSevenCustomResponse();
    schedule1.setId(1L);
    schedule1.setDate(LocalDate.now().plusDays(1));
    schedule1.setDayOfWeek("Monday");
    schedule1.setStartTime(LocalTime.of(9, 0));

    ScheduleGetSevenCustomResponse schedule2 = new ScheduleGetSevenCustomResponse();
    schedule2.setId(2L);
    schedule2.setDate(LocalDate.now().plusDays(2));
    schedule2.setDayOfWeek("Tuesday");
    schedule2.setStartTime(LocalTime.of(9, 0));

    List<ScheduleGetSevenCustomResponse> schedules = Arrays.asList(schedule1, schedule2);

    when(scheduleService.getNextSevenSchedulesCustom()).thenReturn(schedules);

    // Act & Assert
    mockMvc.perform(get("/api/schedules/getSevenCustom"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].dayOfWeek", is("Monday")))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].dayOfWeek", is("Tuesday")));
  }

  @Test
  public void testGetCancelledSchedules_Success() throws Exception {
    // Arrange
    CancelledScheduleResponse schedule1 = new CancelledScheduleResponse();
    schedule1.setDate("2023-01-01");
    schedule1.setStartTime("09:00");
    schedule1.setEndTime("17:00");

    CancelledScheduleResponse schedule2 = new CancelledScheduleResponse();
    schedule2.setDate("2023-01-02");
    schedule2.setStartTime("10:00");
    schedule2.setEndTime("18:00");

    List<CancelledScheduleResponse> schedules = Arrays.asList(schedule1, schedule2);

    when(scheduleService.getCancelledSchedules()).thenReturn(schedules);

    // Act & Assert
    mockMvc.perform(get("/api/schedules/cancelledSchedules"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].date", is("2023-01-01")))
            .andExpect(jsonPath("$[0].startTime", is("09:00")))
            .andExpect(jsonPath("$[1].date", is("2023-01-02")))
            .andExpect(jsonPath("$[1].startTime", is("10:00")));
  }

  @Test
  public void testGetCancelledSchedules_NoSchedules() throws Exception {
    // Arrange
    when(scheduleService.getCancelledSchedules())
            .thenThrow(new ResourceNotFoundException("No cancelled schedules found."));

    // Act & Assert
    mockMvc.perform(get("/api/schedules/cancelledSchedules"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.details.error").value("No cancelled schedules found."));
  }

  @Test
  public void testGetUpcomingSchedules_Success() throws Exception {
    // Arrange
    List<UpcomingScheduleResponse> schedules = getUpcomingScheduleResponses();

    when(scheduleService.getUpcomingSchedules()).thenReturn(schedules);

    // Act & Assert
    mockMvc.perform(get("/api/schedules/upcomingSchedules"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].date", is("2023-01-01")))
            .andExpect(jsonPath("$[0].startTime", is("09:00")))
            .andExpect(jsonPath("$[0].appointmentCount", is(5)))
            .andExpect(jsonPath("$[1].date", is("2023-01-02")))
            .andExpect(jsonPath("$[1].appointmentCount", is(3)));
  }

  private static List<UpcomingScheduleResponse> getUpcomingScheduleResponses() {
    UpcomingScheduleResponse schedule1 = new UpcomingScheduleResponse();
    schedule1.setDate("2023-01-01");
    schedule1.setStartTime("09:00");
    schedule1.setEndTime("17:00");
    schedule1.setAppointmentCount(5);

    UpcomingScheduleResponse schedule2 = new UpcomingScheduleResponse();
    schedule2.setDate("2023-01-02");
    schedule2.setStartTime("10:00");
    schedule2.setEndTime("18:00");
    schedule2.setAppointmentCount(3);

    return Arrays.asList(schedule1, schedule2);
  }

  @Test
  public void testGetUpcomingSchedules_NoSchedules() throws Exception {
    // Arrange
    when(scheduleService.getUpcomingSchedules())
            .thenThrow(new ResourceNotFoundException("No upcoming schedules found."));

    // Act & Assert
    mockMvc.perform(get("/api/schedules/upcomingSchedules"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.details.error").value("No upcoming schedules found."));
  }

  @Test
  public void testGetScheduleHistory_Success() throws Exception {
    // Arrange
    ScheduleHistoryResponse history1 = new ScheduleHistoryResponse();
    history1.setDate("2023-01-01");
    history1.setAppointmentCount(5);

    ScheduleHistoryResponse history2 = new ScheduleHistoryResponse();
    history2.setDate("2023-01-02");
    history2.setAppointmentCount(3);

    List<ScheduleHistoryResponse> history = Arrays.asList(history1, history2);

    when(scheduleService.getScheduleHistory()).thenReturn(history);

    // Act & Assert
    mockMvc.perform(get("/api/schedules/scheduleHistory"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].date", is("2023-01-01")))
            .andExpect(jsonPath("$[0].appointmentCount", is(5)))
            .andExpect(jsonPath("$[1].date", is("2023-01-02")))
            .andExpect(jsonPath("$[1].appointmentCount", is(3)));
  }
}