package com.uor.eng.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Entity
@Table(name = "patients", uniqueConstraints = {
    @UniqueConstraint(columnNames = "patient_nic")
})
@Data
@RequiredArgsConstructor
public class Patient {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "patient_id")
  private Long id;

  @Column(name = "patient_name")
  @NotBlank(message = "Name is required")
  @Size(min = 3, max = 50)
  private String name;

  @NotBlank(message = "Email is required")
  @Email
  @Size(max = 50)
  @Column(name = "patient_email")
  private String email;

  @NotBlank(message = "NIC is required")
  @Size(min = 10, max = 12)
  @Column(name = "patient_nic")
  private String nic;

  @NotEmpty(message = "Contact number is required")
  @Column(name = "patient_contact_number")
  @ElementCollection
  @Size(min = 1, max = 3)
  private List<
      @NotBlank(message = "Contact number cannot be blank")
      @Size(min = 10, max = 10)
          String> contactNumbers;

  @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PatientLog> patientLogs;

}
