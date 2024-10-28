package com.uor.eng.controller;

import com.uor.eng.payload.ScheduleDTO;
import com.uor.eng.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

  @Autowired
  private ScheduleService scheduleService;

  @PostMapping("/create")
  public ResponseEntity<?> createSchedule(@RequestBody ScheduleDTO scheduleDTO) {
    try {
      ScheduleDTO createdSchedule = scheduleService.createSchedule(scheduleDTO);
      return new ResponseEntity<>(createdSchedule, HttpStatus.CREATED);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @GetMapping("/all")
  public ResponseEntity<?> getAllSchedules() {
    try {
      List<ScheduleDTO> schedules = scheduleService.getAllSchedules();
      return new ResponseEntity<>(schedules, HttpStatus.OK);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NO_CONTENT);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getScheduleById(@PathVariable Long id) {
    try {
      ScheduleDTO schedule = scheduleService.getScheduleById(id);
      return new ResponseEntity<>(schedule, HttpStatus.OK);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
  }
}
