package com.uor.eng.payload.patient.logs;

import lombok.Data;

@Data
public class PresignedUrlRequest {
  private String fileName;
  private String contentType;
}
