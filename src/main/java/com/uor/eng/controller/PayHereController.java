package com.uor.eng.controller;

import com.uor.eng.payload.payment.PaymentDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

@RestController
@RequestMapping("/api/payment")
public class PayHereController {

  private final WebClient webClient;
  private final String merchantSecret = "1229605";
  private final String merchantId = "1224501";

  public PayHereController(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.baseUrl("https://sandbox.payhere.lk").build();
  }

  @PutMapping("/initiate")
  public ResponseEntity<String> initiatePayment(@RequestParam("amount") double amount) {
    String orderID = Long.toString(System.currentTimeMillis());
    DecimalFormat df = new DecimalFormat("0.00");
    String amountFormatted = df.format(amount);
    String currency = "LKR";
    String hash = getMd5(merchantId + orderID + amountFormatted + currency + getMd5(merchantSecret));

    // Encode parameters
    String requestBody = String.format(
        "merchant_id=%s&return_url=%s&cancel_url=%s&notify_url=%s&order_id=%s&items=%s&amount=%s&currency=%s&first_name=%s&last_name=%s&email=%s&phone=%s&address=%s&city=%s&country=%s&hash=%s",
        URLEncoder.encode(merchantId, StandardCharsets.UTF_8),
        URLEncoder.encode("http://yourdomain.com/success", StandardCharsets.UTF_8),
        URLEncoder.encode("http://yourdomain.com/cancel", StandardCharsets.UTF_8),
        URLEncoder.encode("http://yourdomain.com/notify", StandardCharsets.UTF_8),
        URLEncoder.encode(orderID, StandardCharsets.UTF_8),
        URLEncoder.encode("Order Payment", StandardCharsets.UTF_8),
        URLEncoder.encode(amountFormatted, StandardCharsets.UTF_8),
        URLEncoder.encode(currency, StandardCharsets.UTF_8),
        URLEncoder.encode("John", StandardCharsets.UTF_8),
        URLEncoder.encode("Doe", StandardCharsets.UTF_8),
        URLEncoder.encode("johndoe@example.com", StandardCharsets.UTF_8),
        URLEncoder.encode("0771234567", StandardCharsets.UTF_8),
        URLEncoder.encode("No. 1, Galle Road", StandardCharsets.UTF_8),
        URLEncoder.encode("Colombo", StandardCharsets.UTF_8),
        URLEncoder.encode("Sri Lanka", StandardCharsets.UTF_8),
        URLEncoder.encode(hash, StandardCharsets.UTF_8)
    );

    // Call PayHere API
    String response = webClient.post()
        .uri("/pay/checkout")
        .header("Content-Type", "application/x-www-form-urlencoded")
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class)
        .block();

    return ResponseEntity.ok(response);
  }

  public static String getMd5(String input) {
    try {
      java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
      byte[] messageDigest = md.digest(input.getBytes());
      java.math.BigInteger no = new java.math.BigInteger(1, messageDigest);
      String hashtext = no.toString(16);
      while (hashtext.length() < 32) {
        hashtext = "0" + hashtext;
      }
      return hashtext.toUpperCase();
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
