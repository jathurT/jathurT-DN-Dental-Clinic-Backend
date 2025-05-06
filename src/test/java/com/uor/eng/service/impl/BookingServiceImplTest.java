package com.uor.eng.service.impl;

import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.*;
import com.uor.eng.payload.booking.BookingResponseDTO;
import com.uor.eng.payload.booking.CreateBookingDTO;
import com.uor.eng.payload.dashboard.MonthlyBookingStatsResponse;
import com.uor.eng.payload.patient.PatientResponse;
import com.uor.eng.repository.BookingRepository;
import com.uor.eng.repository.PatientRepository;
import com.uor.eng.repository.ScheduleRepository;
import com.uor.eng.util.EmailService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {

  @Mock
  private BookingRepository bookingRepository;

  @Mock
  private ScheduleRepository scheduleRepository;

  @Mock
  private PatientRepository patientRepository;

  @Mock
  private ModelMapper modelMapper;

  @Mock
  private EmailService emailService;

  @Mock
  private Counter createBookingCounter;

  @Mock
  private Counter createBookingErrorCounter;

  @Mock
  private Timer createBookingTimer;

  @Mock
  private Timer.Sample timerSample;

  private BookingServiceImpl bookingService;

  @Captor
  private ArgumentCaptor<Booking> bookingCaptor;

  private Schedule schedule;
  private Booking booking;
  private CreateBookingDTO createBookingDTO;
  private BookingResponseDTO bookingResponseDTO;
  private Patient patient;

  @BeforeEach
  public void setUp() {
    // Initialize BookingServiceImpl with mocked dependencies
    bookingService = new BookingServiceImpl(
            createBookingCounter,
            createBookingErrorCounter,
            createBookingTimer,
            bookingRepository,
            modelMapper,
            scheduleRepository,
            emailService,
            patientRepository
    );

    // Setup mock Timer.start() for all tests
    try (MockedStatic<Timer> mockedTimer = mockStatic(Timer.class)) {
      mockedTimer.when(Timer::start).thenReturn(timerSample);
    }

    // Setup common test objects
    Dentist dentist = new Dentist();
    dentist.setUserId(1L);
    dentist.setFirstName("Dr. Smith");

    schedule = new Schedule();
    schedule.setId(1L);
    schedule.setDate(LocalDate.now());
    schedule.setDayOfWeek("Monday");
    schedule.setStartTime(LocalTime.of(9, 0));
    schedule.setEndTime(LocalTime.of(17, 0));
    schedule.setStatus(ScheduleStatus.AVAILABLE);
    schedule.setCapacity(10);
    schedule.setAvailableSlots(5);
    schedule.setDentist(dentist);
    schedule.setBookings(new ArrayList<>());

    booking = new Booking();
    booking.setReferenceId("REF12345");
    booking.setName("John Doe");
    booking.setNic("123456789V");
    booking.setContactNumber("0771234567");
    booking.setEmail("john@example.com");
    booking.setAddress("123 Main St");
    booking.setSchedule(schedule);
    booking.setStatus(BookingStatus.PENDING);
    booking.setDate(LocalDate.now());
    booking.setCreatedAt(LocalDateTime.now());
    booking.setAppointmentNumber(1);

    createBookingDTO = new CreateBookingDTO();
    createBookingDTO.setScheduleId(1L);
    createBookingDTO.setName("John Doe");
    createBookingDTO.setNic("123456789V");
    createBookingDTO.setContactNumber("0771234567");
    createBookingDTO.setEmail("john@example.com");
    createBookingDTO.setAddress("123 Main St");

    bookingResponseDTO = new BookingResponseDTO();
    bookingResponseDTO.setReferenceId("REF12345");
    bookingResponseDTO.setName("John Doe");
    bookingResponseDTO.setNic("123456789V");
    bookingResponseDTO.setContactNumber("0771234567");
    bookingResponseDTO.setEmail("john@example.com");
    bookingResponseDTO.setAddress("123 Main St");
    bookingResponseDTO.setStatus(BookingStatus.PENDING);
    bookingResponseDTO.setScheduleDate(LocalDate.now());
    bookingResponseDTO.setScheduleDayOfWeek("Monday");
    bookingResponseDTO.setScheduleStartTime(LocalTime.of(9, 0));
    bookingResponseDTO.setDoctorName("Dr. Smith");

    patient = new Patient();
    patient.setId(1L);
    patient.setName("John Doe");
    patient.setNic("123456789V");
    patient.setEmail("john@example.com");
    patient.setContactNumbers(Collections.singletonList("0771234567"));
  }

  // Test 1: Successful booking creation
  @Test
  public void testCreateBooking_Success() {
    // Arrange
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
    when(scheduleRepository.findByIdWithLock(1L)).thenReturn(Optional.of(schedule));
    when(modelMapper.map(any(CreateBookingDTO.class), eq(Booking.class))).thenReturn(booking);
    when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
    when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);
    when(modelMapper.map(any(Booking.class), eq(BookingResponseDTO.class))).thenReturn(bookingResponseDTO);

    // Mock static Timer.start() for this test
    try (MockedStatic<Timer> mockedTimer = mockStatic(Timer.class)) {
      mockedTimer.when(Timer::start).thenReturn(timerSample);

      // Act
      BookingResponseDTO result = bookingService.createBooking(createBookingDTO);

      // Assert
      assertNotNull(result);
      assertEquals("REF12345", result.getReferenceId());
      assertEquals("John Doe", result.getName());

      // Verify schedule was updated
      assertEquals(4, schedule.getAvailableSlots());

      // Verify interactions
      verify(scheduleRepository).save(any(Schedule.class));
      verify(bookingRepository).save(any(Booking.class));
      verify(emailService).sendBookingConfirmation(any(BookingResponseDTO.class));
      verify(createBookingCounter).increment();
      verify(timerSample).stop(createBookingTimer);
    }
  }

  // Test 2: Create booking with invalid schedule ID
  @Test
  public void testCreateBooking_InvalidScheduleId() {
    // Arrange
    when(scheduleRepository.findById(1L)).thenReturn(Optional.empty());

    // Mock static Timer.start() for this test
    try (MockedStatic<Timer> mockedTimer = mockStatic(Timer.class)) {
      mockedTimer.when(Timer::start).thenReturn(timerSample);

      // Act & Assert
      ResourceNotFoundException exception = assertThrows(
              ResourceNotFoundException.class,
              () -> bookingService.createBooking(createBookingDTO)
      );

      assertTrue(exception.getMessage().contains("Schedule with ID 1 not found"));

      // Verify interactions
      verify(bookingRepository, never()).save(any(Booking.class));
      verify(createBookingErrorCounter).increment();
      verify(timerSample).stop(createBookingTimer);
    }
  }

  // Test 3: Create booking when schedule is full
  @Test
  public void testCreateBooking_ScheduleIsFull() {
    // Arrange
    schedule.setAvailableSlots(0);
    schedule.setStatus(ScheduleStatus.FULL);
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

    // Mock static Timer.start() for this test
    try (MockedStatic<Timer> mockedTimer = mockStatic(Timer.class)) {
      mockedTimer.when(Timer::start).thenReturn(timerSample);

      // Act & Assert
      BadRequestException exception = assertThrows(
              BadRequestException.class,
              () -> bookingService.createBooking(createBookingDTO)
      );

      assertTrue(exception.getMessage().contains("schedule is currently full"));

      // Verify interactions
      verify(scheduleRepository, never()).save(any(Schedule.class));
      verify(createBookingErrorCounter).increment();
      verify(timerSample).stop(createBookingTimer);
    }
  }

  // Test 4: Create booking when schedule is unavailable
  @Test
  public void testCreateBooking_ScheduleIsUnavailable() {
    // Arrange
    schedule.setStatus(ScheduleStatus.UNAVAILABLE);
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

    // Mock static Timer.start() for this test
    try (MockedStatic<Timer> mockedTimer = mockStatic(Timer.class)) {
      mockedTimer.when(Timer::start).thenReturn(timerSample);

      // Act & Assert
      BadRequestException exception = assertThrows(
              BadRequestException.class,
              () -> bookingService.createBooking(createBookingDTO)
      );

      assertTrue(exception.getMessage().contains("schedule is currently unavailable"));

      // Verify interactions
      verify(createBookingErrorCounter).increment();
      verify(timerSample).stop(createBookingTimer);
    }
  }

  // Test 5: Create booking with optimistic locking failure
  @Test
  public void testCreateBooking_OptimisticLockingFailure() {
    // Arrange
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
    when(scheduleRepository.findByIdWithLock(1L)).thenReturn(Optional.of(schedule));
    when(modelMapper.map(any(CreateBookingDTO.class), eq(Booking.class))).thenReturn(booking);

    // First attempt fails with optimistic locking exception
    when(scheduleRepository.save(any(Schedule.class)))
            .thenThrow(ObjectOptimisticLockingFailureException.class);

    // Mock static Timer.start() for this test
    try (MockedStatic<Timer> mockedTimer = mockStatic(Timer.class)) {
      mockedTimer.when(Timer::start).thenReturn(timerSample);

      // Act & Assert
      BadRequestException exception = assertThrows(
              BadRequestException.class,
              () -> bookingService.createBooking(createBookingDTO)
      );

      assertTrue(exception.getMessage().contains("System is currently busy"));

      // Verify interactions
      verify(bookingRepository, never()).save(any(Booking.class));
      verify(createBookingErrorCounter).increment();
      verify(timerSample).stop(createBookingTimer);
    }
  }

  // Test 6: Get all bookings
  @Test
  public void testGetAllBookings_Success() {
    // Arrange
    List<Booking> bookings = Collections.singletonList(booking);
    when(bookingRepository.findAll()).thenReturn(bookings);
    when(modelMapper.map(any(Booking.class), eq(BookingResponseDTO.class))).thenReturn(bookingResponseDTO);
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

    // Act
    List<BookingResponseDTO> result = bookingService.getAllBookings();

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("REF12345", result.get(0).getReferenceId());
  }

  // Test 7: Get all bookings when no bookings exist
  @Test
  public void testGetAllBookings_NoBookingsExist() {
    // Arrange
    when(bookingRepository.findAll()).thenReturn(Collections.emptyList());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> bookingService.getAllBookings()
    );

    assertTrue(exception.getMessage().contains("No bookings found"));
  }

  // Test 8: Get booking by reference ID and contact number
  @Test
  public void testGetBookingByReferenceIdAndContactNumber_Success() {
    // Arrange
    when(bookingRepository.findByReferenceIdAndContactNumber("REF12345", "0771234567"))
            .thenReturn(Optional.of(booking));
    when(modelMapper.map(any(Booking.class), eq(BookingResponseDTO.class))).thenReturn(bookingResponseDTO);
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

    // Act
    BookingResponseDTO result = bookingService.getBookingByReferenceIdAndContactNumber("REF12345", "0771234567");

    // Assert
    assertNotNull(result);
    assertEquals("REF12345", result.getReferenceId());
    assertEquals("0771234567", result.getContactNumber());
  }

  // Test 9: Get booking by reference ID and contact number when booking doesn't exist
  @Test
  public void testGetBookingByReferenceIdAndContactNumber_NotFound() {
    // Arrange
    when(bookingRepository.findByReferenceIdAndContactNumber("REF12345", "0771234567"))
            .thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> bookingService.getBookingByReferenceIdAndContactNumber("REF12345", "0771234567")
    );

    assertTrue(exception.getMessage().contains("Booking not found with reference ID"));
  }

  // Test 10: Get booking by ID
  @Test
  public void testGetBookingById_Success() {
    // Arrange
    when(bookingRepository.findById("REF12345")).thenReturn(Optional.of(booking));
    when(modelMapper.map(any(Booking.class), eq(BookingResponseDTO.class))).thenReturn(bookingResponseDTO);
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

    // Mock the configuration using a mock instead of trying to instantiate it
    Configuration configMock = mock(Configuration.class);
    when(modelMapper.getConfiguration()).thenReturn(configMock);
    when(configMock.setPropertyCondition(any())).thenReturn(configMock);

    // Act
    BookingResponseDTO result = bookingService.getBookingById("REF12345");

    // Assert
    assertNotNull(result);
    assertEquals("REF12345", result.getReferenceId());
  }

  // Test 11: Delete booking
  @Test
  public void testDeleteBooking_Success() {
    // Arrange
    when(bookingRepository.findById("REF12345")).thenReturn(Optional.of(booking));

    // Act
    bookingService.deleteBooking("REF12345");

    // Assert
    verify(bookingRepository).deleteById("REF12345");
  }

  // Test 12: Delete booking when booking doesn't exist
  @Test
  public void testDeleteBooking_NotFound() {
    // Arrange
    when(bookingRepository.findById("REF12345")).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> bookingService.deleteBooking("REF12345")
    );

    assertTrue(exception.getMessage().contains("does not exist"));
    verify(bookingRepository, never()).deleteById(anyString());
  }

  // Test 13: Update booking
  @Test
  public void testUpdateBooking_Success() {
    // Arrange
    when(bookingRepository.findById("REF12345")).thenReturn(Optional.of(booking));
    // Use doNothing() for void method - modelMapper.map() when used with an existing instance
    doNothing().when(modelMapper).map(any(CreateBookingDTO.class), any(Booking.class));
    when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
    when(modelMapper.map(any(Booking.class), eq(BookingResponseDTO.class))).thenReturn(bookingResponseDTO);
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

    // Act
    BookingResponseDTO result = bookingService.updateBooking("REF12345", createBookingDTO);

    // Assert
    assertNotNull(result);
    assertEquals("REF12345", result.getReferenceId());
    verify(bookingRepository).save(booking);
  }

  // Test 14: Update booking status
  @Test
  public void testUpdateBookingStatus_Success() {
    // Arrange
    when(bookingRepository.findById("REF12345")).thenReturn(Optional.of(booking));
    when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
    when(modelMapper.map(any(Booking.class), eq(BookingResponseDTO.class))).thenReturn(bookingResponseDTO);
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

    // Act
    BookingResponseDTO result = bookingService.updateBookingStatus("REF12345", "ACTIVE");

    // Assert
    assertNotNull(result);
    verify(bookingRepository).save(bookingCaptor.capture());
    assertEquals(BookingStatus.ACTIVE, bookingCaptor.getValue().getStatus());
  }

  // Test 15: Update booking status with invalid status
  @Test
  public void testUpdateBookingStatus_InvalidStatus() {
    // Arrange
    when(bookingRepository.findById("REF12345")).thenReturn(Optional.of(booking));

    // Act & Assert
    BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> bookingService.updateBookingStatus("REF12345", "INVALID_STATUS")
    );

    assertTrue(exception.getMessage().contains("Invalid status"));
    verify(bookingRepository, never()).save(any(Booking.class));
  }

  // Test 16: Get monthly booking stats
  @Test
  public void testGetCurrentMonthBookingStats() {
    // Arrange
    List<Booking> bookings = new ArrayList<>();

    // Add bookings with different statuses for the current month
    Booking booking1 = new Booking();
    booking1.setStatus(BookingStatus.PENDING);
    booking1.setDate(LocalDate.now());

    Booking booking2 = new Booking();
    booking2.setStatus(BookingStatus.FINISHED);
    booking2.setDate(LocalDate.now());

    Booking booking3 = new Booking();
    booking3.setStatus(BookingStatus.CANCELLED);
    booking3.setDate(LocalDate.now());

    bookings.add(booking1);
    bookings.add(booking2);
    bookings.add(booking3);

    when(bookingRepository.findAll()).thenReturn(bookings);

    // Act
    MonthlyBookingStatsResponse result = bookingService.getCurrentMonthBookingStats();

    // Assert
    assertNotNull(result);
    assertEquals(3, result.getTotalBookings());
    assertEquals(1, result.getFinishedBookings());
    assertEquals(1, result.getCancelledBookings());
    assertEquals(1, result.getPendingBookings());
  }

  // Test 17: Get or create patient from booking
  @Test
  public void testGetOrCreatePatientFromBooking_NewPatient() {
    // Arrange
    when(patientRepository.findByNic("123456789V")).thenReturn(Optional.empty());
    when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
      Patient savedPatient = invocation.getArgument(0);
      savedPatient.setId(1L);
      return savedPatient;
    });

    // Act
    PatientResponse result = bookingService.getOrCreatePatientFromBooking(booking);

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("John Doe", result.getName());
    assertEquals("123456789V", result.getNic());
    assertEquals("john@example.com", result.getEmail());
    assertEquals(1, result.getContactNumbers().size());
    assertEquals("0771234567", result.getContactNumbers().get(0));
  }

  // Test 18: Get or create patient from booking with existing patient
  @Test
  public void testGetOrCreatePatientFromBooking_ExistingPatient() {
    // Arrange
    when(patientRepository.findByNic("123456789V")).thenReturn(Optional.of(patient));

    // Act & Assert
    BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> bookingService.getOrCreatePatientFromBooking(booking)
    );

    assertTrue(exception.getMessage().contains("Patient with NIC 123456789V already exists"));
  }

  // Test 19: Get or create patient from booking ID
  @Test
  public void testGetOrCreatePatientFromBookingId_Success() {
    // Arrange
    when(bookingRepository.findById("REF12345")).thenReturn(Optional.of(booking));
    when(patientRepository.findByNic("123456789V")).thenReturn(Optional.empty());
    when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
      Patient savedPatient = invocation.getArgument(0);
      savedPatient.setId(1L);
      return savedPatient;
    });

    // Act
    PatientResponse result = bookingService.getOrCreatePatientFromBookingId("REF12345");

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("John Doe", result.getName());
  }

  // Test 20: Booking with pessimistic locking failure
  @Test
  public void testCreateBooking_PessimisticLockingFailure() {
    // Arrange
    when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
    when(scheduleRepository.findByIdWithLock(1L))
            .thenThrow(PessimisticLockingFailureException.class);
    when(modelMapper.map(any(CreateBookingDTO.class), eq(Booking.class))).thenReturn(booking);

    // Mock static Timer.start() for this test
    try (MockedStatic<Timer> mockedTimer = mockStatic(Timer.class)) {
      mockedTimer.when(Timer::start).thenReturn(timerSample);

      // Act & Assert
      BadRequestException exception = assertThrows(
              BadRequestException.class,
              () -> bookingService.createBooking(createBookingDTO)
      );

      assertTrue(exception.getMessage().contains("System is experiencing high demand"));

      // Verify interactions
      verify(createBookingErrorCounter).increment();
      verify(timerSample).stop(createBookingTimer);
    }
  }
}