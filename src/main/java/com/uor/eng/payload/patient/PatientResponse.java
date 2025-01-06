package com.uor.eng.payload.patient;

import lombok.Data;

import java.util.List;

@Data
public class PatientResponse {
  private Long id;
  private String name;
  private String email;
  private String nic;
  private List<String> contactNumbers;
  private List<PatientLogResponse> logs;
}