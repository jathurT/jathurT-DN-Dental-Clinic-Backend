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
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long referenceId;

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
}
