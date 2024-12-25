package com.uor.eng.service;

import com.uor.eng.payload.CreateScheduleDTO;
import com.uor.eng.payload.ScheduleResponseDTO;

import java.util.List;

public interface IScheduleService {
  ScheduleResponseDTO createSchedule(CreateScheduleDTO scheduleDTO);

  List<ScheduleResponseDTO> getAllSchedules();

  ScheduleResponseDTO getScheduleById(Long id);

  void deleteSchedule(Long id);
}
