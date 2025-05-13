package com.uor.eng.repository;

import com.uor.eng.model.Dentist;
import com.uor.eng.model.Schedule;
import com.uor.eng.model.ScheduleStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ScheduleRepositoryTest {

  @Autowired
  private ScheduleRepository scheduleRepository;

  @Autowired
  private DentistRepository dentistRepository;

  @BeforeEach
  void setUp() {
    Dentist testDentist = Dentist.dentistBuilder()
        .email("ktmjathur2001@gmail.com")
        .userName("jathur")
        .firstName("John")
        .password("securePassword@123")
        .nic("200132504295")
        .phoneNumber("0779797254")
        .gender("Male")
        .specialization("Orthodontist")
        .licenseNumber("LIC12345")
        .schedules(Collections.emptyList())
        .build();
    dentistRepository.save(testDentist);

    Schedule testSchedule = Schedule.builder()
        .date(LocalDate.now())
        .dayOfWeek(LocalDate.now().getDayOfWeek().toString())
        .status(ScheduleStatus.AVAILABLE)
        .startTime(LocalTime.of(9, 0))
        .endTime(LocalTime.of(17, 0))
        .duration(480L)
        .capacity(10)
        .availableSlots(10)
        .dentist(testDentist)
        .build();
    scheduleRepository.save(testSchedule);
  }

//  @Test
//  @DisplayName("Test find schedules to finish")
//  @Order(1)
//  void testFindSchedulesToFinish_ShouldReturnSchedules() {
//    List<Schedule> schedules = scheduleRepository.findSchedulesToFinish(
//        LocalDate.now(),
//        LocalTime.now().plusHours(1),
//        List.of(ScheduleStatus.CANCELLED, ScheduleStatus.FINISHED, ScheduleStatus.UNAVAILABLE, ScheduleStatus.FULL)
//    );
//
//    assertThat(schedules).isNotEmpty();
//    assertThat(schedules.get(0).getStatus())
//        .isNotIn(ScheduleStatus.CANCELLED, ScheduleStatus.FINISHED, ScheduleStatus.UNAVAILABLE, ScheduleStatus.FULL);
//  }

  @Test
  @DisplayName("Test find schedules to finish with empty list")
  @Order(2)
  void testFindSchedulesToFinish_ShouldReturnEmptyList() {
    List<Schedule> schedules = scheduleRepository.findSchedulesToFinish(
        LocalDate.now().minusDays(1),
        LocalTime.now(),
        List.of(ScheduleStatus.ACTIVE)
    );

    assertThat(schedules).isEmpty();
  }

  @Test
  @DisplayName("Test find top 7 by date greater than and status order by date ascending")
  @Order(3)
  void testFindTop7ByDateGreaterThanAndStatusOrderByDateAsc_ShouldReturnSchedules() {
    List<Schedule> schedules = scheduleRepository.findTop7ByDateGreaterThanAndStatusOrderByDateAsc(
        LocalDate.now().minusDays(1), ScheduleStatus.AVAILABLE);

    assertThat(schedules).isNotEmpty();
    assertThat(schedules.get(0).getDate()).isAfterOrEqualTo(LocalDate.now().minusDays(1));
    assertThat(schedules.get(0).getStatus()).isEqualTo(ScheduleStatus.AVAILABLE);
  }

  @Test
  @DisplayName("Test find top 7 by date greater than and status order by date ascending with empty list")
  @Order(4)
  void testFindTop7ByDateGreaterThanAndStatusOrderByDateAsc_ShouldReturnEmptyList() {
    List<Schedule> schedules = scheduleRepository.findTop7ByDateGreaterThanAndStatusOrderByDateAsc(
        LocalDate.now().plusDays(1), ScheduleStatus.FINISHED);

    assertThat(schedules).isEmpty();
  }
}
