package com.uor.eng.controller;

import com.uor.eng.payload.CreateScheduleDTO;
import com.uor.eng.payload.ScheduleGetSevenCustomResponse;
import com.uor.eng.payload.ScheduleResponseDTO;
import com.uor.eng.service.IScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

  @Autowired
  private IScheduleService scheduleService;

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

}
