package com.uor.eng.util;

import com.uor.eng.service.IScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleTaskSchedulerTest {

  @Mock
  private IScheduleService scheduleService;

  @InjectMocks
  private ScheduleTaskScheduler scheduleTaskScheduler;

  @BeforeEach
  void setUp() {
    // No additional setup needed with MockitoExtension
  }

  @Test
  void updateScheduleStatuses_shouldCallService() {
    // Act
    scheduleTaskScheduler.updateScheduleStatuses();

    // Assert
    verify(scheduleService).updateExpiredSchedules();
  }

  @Test
  void updateScheduleStatuses_shouldHandleExceptions() {
    // Arrange
    doThrow(new RuntimeException("Service failure")).when(scheduleService).updateExpiredSchedules();

    // Act - should not throw exception
    scheduleTaskScheduler.updateScheduleStatuses();

    // Assert
    verify(scheduleService).updateExpiredSchedules();
    // We'd verify logging in a real test, but that's harder to test
  }

  @Test
  void processDailySchedules_shouldCallService() {
    // Act
    scheduleTaskScheduler.processDailySchedules();

    // Assert
    verify(scheduleService).processDailySchedules();
  }

  @Test
  void processDailySchedules_shouldHandleExceptions() {
    // Arrange
    doThrow(new RuntimeException("Service failure")).when(scheduleService).processDailySchedules();

    // Act - should not throw exception
    scheduleTaskScheduler.processDailySchedules();

    // Assert
    verify(scheduleService).processDailySchedules();
  }

  @Test
  void sendScheduleReminders_shouldCallService() {
    // Act
    scheduleTaskScheduler.sendScheduleReminders();

    // Assert
    verify(scheduleService).sendAppointmentReminders();
  }

  @Test
  void sendScheduleReminders_shouldHandleExceptions() {
    // Arrange
    doThrow(new RuntimeException("Service failure")).when(scheduleService).sendAppointmentReminders();

    // Act - should not throw exception
    scheduleTaskScheduler.sendScheduleReminders();

    // Assert
    verify(scheduleService).sendAppointmentReminders();
  }
}