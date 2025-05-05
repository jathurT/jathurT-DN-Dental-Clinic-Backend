package com.uor.eng.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginRequest {
  @NotBlank
  private String userNameOrEmail;

  @NotBlank
  private String password;

}
