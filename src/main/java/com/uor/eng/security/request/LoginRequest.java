package com.uor.eng.security.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequest {
  @NotBlank
  @Email
  private String email;

  @NotBlank
  private String password;

}
