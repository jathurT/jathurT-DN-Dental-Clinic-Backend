package com.uor.eng.payload.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uor.eng.model.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleResponseDTO {
  private Long id;
  private LocalDate date;
  private String dayOfWeek;
  private String status;
  private Integer numberOfBookings;
  private List<Booking> bookings;
  private LocalTime startTime;
  private LocalTime endTime;
  private Long duration;
  private Long dentistId;
  private Integer capacity;
  private Integer availableSlots;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

}
