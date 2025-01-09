package com.uor.eng.payload.patient.logs;


import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PatientLogRequestNoPhotos {

  @NotBlank(message = "Action type is required")
  @Size(max = 50)
  private String actionType;

  @Size(max = 500)
  private String description;

  @Column(name = "dentist_id")
  @NotBlank(message = "Dentist ID is required")
  private Long dentistId;
}