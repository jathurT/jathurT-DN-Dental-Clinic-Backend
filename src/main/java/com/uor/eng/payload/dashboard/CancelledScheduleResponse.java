package com.uor.eng.payload.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CancelledScheduleResponse {
  private String date;
  private String startTime;
  private String endTime;
}
