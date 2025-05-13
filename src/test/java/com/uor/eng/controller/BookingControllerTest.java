package com.uor.eng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uor.eng.model.BookingStatus;
import com.uor.eng.model.ScheduleStatus;
import com.uor.eng.payload.booking.BookingResponseDTO;
import com.uor.eng.payload.booking.CreateBookingDTO;
import com.uor.eng.payload.dashboard.MonthlyBookingStatsResponse;
import com.uor.eng.payload.patient.PatientResponse;
import com.uor.eng.service.IBookingService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for BookingController using pure Mockito approach
 * This avoids using @MockBean which is deprecated in Spring Boot 3.4.0
 */
@ExtendWith(MockitoExtension.class)
public class BookingControllerTest {

  private MockMvc mockMvc;

  @Mock
  private IBookingService bookingService;

  @InjectMocks
  private BookingController bookingController;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private CreateBookingDTO createBookingDTO;
  private BookingResponseDTO bookingResponseDTO;
  private List<BookingResponseDTO> bookingResponseList;
  private MonthlyBookingStatsResponse monthlyStats;

  @BeforeEach
  void setUp() {
    // Setup MockMvc
    mockMvc = MockMvcBuilders.standaloneSetup(bookingController).build();

    // Configure ObjectMapper for Java 8 date/time types
    objectMapper.findAndRegisterModules();

    // Setup test data
    createBookingDTO = new CreateBookingDTO();
    createBookingDTO.setName("John Doe");
    createBookingDTO.setNic("123456789V");
    createBookingDTO.setContactNumber("1234567890");
    createBookingDTO.setEmail("john@example.com");
    createBookingDTO.setAddress("123 Main St");
    createBookingDTO.setScheduleId(1L);

    bookingResponseDTO = BookingResponseDTO.builder()
            .referenceId("ABC123")
            .appointmentNumber(1)
            .name("John Doe")
            .nic("123456789V")
            .contactNumber("1234567890")
            .email("john@example.com")
            .address("123 Main St")
            .scheduleId(1L)
            .scheduleDate(LocalDate.now())
            .scheduleDayOfWeek("Monday")
            .scheduleStatus(ScheduleStatus.AVAILABLE)
            .scheduleStartTime(LocalTime.of(10, 0))
            .doctorName("Dr. Smith")
            .status(BookingStatus.PENDING)
            .date(LocalDate.now())
            .dayOfWeek("Monday")
            .createdAt(LocalDateTime.now())
            .build();

    bookingResponseList = Collections.singletonList(bookingResponseDTO);

    monthlyStats = MonthlyBookingStatsResponse.builder()
            .month("APRIL")
            .totalBookings(10)
            .finishedBookings(5)
            .cancelledBookings(2)
            .pendingBookings(3)
            .build();
  }

  @Test
  void testCreateBooking() throws Exception {
    when(bookingService.createBooking(any(CreateBookingDTO.class))).thenReturn(bookingResponseDTO);

    mockMvc.perform(post("/api/bookings/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createBookingDTO)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.referenceId").value(bookingResponseDTO.getReferenceId()))
            .andExpect(jsonPath("$.name").value(bookingResponseDTO.getName()));

    verify(bookingService, times(1)).createBooking(any(CreateBookingDTO.class));
  }

