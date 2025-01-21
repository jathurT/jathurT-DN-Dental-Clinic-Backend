package com.uor.eng.repository;

import com.uor.eng.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class BookingRepositoryTest {

  @Autowired
  private BookingRepository bookingRepository;

  @Autowired
  private DentistRepository dentistRepository;

  @Autowired
  private ScheduleRepository scheduleRepository;

  @Test
  @DisplayName("Test find by reference id and contact number")
  @Order(1)
  public void testFindByReferenceIdAndContactNumber() {
    // Arrange
    Dentist dentist = createDentist();
    dentist = dentistRepository.save(dentist);

    Schedule schedule = createSchedule(dentist);
    scheduleRepository.save(schedule);

    Booking booking = Booking.builder()
        .referenceId("REF123")
        .appointmentNumber(1)
        .name("John Doe")
        .nic("123456789V")
        .contactNumber("0712345678")
        .email("john.doe@example.com")
        .address("123 Main Street")
        .status(BookingStatus.PENDING)
        .schedule(schedule)
        .build();
    bookingRepository.save(booking);

    // Act
    Optional<Booking> result = bookingRepository.findByReferenceIdAndContactNumber("REF123", "0712345678");

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getReferenceId()).isEqualTo("REF123");
    assertThat(result.get().getContactNumber()).isEqualTo("0712345678");
  }

  @Test
  @DisplayName("Test find by schedule id")
  @Order(2)
  public void testFindByScheduleId() {
    // Arrange
    Dentist dentist = createDentist();
    dentist = dentistRepository.save(dentist);
    Schedule schedule = createSchedule(dentist);
    scheduleRepository.save(schedule);

    Booking booking1 = Booking.builder()
        .appointmentNumber(1)
        .name("John Doe")
        .nic("123456789V")
        .contactNumber("0712345678")
        .email("john.doe@example.com")
        .address("123 Main Street")
        .status(BookingStatus.PENDING)
        .schedule(schedule)
        .build();

    Booking booking2 = Booking.builder()
        .appointmentNumber(2)
        .name("Jane Doe")
        .nic("987654321X")
        .contactNumber("0718765432")
        .email("jane.doe@example.com")
        .address("456 High Street")
        .status(BookingStatus.PENDING)
        .schedule(schedule)
        .build();

    bookingRepository.save(booking1);
    bookingRepository.save(booking2);

    // Act
    List<Booking> result = bookingRepository.findByScheduleId(schedule.getId());

    // Assert
    assertThat(result).hasSize(2);
    assertThat(result).extracting(Booking::getReferenceId)
        .containsExactlyInAnyOrder(booking1.getReferenceId(), booking2.getReferenceId());
  }

  private Schedule createSchedule(Dentist dentist) {
    return Schedule.builder()
        .date(LocalDate.now())
        .dayOfWeek(LocalDate.now().getDayOfWeek().toString())
        .status(ScheduleStatus.ACTIVE)
        .startTime(LocalTime.of(9, 0))
        .endTime(LocalTime.of(17, 0))
        .duration(480L)
        .capacity(10)
        .availableSlots(10)
        .dentist(dentist)
        .build();
  }

  private Dentist createDentist() {
    return Dentist.dentistBuilder()
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
  }
}
