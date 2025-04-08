package com.uor.eng.repository;

import com.uor.eng.model.Schedule;
import com.uor.eng.model.ScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
  @Query(nativeQuery = true, value = "SELECT * FROM schedules WHERE date <= :today AND end_time < :nowTime AND status NOT IN (:excludedStatuses)")
  List<Schedule> findSchedulesToFinish(@Param("today") LocalDate today,
                                       @Param("nowTime") LocalTime nowTime,
                                       @Param("excludedStatuses") List<ScheduleStatus> excludedStatuses);

  List<Schedule> findTop7ByDateGreaterThanAndStatusOrderByDateAsc(LocalDate today, ScheduleStatus scheduleStatus);


  Page<Schedule> findByStatus(ScheduleStatus scheduleStatus, Pageable topTen);

  List<Schedule> findByDateBetweenAndStatusNot(LocalDate startDate, LocalDate today, ScheduleStatus scheduleStatus);
}

