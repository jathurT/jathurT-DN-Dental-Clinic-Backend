package com.uor.eng.controller;

import com.uor.eng.payload.dashboard.CancelledScheduleResponse;
import com.uor.eng.payload.dashboard.ScheduleHistoryResponse;
import com.uor.eng.payload.dashboard.UpcomingScheduleResponse;
import com.uor.eng.payload.schedule.CreateScheduleDTO;
import com.uor.eng.payload.schedule.ScheduleGetSevenCustomResponse;
import com.uor.eng.payload.schedule.ScheduleResponseDTO;
import com.uor.eng.service.IScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

  private final IScheduleService scheduleService;

  public ScheduleController(IScheduleService scheduleService) {
    this.scheduleService = scheduleService;
  }

  @PostMapping("/create")
  public ResponseEntity<ScheduleResponseDTO> createSchedule(@RequestBody CreateScheduleDTO scheduleDTO) {
    ScheduleResponseDTO createdSchedule = scheduleService.createSchedule(scheduleDTO);
    return new ResponseEntity<>(createdSchedule, HttpStatus.CREATED);
  }

  @GetMapping("/all")
  public ResponseEntity<List<ScheduleResponseDTO>> getAllSchedules() {
    List<ScheduleResponseDTO> schedules = scheduleService.getAllSchedules();
    return new ResponseEntity<>(schedules, HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ScheduleResponseDTO> getScheduleById(@PathVariable Long id) {
    ScheduleResponseDTO schedule = scheduleService.getScheduleById(id);
    return new ResponseEntity<>(schedule, HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
    scheduleService.deleteSchedule(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ScheduleResponseDTO> updateSchedule(@PathVariable Long id, @RequestBody CreateScheduleDTO scheduleDTO) {
    ScheduleResponseDTO updatedSchedule = scheduleService.updateSchedule(id, scheduleDTO);
    return new ResponseEntity<>(updatedSchedule, HttpStatus.OK);
  }

  @GetMapping("/getSeven")
  public ResponseEntity<List<ScheduleResponseDTO>> getNextSevenSchedules() {
    List<ScheduleResponseDTO> schedules = scheduleService.getNextSevenSchedules();
    return new ResponseEntity<>(schedules, HttpStatus.OK);
  }

  @GetMapping("/getSevenCustom")
  public ResponseEntity<List<ScheduleGetSevenCustomResponse>> getNextSevenSchedulesCustom() {
    List<ScheduleGetSevenCustomResponse> schedules = scheduleService.getNextSevenSchedulesCustom();
    return new ResponseEntity<>(schedules, HttpStatus.OK);
  }

  @PutMapping("/updateStatus/{id}")
  public ResponseEntity<ScheduleResponseDTO> updateScheduleStatus(@PathVariable Long id, @RequestParam String status) {
    ScheduleResponseDTO updatedSchedule = scheduleService.updateScheduleStatus(id, status);
    return new ResponseEntity<>(updatedSchedule, HttpStatus.OK);
  }

  @GetMapping("/cancelledSchedules")
  public ResponseEntity<List<CancelledScheduleResponse>> getCancelledSchedules() {
    List<CancelledScheduleResponse> cancelledSchedules = scheduleService.getCancelledSchedules();
    return new ResponseEntity<>(cancelledSchedules, HttpStatus.OK);
  }

  @GetMapping("/upcomingSchedules")
  public ResponseEntity<List<UpcomingScheduleResponse>> getUpcomingSchedules() {
    List<UpcomingScheduleResponse> upcomingSchedules = scheduleService.getUpcomingSchedules();
    return new ResponseEntity<>(upcomingSchedules, HttpStatus.OK);
  }

  @GetMapping("/scheduleHistory")
  public ResponseEntity<List<ScheduleHistoryResponse>> getScheduleHistory() {
    List<ScheduleHistoryResponse> scheduleHistory = scheduleService.getScheduleHistory();
    return new ResponseEntity<>(scheduleHistory, HttpStatus.OK);
  }


}
