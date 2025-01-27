package com.uor.eng.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "patient_log_photos")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PatientLogPhoto {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "photo_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "patient_log_id", nullable = false)
  private PatientLog patientLog;

  @Column(name = "s3_key", nullable = false)
  @NotBlank(message = "S3 Key is required")
  private String s3Key;

  @Column(name = "description")
  private String description;

  @Transient
  private String url;

  @Column(name = "timestamp", nullable = false)
  private LocalDateTime timestamp;

}
