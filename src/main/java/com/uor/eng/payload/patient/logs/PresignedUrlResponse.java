package com.uor.eng.payload.patient.logs;

import lombok.Data;

@Data
public class PresignedUrlResponse {
  private String url;
  private String key;
}