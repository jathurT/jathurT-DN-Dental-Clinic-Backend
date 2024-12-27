package com.uor.eng.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {

  @Id
  @Column(nullable = false, unique = true, length = 255)
  private String referenceId;

  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank(message = "NIC is required")
  @Pattern(regexp = "^(\\d{9}[VXvx]|\\d{12})$", message = "NIC should be in the correct format: 9 digits followed by V or X, or 12 digits")
  private String nic;

  @NotBlank(message = "Contact number is required")
  @Pattern(regexp = "\\d{10}", message = "Contact number should be 10 digits")
  private String contactNumber;

  @NotBlank(message = "Email is required")
  @Email(message = "Please provide a valid email address")
  private String email;

  @NotBlank(message = "Address is required")
  private String address;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private BookingStatus status;

  @Column(nullable = false)
  private LocalDate date;

  @Column(nullable = false)
  private String dayOfWeek;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "schedule_id", nullable = false)
  @JsonBackReference
  private Schedule schedule;

  @PrePersist
  public void prePersist() {
    if (this.referenceId == null || this.referenceId.isEmpty()) {
      this.referenceId = generateRandomAlphanumeric(8);
    }
    if (this.status == null) {
      this.status = BookingStatus.PENDING;
    }
    this.date = LocalDate.now();
    this.dayOfWeek = this.date.getDayOfWeek().toString();
    this.createdAt = LocalDateTime.now();
  }


  private String generateRandomAlphanumeric(int length) {
    String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    StringBuilder referenceId = new StringBuilder();

    for (int i = 0; i < length; i++) {
      int index = (int) (Math.random() * characters.length());
      referenceId.append(characters.charAt(index));
    }
    return referenceId.toString();
  }
}
