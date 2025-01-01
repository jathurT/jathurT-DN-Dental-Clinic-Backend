package com.uor.eng.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest  {
  @NotBlank
  private String token;

  @NotBlank
  private String newPassword;
}
