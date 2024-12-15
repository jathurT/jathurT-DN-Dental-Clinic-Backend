package com.uor.eng.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "role_id")
  private Long roleId;

  @Column(name = "role_name", length = 20)
  @Enumerated(EnumType.STRING)
  @ToString.Exclude
  private AppRole roleName;

  public Role(AppRole roleName) {
    this.roleName = roleName;
  }
}
