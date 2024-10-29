package com.uor.eng.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDTO {
  private Long id;
  private LocalDate date;
  private String dayOfWeek;

  @JsonIgnore
  private List<BookingDTO> bookings;
  private String status;
}
