package com.uor.eng.payload.patient.logs;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientLogUpdateRequest {

  @Size(max = 50, message = "Action type must be at most 50 characters")
  private String actionType;

  @Size(max = 500, message = "Description must be at most 500 characters")
  private String description;

  private List<String> newPhotoKeys;

  private List<Long> photosToDelete;
}