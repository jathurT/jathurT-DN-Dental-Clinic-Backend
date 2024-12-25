package com.uor.eng.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "schedules")
@Data
@NoArgsConstructor
public class Schedule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  @NotNull(message = "Date is required")
  private LocalDate date;

  @Column(nullable = false)
  @NotBlank(message = "Day is required")
  private String dayOfWeek;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private ScheduleStatus status;

  @Column(nullable = false)
  @NotNull(message = "Start time is required")
  private LocalTime startTime;

  @Column(nullable = false)
  @NotNull(message = "End time is required")
  private LocalTime endTime;

  @Column(nullable = false)
  private Long duration;

  @PrePersist
  @PreUpdate
  public void calculateDuration() {
    if (startTime != null && endTime != null) {
      this.duration = Duration.between(startTime, endTime).toMinutes();
    }
  }

  @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JsonManagedReference
  private List<Booking> bookings;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "dentist_id",nullable = false)
  @JsonManagedReference
  private Dentist dentist;

}
