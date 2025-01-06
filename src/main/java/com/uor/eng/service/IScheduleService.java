package com.uor.eng.service;

import com.uor.eng.payload.schedule.CreateScheduleDTO;
import com.uor.eng.payload.schedule.ScheduleGetSevenCustomResponse;
import com.uor.eng.payload.schedule.ScheduleResponseDTO;

import java.util.List;

public interface IScheduleService {
  ScheduleResponseDTO createSchedule(CreateScheduleDTO scheduleDTO);

  List<ScheduleResponseDTO> getAllSchedules();

  ScheduleResponseDTO getScheduleById(Long id);

  void deleteSchedule(Long id);

  ScheduleResponseDTO updateSchedule(Long id, CreateScheduleDTO scheduleDTO);

  List<ScheduleResponseDTO> getNextSevenSchedules();

  List<ScheduleGetSevenCustomResponse> getNextSevenSchedulesCustom();
}
