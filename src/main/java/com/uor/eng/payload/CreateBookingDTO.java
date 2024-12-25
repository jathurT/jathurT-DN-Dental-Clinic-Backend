package com.uor.eng.payload;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingDTO {
  private String referenceId;

  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank(message = "NIC is required")
  private String nic;

  @NotBlank(message = "Contact number is required")
  private String contactNumber;

  @NotBlank(message = "Email is required")
  private String email;

  @NotBlank(message = "Address is required")
  private String address;

  @NotBlank(message = "Schedule Id is required")
  private Long scheduleId;

}
