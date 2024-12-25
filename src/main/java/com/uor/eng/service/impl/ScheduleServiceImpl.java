package com.uor.eng.service.impl;

import com.uor.eng.model.Dentist;
import com.uor.eng.model.Schedule;
import com.uor.eng.model.ScheduleStatus;
import com.uor.eng.payload.ScheduleDTO;
import com.uor.eng.repository.DentistRepository;
import com.uor.eng.repository.ScheduleRepository;
import com.uor.eng.service.IScheduleService;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.exceptions.BadRequestException;
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
  public ScheduleDTO createSchedule(ScheduleDTO scheduleDTO) {
    if (scheduleDTO == null) {
      throw new BadRequestException("Schedule data cannot be null.");
    }
    LocalDate date = scheduleDTO.getDate();
    if (date != null) {
      String dayOfWeek = date.getDayOfWeek().toString();
      scheduleDTO.setDayOfWeek(dayOfWeek);
    } else {
      throw new BadRequestException("Schedule date cannot be null.");
    }

    Long dentistId = scheduleDTO.getDentistId();
    Dentist dentist = dentistRepository.findById(dentistId)
        .orElseThrow(() -> new BadRequestException("Dentist with ID " + dentistId + " not found."));

    ScheduleStatus status;
    try {
      status = ScheduleStatus.valueOf(scheduleDTO.getStatus().toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new BadRequestException("Invalid schedule status: " + scheduleDTO.getStatus() + ". Allowed statuses: AVAILABLE, UNAVAILABLE, CANCELLED, FULL, FINISHED.");
    }

    if (status == ScheduleStatus.FINISHED || status == ScheduleStatus.CANCELLED) {
      throw new BadRequestException("Cannot create a schedule with status " + status + ". Allowed statuses: AVAILABLE, UNAVAILABLE, FULL.");
    }
    scheduleDTO.setStatus(String.valueOf(status));

    Schedule schedule = modelMapper.map(scheduleDTO, Schedule.class);
    schedule.setDentist(dentist);
    Schedule savedSchedule = scheduleRepository.save(schedule);

    return modelMapper.map(savedSchedule, ScheduleDTO.class);
  }


  @Override
  public List<ScheduleDTO> getAllSchedules() {
    List<Schedule> schedules = scheduleRepository.findAll();
    if (schedules.isEmpty()) {
      throw new ResourceNotFoundException("No schedules found.");
    }
    return schedules.stream().map(schedule -> {
      ScheduleDTO scheduleDTO = modelMapper.map(schedule, ScheduleDTO.class);
      scheduleDTO.setNumberOfBookings(schedule.getBookings() != null ? schedule.getBookings().size() : 0);
      return scheduleDTO;
    }).collect(Collectors.toList());
  }

  @Override
  public ScheduleDTO getScheduleById(Long id) {
    Schedule schedule = scheduleRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Schedule with ID " + id + " not found."));
    return modelMapper.map(schedule, ScheduleDTO.class);
  }

  @Override
  public void deleteSchedule(Long id) {
    if (scheduleRepository.existsById(id)) {
      scheduleRepository.deleteById(id);
    } else {
      throw new ResourceNotFoundException("Schedule with ID " + id + " not found. Unable to delete.");
    }
  }
}
