package com.uor.eng.payload;

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