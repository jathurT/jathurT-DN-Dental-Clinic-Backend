package com.uor.eng.payload;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PatientLogResponse {
  private Long id;
  private String actionType;
  private String description;
  private LocalDateTime timestamp;
  private List<PatientLogPhotoResponse> photos;
}
