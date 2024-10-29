package com.uor.eng.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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
  private String status;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer numberOfBookings;
  @JsonIgnore
  private List<BookingDTO> bookings;
}
