package com.uor.eng.service;

import com.uor.eng.payload.dashboard.CancelledScheduleResponse;
import com.uor.eng.payload.dashboard.ScheduleHistoryResponse;
import com.uor.eng.payload.dashboard.UpcomingScheduleResponse;
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

  ScheduleResponseDTO updateScheduleStatus(Long id, String status);

  List<CancelledScheduleResponse> getCancelledSchedules();

  List<UpcomingScheduleResponse> getUpcomingSchedules();

  List<ScheduleHistoryResponse> getScheduleHistory();

  void updateExpiredSchedules();

  void initialUpdaterScheduleOnStartup();

  void processDailySchedules();

  void sendAppointmentReminders();
}
