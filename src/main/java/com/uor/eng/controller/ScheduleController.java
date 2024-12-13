package com.uor.eng.controller;

import com.uor.eng.payload.ScheduleDTO;
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
  public ResponseEntity<ScheduleDTO> createSchedule(@RequestBody ScheduleDTO scheduleDTO) {
    ScheduleDTO createdSchedule = scheduleService.createSchedule(scheduleDTO);
    return new ResponseEntity<>(createdSchedule, HttpStatus.CREATED);
  }

  @GetMapping("/all")
  public ResponseEntity<List<ScheduleDTO>> getAllSchedules() {
    List<ScheduleDTO> schedules = scheduleService.getAllSchedules();
    return new ResponseEntity<>(schedules, HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ScheduleDTO> getScheduleById(@PathVariable Long id) {
    ScheduleDTO schedule = scheduleService.getScheduleById(id);
    return new ResponseEntity<>(schedule, HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
    scheduleService.deleteSchedule(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
