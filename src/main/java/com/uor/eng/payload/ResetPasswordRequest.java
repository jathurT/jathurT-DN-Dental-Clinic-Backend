package com.uor.eng.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest  {
  @NotBlank
  private String token;

  @NotBlank
  @Size(min = 6, max = 40)
  @Pattern(
      regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,40}$",
      message = "Password must be 6-40 characters long, contain at least one digit, one lowercase letter, one uppercase letter, and one special character"
  )
  private String newPassword;
}
