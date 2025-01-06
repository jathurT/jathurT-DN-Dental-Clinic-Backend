package com.uor.eng.payload.patient;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PatientLogUpdateRequest {

  @Size(max = 50, message = "Action type must be at most 50 characters")
  private String actionType;

  @Size(max = 500, message = "Description must be at most 500 characters")
  private String description;

  private List<MultipartFile> newPhotos;

  private List<Long> photosToDelete;
}