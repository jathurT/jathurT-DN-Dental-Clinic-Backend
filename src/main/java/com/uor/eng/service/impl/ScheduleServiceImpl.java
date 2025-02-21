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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class ScheduleServiceImpl implements IScheduleService {

  @Autowired
  private ScheduleRepository scheduleRepository;

  @Autowired
  private ModelMapper modelMapper;

  @Autowired
  private DentistRepository dentistRepository;

  @Autowired
  private EmailService emailService;

  @Autowired
  private BookingRepository bookingRepository;

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
    LocalDate date = scheduleDTO.getDate();
    if (date != null) {
      schedule.setDate(date);
      schedule.setDayOfWeek(date.getDayOfWeek().toString());
    }
    ScheduleStatus updatedStatus;
    try {
      updatedStatus = ScheduleStatus.valueOf(scheduleDTO.getStatus().toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new BadRequestException("Invalid schedule status: " + scheduleDTO.getStatus() +
          ". Allowed statuses: AVAILABLE, UNAVAILABLE, CANCELLED, FULL, FINISHED.");
    }
    scheduleUpdateTriggerActions(schedule, updatedStatus, scheduleDTO.getCapacity());

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
  @Scheduled(cron = "0 0/15 * * * ?")
  public void initialUpdaterScheduleOnStartup() {
    LocalDate today = LocalDate.now();
    LocalTime nowTime = LocalTime.now();
    List<ScheduleStatus> excludedStatuses = List.of(ScheduleStatus.ON_GOING, ScheduleStatus.FULL);

    log.info("Updating schedule statuses for date: {} and time: {}", today, nowTime);
    List<Schedule> schedulesToFinish = scheduleRepository.findSchedulesToFinish(today, nowTime, excludedStatuses);

    if (!schedulesToFinish.isEmpty()) {
      for (Schedule schedule : schedulesToFinish) {
        schedule.setStatus(ScheduleStatus.FINISHED);
        List<Booking> bookings = schedule.getBookings();
        if (bookings != null && !bookings.isEmpty()) {
          bookings.forEach(booking -> {
            booking.setStatus(BookingStatus.FINISHED);
            bookingRepository.save(booking);
            log.info("Booking ID {} marked as FINISHED", booking.getReferenceId());
          });
        }
        log.info("Schedule ID {} marked as FINISHED", schedule.getId());
      }
      scheduleRepository.saveAll(schedulesToFinish);
      log.info("Updated {} schedules to FINISHED", schedulesToFinish.size());
    } else {
      log.info("No schedules to update at this time.");
    }
  }

  @Override
  @Transactional
  public ScheduleResponseDTO updateScheduleStatus(Long id, String status) {
    Schedule schedule = scheduleRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Schedule with ID " + id + " not found."));
    ScheduleStatus updatedStatus;
    try {
      updatedStatus = ScheduleStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new BadRequestException("Invalid schedule status: " + status +
          ". Allowed statuses: AVAILABLE, UNAVAILABLE, CANCELLED, FULL, FINISHED.");
    }
    scheduleUpdateTriggerActions(schedule, updatedStatus, schedule.getCapacity());
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
      bookings.forEach(booking -> {
        booking.setStatus(BookingStatus.FINISHED);
      });
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
    BookingResponseDTO.builder()
        .scheduleId(scheduleId)
        .scheduleDate(schedule.getDate())
        .scheduleDayOfWeek(schedule.getDayOfWeek())
        .scheduleStartTime(schedule.getStartTime())
        .doctorName(schedule.getDentist().getFirstName())
        .scheduleStatus(schedule.getStatus())
        .dayOfWeek(schedule.getDayOfWeek())
        .status(booking.getStatus())
        .build();
    return bookingResponseDTO;
  }
}
