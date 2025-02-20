package com.uor.eng.payload.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyBookingStatsResponse {
  private String month;
  private int totalBookings;
  private int finishedBookings;
  private int cancelledBookings;
  private int pendingBookings;
}
