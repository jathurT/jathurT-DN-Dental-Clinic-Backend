package com.uor.eng.payload.patient.logs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssociatePhotosRequest {
  private List<String> s3Keys;
  private List<String> descriptions;
}