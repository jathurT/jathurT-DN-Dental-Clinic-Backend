package com.uor.eng.payload.dentist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DentistResponseDTO {

  private Long id;
  private String userName;
  private String email;
  private String gender;
  private String firstName;
  private String specialization;
  private String licenseNumber;
  private String phoneNumber;
  private String nic;
  private Set<String> roles;
}
