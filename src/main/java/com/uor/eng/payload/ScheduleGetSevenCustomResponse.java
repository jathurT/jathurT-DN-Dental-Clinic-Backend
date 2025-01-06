package com.uor.eng.payload;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleGetSevenCustomResponse {
  private LocalDate date;
  private String dayOfWeek;
  private LocalTime startTime;
}
