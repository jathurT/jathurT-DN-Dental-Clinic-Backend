package com.uor.eng.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingDTO {
  private String referenceId;

  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank(message = "NIC is required")
  @Pattern(regexp = "^(\\d{9}[VXvx]|\\d{12})$", message = "NIC should be in the correct format: 9 digits followed by V or X, or 12 digits")
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
