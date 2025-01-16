package com.uor.eng.payload.patient;

import com.uor.eng.payload.patient.logs.PatientLogResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {
  private Long id;
  private String name;
  private String email;
  private String nic;
  private List<String> contactNumbers;
  private List<PatientLogResponse> logs;
}