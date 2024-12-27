package com.uor.eng.repository;

import com.uor.eng.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
  List<Schedule> findTop7ByDateGreaterThanEqualOrderByDateAsc(LocalDate today);
}
