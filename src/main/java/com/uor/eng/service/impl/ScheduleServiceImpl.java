package com.uor.eng.service.impl;

import com.uor.eng.model.Schedule;
import com.uor.eng.payload.ScheduleDTO;
import com.uor.eng.repository.ScheduleRepository;
import com.uor.eng.service.IScheduleService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    Schedule schedule = modelMapper.map(scheduleDTO, Schedule.class);
    Schedule savedSchedule = scheduleRepository.save(schedule);
    return modelMapper.map(savedSchedule, ScheduleDTO.class);
  }

  @Override
  public List<ScheduleDTO> getAllSchedules() {
    List<Schedule> schedules = scheduleRepository.findAll();
    if (schedules.isEmpty()) {
      throw new RuntimeException("No schedules found. Please create a schedule to view the list.");
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
            .orElseThrow(() -> new RuntimeException("Schedule with ID " + id + " not found. Please check the ID and try again."));
    return modelMapper.map(schedule, ScheduleDTO.class);
  }
}