  @Test
  void testGetAllBookings() throws Exception {
    when(bookingService.getAllBookings()).thenReturn(bookingResponseList);

    mockMvc.perform(get("/api/bookings/all"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", Matchers.hasSize(1)))
            .andExpect(jsonPath("$[0].referenceId").value(bookingResponseDTO.getReferenceId()));

    verify(bookingService, times(1)).getAllBookings();
  }

  @Test
  void testGetBookingByReferenceIdAndContactNumber() throws Exception {
    String referenceId = "ABC123";
    String contactNumber = "1234567890";
    when(bookingService.getBookingByReferenceIdAndContactNumber(referenceId, contactNumber))
            .thenReturn(bookingResponseDTO);

    mockMvc.perform(get("/api/bookings/{referenceId}/{contactNumber}", referenceId, contactNumber))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.referenceId").value(referenceId))
            .andExpect(jsonPath("$.contactNumber").value(contactNumber));

    verify(bookingService, times(1)).getBookingByReferenceIdAndContactNumber(referenceId, contactNumber);
  }

  @Test
  void testGetOrCreatePatientFromBooking() throws Exception {
    // Arrange
    String bookingId = "ABC123";
    PatientResponse patientResponse = PatientResponse.builder()
            .id(1L)
            .name("John Doe")
            .email("john@example.com")
            .nic("123456789V")
            .contactNumbers(List.of("1234567890"))
            .logs(Collections.emptyList())
            .build();

    when(bookingService.getOrCreatePatientFromBookingId(bookingId)).thenReturn(patientResponse);

    // Act & Assert
    mockMvc.perform(post("/api/bookings/{bookingId}/get-or-create-patient", bookingId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(patientResponse.getId()))
            .andExpect(jsonPath("$.name").value(patientResponse.getName()))
            .andExpect(jsonPath("$.email").value(patientResponse.getEmail()))
            .andExpect(jsonPath("$.nic").value(patientResponse.getNic()))
            .andExpect(jsonPath("$.contactNumbers[0]").value(patientResponse.getContactNumbers().get(0)))
            .andExpect(jsonPath("$.logs").isArray());

    // Verify
    verify(bookingService, times(1)).getOrCreatePatientFromBookingId(bookingId);
  }

  @Test
  void testDeleteBooking() throws Exception {
    String id = "ABC123";
    doNothing().when(bookingService).deleteBooking(id);

    mockMvc.perform(delete("/api/bookings/{id}", id))
            .andExpect(status().isNoContent());

    verify(bookingService, times(1)).deleteBooking(id);
  }

  @Test
  void testGetBookingById() throws Exception {
    String id = "ABC123";
    when(bookingService.getBookingById(id)).thenReturn(bookingResponseDTO);

    mockMvc.perform(get("/api/bookings/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.referenceId").value(bookingResponseDTO.getReferenceId()));

    verify(bookingService, times(1)).getBookingById(id);
  }

  @Test
  void testUpdateBooking() throws Exception {
    String id = "ABC123";
    when(bookingService.updateBooking(eq(id), any(CreateBookingDTO.class))).thenReturn(bookingResponseDTO);

    mockMvc.perform(put("/api/bookings/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createBookingDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.referenceId").value(bookingResponseDTO.getReferenceId()));

    verify(bookingService, times(1)).updateBooking(eq(id), any(CreateBookingDTO.class));
  }

  @Test
  void testUpdateBookingStatus() throws Exception {
    String id = "ABC123";
    String status = "ACTIVE";
    when(bookingService.updateBookingStatus(id, status)).thenReturn(bookingResponseDTO);

    mockMvc.perform(put("/api/bookings/updateStatus/{id}", id)
                    .param("status", status))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.referenceId").value(bookingResponseDTO.getReferenceId()));

    verify(bookingService, times(1)).updateBookingStatus(id, status);
  }

  @Test
  void testGetCurrentMonthBookingStats() throws Exception {
    when(bookingService.getCurrentMonthBookingStats()).thenReturn(monthlyStats);

    mockMvc.perform(get("/api/bookings/currentMonth/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.month").value(monthlyStats.getMonth()))
            .andExpect(jsonPath("$.totalBookings").value(monthlyStats.getTotalBookings()))
            .andExpect(jsonPath("$.finishedBookings").value(monthlyStats.getFinishedBookings()))
            .andExpect(jsonPath("$.cancelledBookings").value(monthlyStats.getCancelledBookings()))
            .andExpect(jsonPath("$.pendingBookings").value(monthlyStats.getPendingBookings()));

    verify(bookingService, times(1)).getCurrentMonthBookingStats();
  }
}