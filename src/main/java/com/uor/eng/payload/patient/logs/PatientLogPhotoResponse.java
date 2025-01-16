package com.uor.eng.payload.patient.logs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientLogPhotoResponse {
  private Long id;
  private String url;
  private String description;
  private LocalDateTime timestamp;
}