package com.uor.eng.service.impl;

import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.Booking;
import com.uor.eng.model.BookingStatus;
import com.uor.eng.model.Schedule;
import com.uor.eng.model.ScheduleStatus;
import com.uor.eng.payload.booking.BookingResponseDTO;
import com.uor.eng.payload.booking.CreateBookingDTO;
import com.uor.eng.payload.dashboard.MonthlyBookingStatsResponse;
import com.uor.eng.repository.BookingRepository;
import com.uor.eng.repository.ScheduleRepository;
import com.uor.eng.service.IBookingService;
import com.uor.eng.util.EmailService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements IBookingService {

  private final Counter createBookingCounter;
  private final Counter createBookingErrorCounter;
  private final Timer createBookingTimer;
  private final BookingRepository bookingRepository;
  private final ModelMapper modelMapper;
  private final ScheduleRepository scheduleRepository;
  private final EmailService emailService;

  public BookingServiceImpl(Counter createBookingCounter,
                            Counter createBookingErrorCounter,
                            Timer createBookingTimer,
                            BookingRepository bookingRepository,
                            ModelMapper modelMapper,
                            ScheduleRepository scheduleRepository,
                            EmailService emailService) {
    this.createBookingCounter = createBookingCounter;
    this.createBookingErrorCounter = createBookingErrorCounter;
    this.createBookingTimer = createBookingTimer;
    this.bookingRepository = bookingRepository;
    this.modelMapper = modelMapper;
    this.scheduleRepository = scheduleRepository;
    this.emailService = emailService;
  }

  @Override
  @Transactional
  public synchronized BookingResponseDTO createBooking(CreateBookingDTO bookingDTO) {
    Timer.Sample sample = Timer.start();
    try {
      Schedule schedule = scheduleRepository.findById(bookingDTO.getScheduleId())
          .orElseThrow(() -> {
            createBookingErrorCounter.increment();
            return new ResourceNotFoundException("Schedule with ID " + bookingDTO.getScheduleId() + " not found. Please select a valid schedule.");
          });

      if (schedule.getAvailableSlots() == 0 || schedule.getStatus() == ScheduleStatus.FULL) {
        createBookingErrorCounter.increment();
        throw new BadRequestException("Cannot create booking. The selected schedule is currently full.");
      } else if (schedule.getStatus() == ScheduleStatus.UNAVAILABLE ||
          schedule.getStatus() == ScheduleStatus.CANCELLED ||
          schedule.getStatus() == ScheduleStatus.FINISHED ||
          schedule.getStatus() == ScheduleStatus.ON_GOING ||
          schedule.getStatus() == ScheduleStatus.ACTIVE
      ) {
        createBookingErrorCounter.increment();
        throw new BadRequestException("Cannot create booking. The selected schedule is currently unavailable.");
      }

      Booking booking = modelMapper.map(bookingDTO, Booking.class);
      schedule.setAvailableSlots(schedule.getAvailableSlots() - 1);
      booking.setAppointmentNumber(schedule.getCapacity() - schedule.getAvailableSlots());

      if (schedule.getAvailableSlots() == 0) {
        schedule.setStatus(ScheduleStatus.FULL);
      }
      Booking savedBooking = bookingRepository.save(booking);
      scheduleRepository.save(schedule);

      BookingResponseDTO bookingResponseDTO = mapToResponse(savedBooking);
      emailService.sendBookingConfirmation(bookingResponseDTO);
      createBookingCounter.increment();
      return bookingResponseDTO;
    } catch (OptimisticLockingFailureException e) {
      createBookingErrorCounter.increment();
      throw new BadRequestException("Unable to create booking due to high demand. Please try again.");
    } catch (Exception e) {
      createBookingErrorCounter.increment();
      throw new BadRequestException("Unable to create booking. Please check the details and try again.");
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
