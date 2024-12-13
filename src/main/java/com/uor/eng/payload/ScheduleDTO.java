package com.uor.eng.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDTO {
  private Long id;
  private LocalDate date;
  private String dayOfWeek;
  private String status;
  private Integer numberOfBookings;
  private List<BookingDTO> bookings;
  private LocalTime startTime;
  private LocalTime endTime;
  private long duration;
}
