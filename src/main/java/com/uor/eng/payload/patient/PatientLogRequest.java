package com.uor.eng.payload.patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PatientLogRequest {

  @NotBlank(message = "Action type is required")
  @Size(max = 50)
  private String actionType;

  @Size(max = 500)
  private String description;

  private List<MultipartFile> photos;
}