package com.uor.eng.service.impl;

import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.Booking;
import com.uor.eng.model.Dentist;
import com.uor.eng.model.Schedule;
import com.uor.eng.model.ScheduleStatus;
import com.uor.eng.payload.dashboard.CancelledScheduleResponse;
import com.uor.eng.payload.dashboard.UpcomingScheduleResponse;
import com.uor.eng.payload.schedule.CreateScheduleDTO;
import com.uor.eng.payload.schedule.ScheduleResponseDTO;
import com.uor.eng.repository.BookingRepository;
import com.uor.eng.repository.DentistRepository;
import com.uor.eng.repository.ScheduleRepository;
import com.uor.eng.util.EmailService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ScheduleServiceImplTest {

  @Mock
  private ScheduleRepository scheduleRepository;

  @Mock
  private DentistRepository dentistRepository;

  @Mock
  private BookingRepository bookingRepository;

  @Mock
  private EmailService emailService;

  @Mock
  private ModelMapper modelMapper;

  @InjectMocks
  private ScheduleServiceImpl scheduleService;

  private Schedule schedule;
  private CreateScheduleDTO createScheduleDTO;
  private Dentist dentist;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    dentist = new Dentist();
    dentist.setUserId(1L);
    dentist.setFirstName("John");

    schedule = new Schedule();
    schedule.setId(1L);
    schedule.setDate(LocalDate.now());
    schedule.setDayOfWeek("Wednesday");
    schedule.setStartTime(LocalTime.of(9, 0));
    schedule.setEndTime(LocalTime.of(17, 0));
    schedule.setDentist(dentist);
    schedule.setCapacity(10);
    schedule.setAvailableSlots(10);

    createScheduleDTO = new CreateScheduleDTO();
    createScheduleDTO.setDentistId(1L);
    createScheduleDTO.setDate(LocalDate.now());
    createScheduleDTO.setStartTime(LocalTime.of(9, 0));
    createScheduleDTO.setEndTime(LocalTime.of(17, 0));
    createScheduleDTO.setCapacity(10);
    createScheduleDTO.setStatus("AVAILABLE");
  }

  @Test
  @DisplayName("Test create schedule with valid data")
  @Order(1)
  void testCreateSchedule() {
    when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
    when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);
    when(modelMapper.map(any(Schedule.class), eq(ScheduleResponseDTO.class))).thenReturn(new ScheduleResponseDTO());

    ScheduleResponseDTO response = scheduleService.createSchedule(createScheduleDTO);

    assertNotNull(response);
    assertEquals(0, response.getNumberOfBookings());
    verify(scheduleRepository, times(1)).save(any(Schedule.class));
  }

  @Test
  @DisplayName("Test create schedule with invalid status")
  @Order(2)
  void testCreateScheduleWithInvalidStatus() {
    createScheduleDTO.setStatus("INVALID_STATUS");

    assertThrows(BadRequestException.class, () -> scheduleService.createSchedule(createScheduleDTO));
  }

  @Test
  @DisplayName("Test get all schedules when available")
  @Order(3)
  void testGetAllSchedules() {
    List<Schedule> schedules = Collections.singletonList(schedule);
    when(scheduleRepository.findAll()).thenReturn(schedules);
    when(modelMapper.map(any(Schedule.class), eq(ScheduleResponseDTO.class))).thenReturn(new ScheduleResponseDTO());

    List<ScheduleResponseDTO> response = scheduleService.getAllSchedules();

    assertNotNull(response);
    assertEquals(1, response.size());
  }

  @Test
  @DisplayName("Test get all schedules when empty")
  @Order(4)
  void testGetAllSchedulesWhenEmpty() {
    when(scheduleRepository.findAll()).thenReturn(Collections.emptyList());

    assertThrows(ResourceNotFoundException.class, () -> scheduleService.getAllSchedules());
  }

  @Test
  @DisplayName("Test get schedule by ID")
  @Order(5)
  void testGetScheduleById() {
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
    when(modelMapper.map(any(Schedule.class), eq(ScheduleResponseDTO.class))).thenReturn(new ScheduleResponseDTO());

    ScheduleResponseDTO response = scheduleService.getScheduleById(1L);

    assertNotNull(response);
  }

  @Test
  @DisplayName("Test get schedule by ID when not found")
  @Order(6)
  void testGetScheduleByIdWhenNotFound() {
    when(scheduleRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> scheduleService.getScheduleById(1L));
  }

  @Test
  @DisplayName("Test delete schedule when no bookings")
  @Order(7)
  void testDeleteSchedule() {
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
    scheduleRepository.deleteById(1L);

    scheduleService.deleteSchedule(1L);

    verify(scheduleRepository, times(2)).deleteById(1L);
  }

  @Test
  @DisplayName("Test delete schedule with bookings")
  @Order(8)
  void testDeleteScheduleWithBookings() {
    Schedule scheduleWithBookings = new Schedule();
    scheduleWithBookings.setId(1L);
    scheduleWithBookings.setBookings(Collections.singletonList(new Booking()));

    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(scheduleWithBookings));

    assertThrows(BadRequestException.class, () -> scheduleService.deleteSchedule(1L));
  }

  @Test
  @DisplayName("Test update schedule with valid data")
  @Order(9)
  @Disabled
  void testUpdateSchedule() {
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
    when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);
    when(modelMapper.map(any(Schedule.class), eq(ScheduleResponseDTO.class))).thenReturn(new ScheduleResponseDTO());

    ScheduleResponseDTO response = scheduleService.updateSchedule(1L, createScheduleDTO);

    assertNotNull(response);
    assertEquals(10, response.getNumberOfBookings());
  }

  @Test
  @DisplayName("Test update schedule with invalid status")
  @Order(10)
  void testUpdateScheduleWithInvalidStatus() {
    createScheduleDTO.setStatus("INVALID_STATUS");

    assertThrows(ResourceNotFoundException.class, () -> scheduleService.updateSchedule(1L, createScheduleDTO));
  }

  @Test
  @DisplayName("Test update schedule status with valid status")
  @Order(11)
  @Disabled
  void testUpdateScheduleStatus() {
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
    when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);
    when(modelMapper.map(any(Schedule.class), eq(ScheduleResponseDTO.class))).thenReturn(new ScheduleResponseDTO());

    ScheduleResponseDTO response = scheduleService.updateScheduleStatus(1L, "AVAILABLE");

    assertNotNull(response);
  }

  @Test
  @DisplayName("Test update schedule status with invalid status")
  @Order(12)
  void testUpdateScheduleStatusWithInvalidStatus() {
    assertThrows(ResourceNotFoundException.class, () -> scheduleService.updateScheduleStatus(1L, "INVALID_STATUS"));
  }

  @Test
  @DisplayName("Test get cancelled schedules")
  @Order(13)
  void testGetCancelledSchedules() {
    when(scheduleRepository.findByStatus(ScheduleStatus.CANCELLED, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "date", "startTime"))))
        .thenReturn(new PageImpl<>(Collections.singletonList(schedule)));

    List<CancelledScheduleResponse> response = scheduleService.getCancelledSchedules();

    assertNotNull(response);
    assertEquals(1, response.size());
  }

  @Test
  @DisplayName("Test get upcoming schedules")
  @Order(14)
  @Disabled
  void testGetUpcomingSchedules() {
    when(scheduleRepository.findByStatus(ScheduleStatus.AVAILABLE, PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "date", "startTime"))))
        .thenReturn(new PageImpl<>(Collections.singletonList(schedule)));

    List<UpcomingScheduleResponse> response = scheduleService.getUpcomingSchedules();

    assertNotNull(response);
    assertEquals(1, response.size());
  }
}
