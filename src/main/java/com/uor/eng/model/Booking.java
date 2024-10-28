package com.uor.eng.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long referenceId;

  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank(message = "NIC is required")
  @Max(value = 12, message = "NIC should be 12 digits")
  private String nic;

  @NotBlank(message = "Contact number is required")
  @Max(value = 10, message = "Contact number should be 10 digits")
  private String contactNumber;

  @NotBlank(message = "Email is required")
  @Email(message = "Please provide a valid email address")
  private String email;

  @NotBlank(message = "Address is required")
  private String address;

  @NotNull(message = "Date and time are required")
  private LocalDateTime dateTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "schedule_id", nullable = false)
  private Schedule schedule;
}
