package com.uor.eng.payload.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {
  private String orderId;
  private String hash;
  private String amount;

}
