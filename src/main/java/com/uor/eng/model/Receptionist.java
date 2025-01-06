package com.uor.eng.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "receptionist",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "nic", name = "UK_receptionist_nic"),
    })
@AllArgsConstructor
@NoArgsConstructor
@Data
@DiscriminatorValue("RECEPTIONIST")
public class Receptionist extends User {

  @Column(name = "first_name", nullable = false, length = 50)
  @NotBlank(message = "First name is mandatory")
  @Size(min = 3, max = 50)
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

  @Column(name = "shift_timing", nullable = false, length = 50)
  @NotBlank(message = "Shift timing is mandatory")
  @Size(min = 3, max = 50)
  private String shiftTiming;
}
