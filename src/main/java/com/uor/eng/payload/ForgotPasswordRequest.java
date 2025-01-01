package com.uor.eng.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest  {
  @NotBlank
  private String email;
}
