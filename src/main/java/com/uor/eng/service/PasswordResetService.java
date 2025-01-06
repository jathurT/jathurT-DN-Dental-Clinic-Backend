package com.uor.eng.service;

import com.uor.eng.payload.auth.ForgotPasswordRequest;
import com.uor.eng.payload.auth.ResetPasswordRequest;
import jakarta.validation.Valid;

public interface PasswordResetService {
  void initiatePasswordReset(@Valid ForgotPasswordRequest forgotPasswordRequest);

  void resetPassword(@Valid ResetPasswordRequest request);
}
