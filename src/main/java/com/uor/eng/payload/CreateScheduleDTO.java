package com.uor.eng.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateScheduleDTO {
  @NotBlank(message = "Date is required")
  @Size(min = 10, max = 10, message = "Date must be in the format yyyy-MM-dd")
  private LocalDate date;

  @NotBlank(message = "Status is required")
  private String status;

  @NotBlank(message = "Start time is required")
  private LocalTime startTime;

  @NotBlank(message = "End time is required")
  private LocalTime endTime;

  @NotBlank(message = "Dentist is required")
  private Long dentistId;

  @NotBlank(message = "Capacity is required")
  private Integer capacity;
}
