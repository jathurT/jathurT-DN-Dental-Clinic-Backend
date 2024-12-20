package com.uor.eng.service.impl;

import com.uor.eng.model.Schedule;
import com.uor.eng.payload.ScheduleDTO;
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
    Schedule schedule = modelMapper.map(scheduleDTO, Schedule.class);
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
