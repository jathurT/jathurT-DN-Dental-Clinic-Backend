package com.uor.eng.service;

import com.uor.eng.payload.ForgotPasswordRequest;
import com.uor.eng.payload.ResetPasswordRequest;
import jakarta.validation.Valid;

public interface PasswordResetService {
  void initiatePasswordReset(@Valid ForgotPasswordRequest forgotPasswordRequest);

  void resetPassword(@Valid ResetPasswordRequest request);
}
