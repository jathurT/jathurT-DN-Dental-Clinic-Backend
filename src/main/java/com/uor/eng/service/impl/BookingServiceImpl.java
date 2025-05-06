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
import com.uor.eng.service.IBookingService;
import com.uor.eng.util.EmailService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleObjectStateException;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BookingServiceImpl implements IBookingService {

  private final Counter createBookingCounter;
  private final Counter createBookingErrorCounter;
  private final Timer createBookingTimer;
  private final BookingRepository bookingRepository;
  private final ModelMapper modelMapper;
  private final ScheduleRepository scheduleRepository;
  private final EmailService emailService;
  private final PatientRepository patientRepository;

  public BookingServiceImpl(Counter createBookingCounter,
                            Counter createBookingErrorCounter,
                            Timer createBookingTimer,
                            BookingRepository bookingRepository,
                            ModelMapper modelMapper,
                            ScheduleRepository scheduleRepository,
                            EmailService emailService,
                            PatientRepository patientRepository) {
    this.createBookingCounter = createBookingCounter;
    this.createBookingErrorCounter = createBookingErrorCounter;
    this.createBookingTimer = createBookingTimer;
    this.bookingRepository = bookingRepository;
    this.modelMapper = modelMapper;
    this.scheduleRepository = scheduleRepository;
    this.emailService = emailService;
    this.patientRepository = patientRepository;
  }

  @Override
  @Transactional
  public BookingResponseDTO createBooking(CreateBookingDTO bookingDTO) {
    Timer.Sample sample = Timer.start();
    try {
      // First perform a quick check without locking
      Schedule schedule = scheduleRepository.findById(bookingDTO.getScheduleId())
              .orElseThrow(() -> {
                createBookingErrorCounter.increment();
                return new ResourceNotFoundException("Schedule with ID " + bookingDTO.getScheduleId() + " not found.");
              });

      // Initial validation
      if (schedule.getAvailableSlots() <= 0 || schedule.getStatus() == ScheduleStatus.FULL) {
        createBookingErrorCounter.increment();
        throw new BadRequestException("Cannot create booking. The selected schedule is currently full.");
      } else if (schedule.getStatus() == ScheduleStatus.UNAVAILABLE ||
              schedule.getStatus() == ScheduleStatus.CANCELLED ||
              schedule.getStatus() == ScheduleStatus.FINISHED ||
              schedule.getStatus() == ScheduleStatus.ON_GOING ||
              schedule.getStatus() == ScheduleStatus.ACTIVE) {
        createBookingErrorCounter.increment();
        throw new BadRequestException("Cannot create booking. The selected schedule is currently unavailable.");
      }

      // Prepare the booking object
      Booking booking = modelMapper.map(bookingDTO, Booking.class);

      // Use optimistic locking with retries
      int maxRetries = 3;

      for (int attempt = 0; attempt < maxRetries; attempt++) {
        try {
          // Get a fresh copy of the schedule with a pessimistic lock
          schedule = scheduleRepository.findByIdWithLock(bookingDTO.getScheduleId())
                  .orElseThrow(() -> new ResourceNotFoundException("Schedule no longer exists"));

          // Revalidate with latest data
          if (schedule.getAvailableSlots() <= 0 || schedule.getStatus() != ScheduleStatus.AVAILABLE) {
            createBookingErrorCounter.increment();
            throw new BadRequestException("Schedule is no longer available");
          }

          // Update the schedule
          schedule.setAvailableSlots(schedule.getAvailableSlots() - 1);
          booking.setSchedule(schedule);
          booking.setAppointmentNumber(schedule.getCapacity() - schedule.getAvailableSlots());

          if (schedule.getAvailableSlots() == 0) {
            schedule.setStatus(ScheduleStatus.FULL);
          }

          // Save both entities
          scheduleRepository.save(schedule);
          Booking savedBooking = bookingRepository.save(booking);

          // Process confirmation
          BookingResponseDTO response = mapToResponse(savedBooking);
          emailService.sendBookingConfirmation(response);
          createBookingCounter.increment();

          return response;

        } catch (ObjectOptimisticLockingFailureException | StaleObjectStateException e) {
          log.info("Concurrent booking detected, attempt {}/{}", attempt + 1, maxRetries);

          if (attempt >= maxRetries - 1) {
            createBookingErrorCounter.increment();
            throw new BadRequestException("System is currently busy. Please try again shortly.");
          }

          // Add a delay before retry
          try {
            Thread.sleep((long) Math.pow(2, (double) attempt + 1) * 50);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new BadRequestException("Booking process was interrupted.");
          }
        } catch (PessimisticLockingFailureException e) {
          log.warn("Lock acquisition failure, attempt {}/{}", attempt + 1, maxRetries);

          if (attempt >= maxRetries - 1) {
            createBookingErrorCounter.increment();
            throw new BadRequestException("System is experiencing high demand. Please try again.");
          }

          try {
            Thread.sleep((long) Math.pow(2, (double)attempt + 1) * 100);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new BadRequestException("Booking process was interrupted.");
          }
        }
      }

      createBookingErrorCounter.increment();
      throw new BadRequestException("Unable to process booking request after multiple attempts.");

    } catch (BadRequestException | ResourceNotFoundException e) {
      throw e;
    } catch (Exception e) {
      createBookingErrorCounter.increment();
      log.error("Unexpected error during booking creation", e);
      throw new BadRequestException("Unable to create booking. Please try again later.");
    } finally {
      sample.stop(createBookingTimer);
    }
  }

  @Override
  public List<BookingResponseDTO> getAllBookings() {
    List<Booking> bookings = bookingRepository.findAll();
    if (bookings.isEmpty()) {
      throw new ResourceNotFoundException("No bookings found. Please create a booking to view the list.");
    }

    return bookings.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
  }

  @Override
  public BookingResponseDTO getBookingByReferenceIdAndContactNumber(String referenceId, String contactNumber) {
    return bookingRepository.findByReferenceIdAndContactNumber(referenceId, contactNumber)
            .map(this::mapToResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference ID " + referenceId + " and contact number " + contactNumber + ". Please verify the details and try again."));
  }

  @Override
  public BookingResponseDTO getBookingById(String id) {
    return bookingRepository.findById(id)
            .map(booking -> {
              modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
              return mapToResponse(booking);
            })
            .orElseThrow(() -> new ResourceNotFoundException("Booking with ID " + id + " not found. Please check the ID and try again."));
  }

  @Override
  @Transactional
  public void deleteBooking(String id) {
    Optional<Booking> booking = bookingRepository.findById(id);
    if (booking.isPresent()) {
      bookingRepository.deleteById(id);
    } else {
      throw new ResourceNotFoundException("Booking with ID " + id + " does not exist. Unable to delete.");
    }
  }

  @Override
  @Transactional
  public BookingResponseDTO updateBooking(String id, CreateBookingDTO bookingDTO) {
    Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking with ID " + id + " not found. Please check the ID and try again."));
    modelMapper.map(bookingDTO, booking);
    Booking updatedBooking = bookingRepository.save(booking);
    return mapToResponse(updatedBooking);
  }

  @Override
  @Transactional
  public BookingResponseDTO updateBookingStatus(String id, String status) {
    Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Booking with ID " + id + " not found. Please check the ID and try again."));
    BookingStatus updatedStatus;
    try {
      updatedStatus = BookingStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new BadRequestException("Invalid status. Please select a valid status.");
    }
    booking.setStatus(updatedStatus);
    Booking updatedBooking = bookingRepository.save(booking);
    return mapToResponse(updatedBooking);
  }

  @Override
  public MonthlyBookingStatsResponse getCurrentMonthBookingStats() {
    LocalDate now = LocalDate.now();
    String currentMonth = String.valueOf(now.getMonth());
    int currentYear = now.getYear();

    List<Booking> currentMonthBookings = bookingRepository.findAll().stream()
            .filter(booking -> {
              LocalDate bookingDate = booking.getDate();
              return Objects.equals(String.valueOf(bookingDate.getMonth()), currentMonth) &&
                      bookingDate.getYear() == currentYear;
            })
            .toList();

    int total = currentMonthBookings.size();
    int finished = (int) currentMonthBookings.stream()
            .filter(booking -> booking.getStatus() == BookingStatus.FINISHED)
            .count();
    int cancelled = (int) currentMonthBookings.stream()
            .filter(booking -> booking.getStatus() == BookingStatus.CANCELLED)
            .count();
    int pending = (int) currentMonthBookings.stream()
            .filter(booking -> booking.getStatus() == BookingStatus.PENDING)
            .count();

    return MonthlyBookingStatsResponse.builder()
            .month(currentMonth)
            .totalBookings(total)
            .finishedBookings(finished)
            .cancelledBookings(cancelled)
            .pendingBookings(pending)
            .build();
  }

  @Override
  @Transactional
  public PatientResponse getOrCreatePatientFromBookingId(String bookingId) {
    Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

    return getOrCreatePatientFromBooking(booking);
  }

  @Transactional
  @Override
  public PatientResponse getOrCreatePatientFromBooking(Booking booking) {
    String nic = booking.getNic();
    if (nic == null || nic.isEmpty()) {
      throw new BadRequestException("NIC is required to create or find a patient");
    }

    Optional<Patient> existingPatient = patientRepository.findByNic(nic);
    Patient patient;

    if (existingPatient.isPresent()) {
      log.info("Found existing patient with NIC: {}", nic);
      throw new BadRequestException("Patient with NIC " + nic + " already exists. Please use a different NIC.");
    } else {
      log.info("Creating new patient with NIC: {}", nic);
      patient = new Patient();
      patient.setName(booking.getName());
      patient.setEmail(booking.getEmail());
      patient.setNic(nic);
      patient.setContactNumbers(Collections.singletonList(booking.getContactNumber()));
      patient.setPatientLogs(Collections.emptyList());

      patient = patientRepository.save(patient);
      log.info("Created new patient: ID={}, NIC={}, Name={}", patient.getId(), nic, booking.getName());
    }

    PatientResponse response = new PatientResponse();
    response.setId(patient.getId());
    response.setName(patient.getName());
    response.setEmail(patient.getEmail());
    response.setNic(patient.getNic());
    response.setContactNumbers(patient.getContactNumbers());
    response.setLogs(Collections.emptyList());

    return response;
  }

  private Schedule getSchedule(Long scheduleId) {
    return scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new ResourceNotFoundException("Schedule with ID " + scheduleId + " not found. Please select a valid schedule."));
  }

  private BookingResponseDTO mapToResponse(Booking booking) {
    BookingResponseDTO bookingResponseDTO = modelMapper.map(booking, BookingResponseDTO.class);
    Long scheduleId = booking.getSchedule().getId();
    Schedule schedule = getSchedule(scheduleId);
    bookingResponseDTO.setScheduleDate(schedule.getDate());
    bookingResponseDTO.setScheduleDayOfWeek(schedule.getDayOfWeek());
    bookingResponseDTO.setScheduleStartTime(schedule.getStartTime());
    bookingResponseDTO.setDayOfWeek(schedule.getDayOfWeek());
    bookingResponseDTO.setDoctorName(schedule.getDentist().getFirstName());
    bookingResponseDTO.setScheduleStatus(schedule.getStatus());
    return bookingResponseDTO;
  }


}
