package com.uor.eng.payload.other;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactDTO {
  private Long id;
  private String name;
  private String email;
  private String contactNumber;
  private String subject;
  private String message;
  private Boolean replySent = false;
}
