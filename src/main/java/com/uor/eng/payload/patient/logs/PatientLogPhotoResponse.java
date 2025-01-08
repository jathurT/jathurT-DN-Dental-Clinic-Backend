package com.uor.eng.payload.patient.logs;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PatientLogPhotoResponse {
  private Long id;
  private String url;
  private String description;
  private LocalDateTime timestamp;
}