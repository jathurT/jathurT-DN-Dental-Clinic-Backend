package com.uor.eng.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
  private Long referenceId;
  private String name;
  private String nic;
  private String contactNumber;
  private String email;
  private String address;
  private Long scheduleId;
}
