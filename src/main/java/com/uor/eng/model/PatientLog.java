package com.uor.eng.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "patient_logs")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PatientLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "log_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "patient_id", nullable = false)
  private Patient patient;

  @Column(name = "action_type")
  @NotBlank(message = "Action type is required")
  @Size(max = 50)
  private String actionType;

  @Column(name = "description")
  @Size(max = 500)
  private String description;

  @Column(name = "timestamp", nullable = false)
  private LocalDateTime timestamp;

  @OneToMany(mappedBy = "patientLog", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PatientLogPhoto> patientLogPhotos;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dentist_id", nullable = false)
  private Dentist dentist;

}
