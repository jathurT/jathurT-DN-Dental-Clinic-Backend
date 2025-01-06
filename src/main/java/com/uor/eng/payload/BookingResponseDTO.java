package com.uor.eng.payload;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uor.eng.model.BookingStatus;
import com.uor.eng.model.ScheduleStatus;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDTO {

  private String referenceId;
  private Integer appointmentNumber;
  private String name;
  private String nic;
  private String contactNumber;
  private String email;
  private String address;
  private Long scheduleId;
  private LocalDate scheduleDate;
  private String scheduleDayOfWeek;
  private ScheduleStatus scheduleStatus;
  private LocalTime scheduleStartTime;
  private String doctorName;
  private BookingStatus status;
  private LocalDate date;
  private String dayOfWeek;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
  private LocalDateTime createdAt;
}
