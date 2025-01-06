package com.uor.eng.payload.patient;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CreatePatientRequest {

  @NotBlank(message = "Name is required")
  @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
  private String name;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  @Size(max = 50, message = "Email must be at most 50 characters")
  private String email;

  @NotBlank(message = "NIC is required")
  @Size(min = 10, max = 12, message = "NIC must be between 10 and 12 characters")
  @Pattern(regexp = "^(\\d{9}[VXvx]|\\d{12})$", message = "NIC should be in the correct format: 9 digits followed by V or X, or 12 digits")
  private String nic;

  @Size(min = 1, max = 3, message = "You must provide between 1 and 3 contact numbers")
  @NotEmpty(message = "Contact number is required")
  private List<
      @NotBlank(message = "Contact number cannot be blank")
      @Size(min = 10, max = 10, message = "Contact number must be 10 characters")
          String> contactNumbers;
}