package com.uor.eng.service.impl;

import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.Dentist;
import com.uor.eng.model.Schedule;
import com.uor.eng.model.ScheduleStatus;
import com.uor.eng.payload.BookingResponseDTO;
import com.uor.eng.payload.CreateScheduleDTO;
import com.uor.eng.payload.ScheduleResponseDTO;
import com.uor.eng.repository.DentistRepository;
import com.uor.eng.repository.ScheduleRepository;
import com.uor.eng.service.IScheduleService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements IScheduleService {

  @Autowired
  private ScheduleRepository scheduleRepository;

  @Autowired
  private ModelMapper modelMapper;

  @Autowired
  private DentistRepository dentistRepository;

  @Override
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

    if (status == ScheduleStatus.FINISHED || status == ScheduleStatus.CANCELLED) {
      throw new BadRequestException("Cannot create a schedule with status " + status +
          ". Allowed statuses: AVAILABLE, UNAVAILABLE, FULL.");
    }

    Schedule schedule = new Schedule();
    schedule.setDate(scheduleDTO.getDate());
    schedule.setDayOfWeek(dayOfWeek);
    schedule.setStatus(status);
    schedule.setStartTime(scheduleDTO.getStartTime());
    schedule.setEndTime(scheduleDTO.getEndTime());
    schedule.setDentist(dentist);
    schedule.setCapacity(scheduleDTO.getCapacity());

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

    schedule.setStatus(updatedStatus);
    if (scheduleDTO.getStartTime() != null) {
      schedule.setStartTime(scheduleDTO.getStartTime());
    }
    if (scheduleDTO.getEndTime() != null) {
      schedule.setEndTime(scheduleDTO.getEndTime());
    }
    if (scheduleDTO.getCapacity() != null) {
      schedule.setCapacity(scheduleDTO.getCapacity());
    }
    Schedule updatedSchedule = scheduleRepository.save(schedule);
    ScheduleResponseDTO responseDTO = modelMapper.map(updatedSchedule, ScheduleResponseDTO.class);
    responseDTO.setNumberOfBookings(updatedSchedule.getBookings() != null ? updatedSchedule.getBookings().size() : 0);
    return responseDTO;
  }

  @Override
  public List<ScheduleResponseDTO> getNextSevenSchedules() {
    LocalDate today = LocalDate.now();
    List<Schedule> schedules = scheduleRepository.findTop7ByDateGreaterThanOrderByDateAsc(today);
    if (schedules.isEmpty()) {
      throw new ResourceNotFoundException("No schedules found for the next 7 days.");
    }
    return schedules.stream().map(schedule -> {
      ScheduleResponseDTO scheduleDTO = modelMapper.map(schedule, ScheduleResponseDTO.class);
      scheduleDTO.setNumberOfBookings(schedule.getBookings() != null ? schedule.getBookings().size() : 0);
      return scheduleDTO;
    }).collect(Collectors.toList());
  }
}
