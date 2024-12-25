package com.uor.eng.payload;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponseDTO {
  private Long id;
  private LocalDate date;
  private String dayOfWeek;
  private String status;
  private Integer numberOfBookings;
  private List<BookingResponseDTO> bookings;
  private LocalTime startTime;
  private LocalTime endTime;
  private Long duration;
  private Long dentistId;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdAt;

}
