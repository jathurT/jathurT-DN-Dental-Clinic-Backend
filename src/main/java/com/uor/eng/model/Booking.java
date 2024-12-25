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

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Booking {

  @Id
  @Column(nullable = false,unique = true,length = 255)
  private String referenceId;

  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank(message = "NIC is required")
  @Pattern(regexp = "(\\d{12}|\\d{9}[A-Z]\\d{2})", message = "NIC should be 12 digits")
  private String nic;

  @NotBlank(message = "Contact number is required")
  @Pattern(regexp = "\\d{10}", message = "Contact number should be 10 digits")
  private String contactNumber;

  @NotBlank(message = "Email is required")
  @Email(message = "Please provide a valid email address")
  private String email;

  @NotBlank(message = "Address is required")
  private String address;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "schedule_id", nullable = false)
  @JsonBackReference
  private Schedule schedule;

  @PrePersist
  public void generateReferenceId() {
    this.referenceId = generateRandomAlphanumeric(8); // Change 8 to 7 if needed
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
