package com.uor.eng.payload.schedule;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleGetSevenCustomResponse {
  private Long id;
  private LocalDate date;
  private String dayOfWeek;
  private LocalTime startTime;
}
