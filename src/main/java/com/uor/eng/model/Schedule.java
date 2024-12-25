package com.uor.eng.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
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
  private LocalDate date;

  @Column(nullable = false)
  private String dayOfWeek;

  @Column(nullable = false)
  private String status;

  @Column(nullable = false)
  private LocalTime startTime;

  @Column(nullable = false)
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
