package com.uor.eng.payload.patient.logs;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PatientLogResponse {
  private Long id;
  private String actionType;
  private String description;
  private LocalDateTime timestamp;
  private String dentistName;
  private List<PatientLogPhotoResponse> photos;
}
