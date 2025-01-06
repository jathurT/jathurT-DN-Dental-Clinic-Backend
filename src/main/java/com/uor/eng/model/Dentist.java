package com.uor.eng.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "dentists",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "nic", name = "UK_dentist_nic"),
        @UniqueConstraint(columnNames = "license_number", name = "UK_dentist_license_number")
    })
@Data
@AllArgsConstructor
@NoArgsConstructor
@DiscriminatorValue("DENTIST")
public class Dentist extends User {

  @Column(name = "first_name", nullable = false, length = 50)
  @NotBlank(message = "First name is mandatory")
  @Size(min = 3, max = 50, message = "First name must be between 3 and 50 characters")
  private String firstName;

  @Column(name = "nic", nullable = false, length = 12)
  @NotBlank(message = "NIC is mandatory")
  @Size(min = 10, max = 12, message = "NIC must be between 10 and 12 characters")
  private String nic;

  @Column(name = "phone_number", nullable = false, length = 10)
  @NotBlank(message = "Phone number is mandatory")
  @Size(min = 10, max = 10, message = "Phone number must be 10 characters")
  private String phoneNumber;

  @Column(name = "gender", nullable = false, length = 6)
  @NotBlank(message = "Gender is mandatory")
  @Size(min = 4, max = 6, message = "Gender must be between 4 and 6 characters")
  private String gender;

  @Column(name = "specialization", nullable = false, length = 50)
  @NotBlank(message = "Specialization is mandatory")
  @Size(min = 3, max = 50, message = "Specialization must be between 3 and 50 characters")
  private String specialization;

  @Column(name = "license_number", nullable = false, length = 10)
  @NotBlank(message = "License number is mandatory")
  @Size(min = 6, max = 10, message = "License number must be between 6 and 10 characters")
  private String licenseNumber;

  @OneToMany(mappedBy = "dentist", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JsonBackReference
  private List<Schedule> schedules;
}
