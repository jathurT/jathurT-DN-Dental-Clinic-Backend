package com.uor.eng.payload.dentist;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDentistRequest {
  @NotBlank(message = "Username is mandatory")
  @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
  private String userName;

  @NotBlank(message = "Email is mandatory")
  @Email(message = "Email should be valid")
  @Size(max = 50, message = "Email must be at most 50 characters")
  private String email;

  @NotBlank(message ="Gender is mandatory")
  @Size(min = 4, max = 6, message ="Gender must be between 4 and 6 characters")
  private String gender;

  @NotBlank(message = "First name is mandatory")
  @Size(min = 2, max = 30, message = "First name must be between 2 and 30 characters")
  private String firstName;

  @NotBlank(message = "Specialization is mandatory")
  @Size(min = 3, max = 50, message = "Specialization must be between 3 and 50 characters")
  private String specialization;

  @NotBlank(message = "License number is mandatory")
  @Size(min = 6, max = 10, message = "License number must be between 6 and 10 characters")
  private String licenseNumber;

  @NotBlank(message = "NIC is mandatory")
  @Size(min = 10, max = 12, message = "NIC must be between 10 and 12 characters")
  @Pattern(regexp = "^(\\d{9}[VXvx]|\\d{12})$", message = "NIC should be in the correct format: 9 digits followed by V or X, or 12 digits")
  private String nic;

  @NotBlank(message = "Phone number is mandatory")
  @Size(min = 10, max = 10, message = "Phone number must be 10 characters")
  private String phoneNumber;
}
