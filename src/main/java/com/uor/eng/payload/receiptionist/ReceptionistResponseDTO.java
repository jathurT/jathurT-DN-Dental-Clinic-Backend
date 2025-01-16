package com.uor.eng.payload.receiptionist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReceptionistResponseDTO {
  private Long id;
  private String userName;
  private String email;
  private String gender;
  private String firstName;
  private String phoneNumber;
  private String nic;
  private String shiftTiming;
  private Set<String> roles;
}
