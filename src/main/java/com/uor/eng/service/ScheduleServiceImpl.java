package com.uor.eng.service;

import com.uor.eng.model.Schedule;
import com.uor.eng.payload.ScheduleDTO;
import com.uor.eng.repository.ScheduleRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

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
    return scheduleRepository.findAll().stream()
            .map(schedule -> modelMapper.map(schedule, ScheduleDTO.class))
            .collect(Collectors.toList());
  }

  @Override
  public ScheduleDTO getScheduleById(Long id) {
    Schedule schedule = scheduleRepository.findById(id).orElse(null);
    return modelMapper.map(schedule, ScheduleDTO.class);
  }
}
