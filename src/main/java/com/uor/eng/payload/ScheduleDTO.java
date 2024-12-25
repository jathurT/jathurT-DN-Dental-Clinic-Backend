package com.uor.eng.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.uor.eng.model.Dentist;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

  @NotBlank(message = "Date is required")
  @Size(min = 10, max = 10, message = "Date must be in the format yyyy-MM-dd")
  private LocalDate date;

  private String dayOfWeek;

  @NotBlank(message = "Status is required")
  private String status;

  private Integer numberOfBookings;
  private List<BookingResponseDTO> bookings;

  @NotBlank(message = "Start time is required")
  private LocalTime startTime;

  @NotBlank(message = "End time is required")
  private LocalTime endTime;

  private Long duration;

  @NotBlank(message = "Dentist is required")
  private Long dentistId;
}
