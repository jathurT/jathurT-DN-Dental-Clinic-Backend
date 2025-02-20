package com.uor.eng.payload.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpcomingScheduleResponse {
  private String date;
  private String startTime;
  private String endTime;
  private Integer appointmentCount;
}
