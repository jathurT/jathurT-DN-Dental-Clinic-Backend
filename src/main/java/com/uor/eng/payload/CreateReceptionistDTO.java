package com.uor.eng.payload;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReceptionistDTO {
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

  @NotBlank(message = "Password is mandatory")
  @Size(min = 6, max = 120, message = "Password must be between 6 and 120 characters")
  private String password;

  @NotBlank(message = "First name is mandatory")
  @Size(min = 2, max = 30, message = "First name must be between 2 and 30 characters")
  private String firstName;

  @NotBlank(message = "NIC is mandatory")
  @Size(min = 10, max = 12, message = "NIC must be between 10 and 12 characters")
  private String nic;

  @NotBlank(message = "Phone number is mandatory")
  @Size(min = 10, max = 10, message = "Phone number must be 10 characters")
  private String phoneNumber;

  @NotBlank(message = "Shift timing is mandatory")
  @Size(min = 2, max = 30, message = "Shift timing must be between 2 and 30 characters")
  private String shiftTiming;

}
