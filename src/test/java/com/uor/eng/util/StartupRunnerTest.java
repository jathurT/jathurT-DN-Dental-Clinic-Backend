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
public class StartupRunnerTest {

  @Mock
  private IScheduleService scheduleService;

  @InjectMocks
  private StartupRunner startupRunner;

  @BeforeEach
  void setUp() {
    // No additional setup needed with MockitoExtension
  }

  @Test
  void run_shouldCallServices() throws Exception {
    // Act
    startupRunner.run();

    // Assert
    verify(scheduleService).initialUpdaterScheduleOnStartup();
    verify(scheduleService).updateExpiredSchedules();
  }

  @Test
  void run_shouldHandleExceptions() throws Exception {
    // Arrange
    doThrow(new RuntimeException("Service failure")).when(scheduleService).initialUpdaterScheduleOnStartup();

    // Act - should not throw exception
    startupRunner.run();

    // Assert
    verify(scheduleService).initialUpdaterScheduleOnStartup();
    verify(scheduleService, never()).updateExpiredSchedules(); // This should not be called if the first method throws
  }

  @Test
  void run_shouldHandleExceptionsFromSecondService() throws Exception {
    // Arrange
    doNothing().when(scheduleService).initialUpdaterScheduleOnStartup();
    doThrow(new RuntimeException("Service failure")).when(scheduleService).updateExpiredSchedules();

    // Act
    startupRunner.run();

    // Assert
    verify(scheduleService).initialUpdaterScheduleOnStartup();
    verify(scheduleService).updateExpiredSchedules();
    // In a real test, we would verify logging behavior here
  }

  @Test
  void run_shouldCallServicesWithCorrectParameters() throws Exception {
    // Act
    String[] args = new String[]{"arg1", "arg2"};
    startupRunner.run(args);

    // Assert
    verify(scheduleService).initialUpdaterScheduleOnStartup();
    verify(scheduleService).updateExpiredSchedules();
  }
}