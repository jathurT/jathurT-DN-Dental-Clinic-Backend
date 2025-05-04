package com.uor.eng.service.impl;

import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.*;
import com.uor.eng.payload.booking.BookingResponseDTO;
import com.uor.eng.payload.dashboard.CancelledScheduleResponse;
import com.uor.eng.payload.dashboard.ScheduleHistoryResponse;
import com.uor.eng.payload.dashboard.UpcomingScheduleResponse;
import com.uor.eng.payload.schedule.CreateScheduleDTO;
import com.uor.eng.payload.schedule.ScheduleGetSevenCustomResponse;
import com.uor.eng.payload.schedule.ScheduleResponseDTO;
import com.uor.eng.repository.BookingRepository;
import com.uor.eng.repository.DentistRepository;
import com.uor.eng.repository.ScheduleRepository;
import com.uor.eng.service.IScheduleService;
import com.uor.eng.util.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ScheduleServiceImpl implements IScheduleService {

  private final ScheduleRepository scheduleRepository;
  private final ModelMapper modelMapper;
  private final DentistRepository dentistRepository;
  private final EmailService emailService;
  private final BookingRepository bookingRepository;

  public ScheduleServiceImpl(ScheduleRepository scheduleRepository,
                             ModelMapper modelMapper,
                             DentistRepository dentistRepository,
                             EmailService emailService,
                             BookingRepository bookingRepository) {
    this.scheduleRepository = scheduleRepository;
    this.modelMapper = modelMapper;
    this.dentistRepository = dentistRepository;
    this.emailService = emailService;
    this.bookingRepository = bookingRepository;
  }

  @Override
  @Transactional
  public ScheduleResponseDTO createSchedule(CreateScheduleDTO scheduleDTO) {
    if (scheduleDTO == null) {
      throw new BadRequestException("Schedule data cannot be null.");
    }

    LocalDate date = scheduleDTO.getDate();
    if (date == null) {
      throw new BadRequestException("Schedule date cannot be null.");
    }

    String dayOfWeek = date.getDayOfWeek().toString();
    dayOfWeek = dayOfWeek.charAt(0) + dayOfWeek.substring(1).toLowerCase();

    Long dentistId = scheduleDTO.getDentistId();
    Dentist dentist = dentistRepository.findById(dentistId)
            .orElseThrow(() -> new BadRequestException("Dentist with ID " + dentistId + " not found."));

    ScheduleStatus status;
    try {
      status = ScheduleStatus.valueOf(scheduleDTO.getStatus().toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new BadRequestException("Invalid schedule status: " + scheduleDTO.getStatus() +
              ". Allowed statuses: AVAILABLE, UNAVAILABLE, CANCELLED, FULL, FINISHED.");
    }

    if (status == ScheduleStatus.FINISHED || status == ScheduleStatus.CANCELLED || status == ScheduleStatus.FULL) {
      throw new BadRequestException("Cannot create a schedule with status " + status +
              ". Allowed statuses: AVAILABLE, UNAVAILABLE");
    }

    Schedule schedule = new Schedule();
    schedule.setDate(date);
    schedule.setDayOfWeek(dayOfWeek);
    schedule.setStatus(status);
    schedule.setStartTime(scheduleDTO.getStartTime());
    schedule.setEndTime(scheduleDTO.getEndTime());
    schedule.setDentist(dentist);
    schedule.setCapacity(scheduleDTO.getCapacity());
    schedule.setAvailableSlots(scheduleDTO.getCapacity());

    Schedule savedSchedule = scheduleRepository.save(schedule);
    ScheduleResponseDTO responseDTO = modelMapper.map(savedSchedule, ScheduleResponseDTO.class);

    responseDTO.setDentistId(dentist.getUserId());
    responseDTO.setNumberOfBookings(savedSchedule.getBookings() != null ? savedSchedule.getBookings().size() : 0);

    return responseDTO;
  }

  @Override
  public List<ScheduleResponseDTO> getAllSchedules() {
    List<Schedule> schedules = scheduleRepository.findAll();
    if (schedules.isEmpty()) {
      throw new ResourceNotFoundException("No schedules found.");
    }
    return schedules.stream().map(schedule -> {
      ScheduleResponseDTO scheduleDTO = modelMapper.map(schedule, ScheduleResponseDTO.class);
      scheduleDTO.setNumberOfBookings(schedule.getBookings() != null ? schedule.getBookings().size() : 0);
      return scheduleDTO;
    }).collect(Collectors.toList());
  }

  @Override
  public ScheduleResponseDTO getScheduleById(Long id) {
    Schedule schedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Schedule with ID " + id + " not found."));
    return modelMapper.map(schedule, ScheduleResponseDTO.class);
  }

  @Override
  @Transactional
  public void deleteSchedule(Long id) {
    Schedule schedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Schedule with ID " + id + " not found."));
    if (schedule.getBookings() != null && !schedule.getBookings().isEmpty()) {
      throw new BadRequestException("Cannot delete schedule with ID " + id + " because it has bookings.");
    } else {
      scheduleRepository.deleteById(id);
    }
  }

  @Override
  @Transactional
  public ScheduleResponseDTO updateSchedule(Long id, CreateScheduleDTO scheduleDTO) {
    Schedule schedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Schedule with ID " + id + " not found."));

    // Get the current status of the schedule
    ScheduleStatus currentStatus = schedule.getStatus();

    // Get the requested status update
    ScheduleStatus requestedStatus;
    try {
      requestedStatus = ScheduleStatus.valueOf(scheduleDTO.getStatus().toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new BadRequestException("Invalid schedule status: " + scheduleDTO.getStatus() +
              ". Allowed statuses: AVAILABLE, UNAVAILABLE, CANCELLED, FULL, FINISHED.");
    }

    // Check if the status transition is allowed
    validateStatusTransition(currentStatus, requestedStatus);

    // Update date if provided
    LocalDate date = scheduleDTO.getDate();
    if (date != null) {
      schedule.setDate(date);
      schedule.setDayOfWeek(date.getDayOfWeek().toString());
    }

    // Apply the status change and trigger related actions
    scheduleUpdateTriggerActions(schedule, requestedStatus, scheduleDTO.getCapacity());

    // Update other fields if provided
    if (scheduleDTO.getStartTime() != null) {
      schedule.setStartTime(scheduleDTO.getStartTime());
    }
    if (scheduleDTO.getEndTime() != null) {
      schedule.setEndTime(scheduleDTO.getEndTime());
    }
    if (scheduleDTO.getCapacity() != null) {
      schedule.setCapacity(scheduleDTO.getCapacity());
    }
    if (scheduleDTO.getDentistId() != null) {
      Dentist dentist = dentistRepository.findById(scheduleDTO.getDentistId())
              .orElseThrow(() -> new BadRequestException("Dentist with ID " + scheduleDTO.getDentistId() + " not found."));
      schedule.setDentist(dentist);
    }

    Schedule updatedSchedule = scheduleRepository.save(schedule);
    ScheduleResponseDTO responseDTO = modelMapper.map(updatedSchedule, ScheduleResponseDTO.class);
    responseDTO.setNumberOfBookings(updatedSchedule.getBookings() != null ? updatedSchedule.getBookings().size() : 0);
    return responseDTO;
  }

  private void validateStatusTransition(ScheduleStatus currentStatus, ScheduleStatus requestedStatus) {
    // List of statuses that cannot be transitioned back to active-like states
    List<ScheduleStatus> finalStates = List.of(ScheduleStatus.CANCELLED, ScheduleStatus.FINISHED);

    // List of statuses that represent active-like states
    List<ScheduleStatus> activeStates = List.of(
            ScheduleStatus.AVAILABLE,
            ScheduleStatus.FULL,
            ScheduleStatus.UNAVAILABLE
    );

    // Check if current status is final and requested status is active
    if (finalStates.contains(currentStatus) && activeStates.contains(requestedStatus)) {
      throw new BadRequestException(
              "Cannot change schedule status from " + currentStatus + " to " + requestedStatus + ". " +
                      "Schedules with status " + currentStatus + " cannot be changed to " +
                      activeStates.stream().map(Enum::name).collect(Collectors.joining(", ")) + "."
      );
    }
  }

  @Override
  public List<ScheduleResponseDTO> getNextSevenSchedules() {
    LocalDate today = LocalDate.now();
    List<Schedule> schedules = scheduleRepository.findTop7ByDateGreaterThanAndStatusOrderByDateAsc(
            today, ScheduleStatus.AVAILABLE
    );
    if (schedules.isEmpty()) {
      throw new ResourceNotFoundException("No schedules found for the next 7 days.");
    }
    return schedules.stream().map(schedule -> {
      ScheduleResponseDTO scheduleDTO = modelMapper.map(schedule, ScheduleResponseDTO.class);
      scheduleDTO.setNumberOfBookings(schedule.getBookings() != null ? schedule.getBookings().size() : 0);
      return scheduleDTO;
    }).collect(Collectors.toList());
  }

  @Override
  public List<ScheduleGetSevenCustomResponse> getNextSevenSchedulesCustom() {
    LocalDate today = LocalDate.now();
    List<Schedule> schedules = scheduleRepository.findTop7ByDateGreaterThanAndStatusOrderByDateAsc(
            today, ScheduleStatus.AVAILABLE
    );
    if (schedules.isEmpty()) {
      throw new ResourceNotFoundException("No schedules found for the next 7 days.");
    }
    List<Schedule> availableSchedule = schedules.stream().filter(schedule -> schedule.getStatus() == ScheduleStatus.AVAILABLE).toList();
    return availableSchedule.stream().map(schedule -> {
      ScheduleGetSevenCustomResponse scheduleGetSevenCustomResponse = new ScheduleGetSevenCustomResponse();
      scheduleGetSevenCustomResponse.setDate(schedule.getDate());
      scheduleGetSevenCustomResponse.setDayOfWeek(schedule.getDayOfWeek());
      scheduleGetSevenCustomResponse.setStartTime(schedule.getStartTime());
      scheduleGetSevenCustomResponse.setId(schedule.getId());
      return scheduleGetSevenCustomResponse;
    }).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void updateExpiredSchedules() {
    LocalDate today = LocalDate.now();
    LocalTime nowTime = LocalTime.now();
    List<ScheduleStatus> excludedStatuses = List.of(
            ScheduleStatus.ON_GOING,
            ScheduleStatus.FULL,
            ScheduleStatus.FINISHED,
            ScheduleStatus.CANCELLED
    );

    log.info("Updating expired schedules for date: {} and time: {}", today, nowTime);
    List<Schedule> expiredSchedules = scheduleRepository.findSchedulesToFinish(today, nowTime, excludedStatuses);

    if (!expiredSchedules.isEmpty()) {
      for (Schedule schedule : expiredSchedules) {
        processExpiredSchedule(schedule);
      }
      scheduleRepository.saveAll(expiredSchedules);
      log.info("Updated {} schedules to FINISHED", expiredSchedules.size());
    } else {
      log.debug("No schedules to update at this time.");
    }
  }

  @Override
  @Transactional
  public void initialUpdaterScheduleOnStartup() {
    LocalDate today = LocalDate.now();
    LocalTime nowTime = LocalTime.now();

    // Find schedules that are still AVAILABLE but should be cancelled
    List<Schedule> schedules = scheduleRepository.findByDate(today);
    List<Schedule> expiredSchedules = schedules.stream()
            .filter(schedule -> schedule.getStatus() == ScheduleStatus.AVAILABLE && schedule.getStartTime().isBefore(nowTime))
            .toList();

    log.info("Found {} expired schedules that need to be cancelled", expiredSchedules.size());

    if (!expiredSchedules.isEmpty()) {
      for (Schedule schedule : expiredSchedules) {
        log.info("Cancelling expired schedule ID: {}, scheduled for {} at {}",
                schedule.getId(), schedule.getDate(), schedule.getStartTime());

        schedule.setStatus(ScheduleStatus.CANCELLED);

        // Update any associated bookings
        if (schedule.getBookings() != null && !schedule.getBookings().isEmpty()) {
          for (Booking booking : schedule.getBookings()) {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            try {
              BookingResponseDTO bookingResponseDTO = mapToResponse(booking);
              emailService.sendBookingCancellation(bookingResponseDTO);
              log.info("Sent cancellation email for booking ID: {}", booking.getReferenceId());
            } catch (Exception e) {
              log.error("Failed to send cancellation email for booking ID: {}", booking.getReferenceId(), e);
            }
          }
        }
        scheduleRepository.save(schedule);
      }
      log.info("Successfully cancelled {} expired schedules", expiredSchedules.size());
    } else {
      log.debug("No expired schedules found to cancel");
    }
  }

  private void processExpiredSchedule(Schedule schedule) {
    schedule.setStatus(ScheduleStatus.FINISHED);

    if (schedule.getBookings() != null && !schedule.getBookings().isEmpty()) {
      for (Booking booking : schedule.getBookings()) {
        if (booking.getStatus() != BookingStatus.CANCELLED) {
          booking.setStatus(BookingStatus.FINISHED);
          bookingRepository.save(booking);
          log.debug("Booking ID {} marked as FINISHED", booking.getReferenceId());
        }
      }
    }
    log.debug("Schedule ID {} marked as FINISHED", schedule.getId());
  }

  @Override
  @Transactional
  public void processDailySchedules() {
    LocalDate today = LocalDate.now();
    // Process schedules for the day
    List<Schedule> todaySchedules = scheduleRepository.findByDate(today);

    log.info("Processing daily schedules for date: {}", today);

    // Add any daily processing logic here
    // For example, you could check for schedules that need to be activated
    for (Schedule schedule : todaySchedules) {
      if (schedule.getStatus() == ScheduleStatus.UNAVAILABLE &&
              schedule.getDate().isEqual(today)) {
        // Mark as available if it's today and was unavailable
        schedule.setStatus(ScheduleStatus.AVAILABLE);
        scheduleRepository.save(schedule);
        log.debug("Schedule ID {} activated for today", schedule.getId());
      }
    }
  }

  @Override
  @Transactional
  public void sendAppointmentReminders() {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    List<Schedule> tomorrowSchedules = scheduleRepository.findByDate(tomorrow);

    log.info("Sending appointment reminders for date: {}", tomorrow);

    for (Schedule schedule : tomorrowSchedules) {
      for (Booking booking : schedule.getBookings()) {
        if (booking.getStatus() == BookingStatus.ACTIVE || booking.getStatus() == BookingStatus.PENDING) {
          try {
            BookingResponseDTO bookingDTO = mapToResponse(booking);
            emailService.sendAppointmentReminder(bookingDTO);
            log.debug("Sent reminder for booking ID: {}", booking.getReferenceId());
          } catch (Exception e) {
            log.error("Failed to send reminder for booking ID: {}", booking.getReferenceId(), e);
          }
        }
      }
    }
  }

  @Override
  @Transactional
  public ScheduleResponseDTO updateScheduleStatus(Long id, String status) {
    Schedule schedule = scheduleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Schedule with ID " + id + " not found."));

    ScheduleStatus currentStatus = schedule.getStatus();
    ScheduleStatus requestedStatus;

    try {
      requestedStatus = ScheduleStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new BadRequestException("Invalid schedule status: " + status +
              ". Allowed statuses: AVAILABLE, UNAVAILABLE, CANCELLED, FULL, FINISHED.");
    }

    // Validate the status transition is allowed
    validateStatusTransition(currentStatus, requestedStatus);

    scheduleUpdateTriggerActions(schedule, requestedStatus, schedule.getCapacity());
    Schedule updatedSchedule = scheduleRepository.save(schedule);
    ScheduleResponseDTO responseDTO = modelMapper.map(updatedSchedule, ScheduleResponseDTO.class);
    responseDTO.setNumberOfBookings(updatedSchedule.getBookings() != null ? updatedSchedule.getBookings().size() : 0);
    return responseDTO;
  }

  @Override
  public List<CancelledScheduleResponse> getCancelledSchedules() {
    Pageable topTen = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "date", "startTime"));
    Page<Schedule> cancelledSchedules = scheduleRepository.findByStatus(ScheduleStatus.CANCELLED, topTen);

    if (cancelledSchedules.isEmpty()) {
      throw new ResourceNotFoundException("No cancelled schedules found.");
    }

    return cancelledSchedules.getContent().stream()
            .map(schedule -> CancelledScheduleResponse.builder()
                    .startTime(String.valueOf(schedule.getStartTime()))
                    .endTime(String.valueOf(schedule.getEndTime()))
                    .date(String.valueOf(schedule.getDate()))
                    .build())
            .collect(Collectors.toList());
  }

  @Override
  public List<UpcomingScheduleResponse> getUpcomingSchedules() {
    Pageable topTen = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "date", "startTime"));
    Page<Schedule> upcomingSchedules = scheduleRepository.findByStatus(ScheduleStatus.AVAILABLE, topTen);

    if (upcomingSchedules.isEmpty()) {
      throw new ResourceNotFoundException("No upcoming schedules found.");
    }

    return upcomingSchedules.getContent().stream()
            .map(schedule -> UpcomingScheduleResponse.builder()
                    .startTime(String.valueOf(schedule.getStartTime()))
                    .endTime(String.valueOf(schedule.getEndTime()))
                    .date(String.valueOf(schedule.getDate()))
                    .appointmentCount(schedule.getBookings().size())
                    .build())
            .collect(Collectors.toList());
  }

  @Override
  public List<ScheduleHistoryResponse> getScheduleHistory() {
    LocalDate today = LocalDate.now();
    LocalDate startDate = today.minusDays(89);

    List<Schedule> scheduleHistory = scheduleRepository.findByDateBetweenAndStatusNot(
            startDate, today, ScheduleStatus.AVAILABLE);

    Map<LocalDate, Integer> bookingCountByDate = scheduleHistory.stream()
            .collect(Collectors.groupingBy(
                    Schedule::getDate,
                    Collectors.summingInt(schedule -> schedule.getBookings().size())
            ));

    List<ScheduleHistoryResponse> result = new ArrayList<>();
    for (int i = 0; i < 90; i++) {
      LocalDate date = today.minusDays(i);
      int bookingCount = bookingCountByDate.getOrDefault(date, 0);
      result.add(new ScheduleHistoryResponse(date.toString(), bookingCount));
    }

    return result;
  }

  private void scheduleUpdateTriggerActions(Schedule schedule, ScheduleStatus updatedStatus, Integer capacity) {
    schedule.setStatus(updatedStatus);

    List<Booking> bookings = schedule.getBookings();
    if (updatedStatus == ScheduleStatus.CANCELLED) {
      schedule.setAvailableSlots(0);
      bookings.forEach(booking -> {
        booking.setStatus(BookingStatus.CANCELLED);
        BookingResponseDTO bookingResponseDTO = mapToResponse(booking);
        emailService.sendBookingCancellation(bookingResponseDTO);
      });
    } else if (updatedStatus == ScheduleStatus.FINISHED) {
      schedule.setAvailableSlots(0);
      bookings.forEach(booking -> booking.setStatus(BookingStatus.FINISHED));
    } else if (updatedStatus == ScheduleStatus.FULL) {
      schedule.setAvailableSlots(0);
    } else if (updatedStatus == ScheduleStatus.ACTIVE) {
      schedule.setAvailableSlots(0);
      bookings.forEach(booking -> {
        booking.setStatus(BookingStatus.ACTIVE);
        BookingResponseDTO bookingResponseDTO = mapToResponse(booking);
        emailService.sendBookingActivation(bookingResponseDTO);
      });
    } else {
      schedule.setAvailableSlots(capacity - bookings.size());
    }
  }

  private Schedule getSchedule(Long scheduleId) {
    return scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new ResourceNotFoundException("Schedule with ID " + scheduleId + " not found. Please select a valid schedule."));
  }

  private BookingResponseDTO mapToResponse(Booking booking) {
    BookingResponseDTO bookingResponseDTO = modelMapper.map(booking, BookingResponseDTO.class);
    Long scheduleId = booking.getSchedule().getId();
    Schedule schedule = getSchedule(scheduleId);

    // Set the fields properly
    bookingResponseDTO.setScheduleId(scheduleId);
    bookingResponseDTO.setScheduleDate(schedule.getDate());
    bookingResponseDTO.setScheduleDayOfWeek(schedule.getDayOfWeek());
    bookingResponseDTO.setScheduleStartTime(schedule.getStartTime());
    bookingResponseDTO.setDoctorName(schedule.getDentist().getFirstName());
    bookingResponseDTO.setScheduleStatus(schedule.getStatus());
    bookingResponseDTO.setDayOfWeek(schedule.getDayOfWeek());

    return bookingResponseDTO;
  }
}