package com.uor.eng.payload.patient.logs;

import lombok.Data;

import java.util.List;

@Data
public class AssociatePhotosRequest {
  private List<String> s3Keys;
  private List<String> descriptions;
}