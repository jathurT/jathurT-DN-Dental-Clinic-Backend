package com.uor.eng.util;

import com.uor.eng.service.IScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class ScheduleTaskScheduler {

  private final IScheduleService scheduleService;

  public ScheduleTaskScheduler(IScheduleService scheduleService) {
    this.scheduleService = scheduleService;
  }

  /**
   * Runs every 15 minutes to check and update schedule statuses
   */
  @Scheduled(cron = "0 */15 * * * *") // Every 15 minutes
  public void updateScheduleStatuses() {
    log.info("Starting scheduled task to update schedule statuses at {}", LocalDateTime.now());
    try {
      scheduleService.updateExpiredSchedules();
      log.info("Successfully completed schedule status update");
    } catch (Exception e) {
      log.error("Error updating schedule statuses", e);
    }
  }

  /**
   * Runs every day at midnight to process finished schedules
   */
  @Scheduled(cron = "0 0 0 * * *") // Daily at midnight
  public void processDailySchedules() {
    log.info("Starting daily schedule processing at {}", LocalDateTime.now());
    try {
      scheduleService.processDailySchedules();
      log.info("Successfully completed daily schedule processing");
    } catch (Exception e) {
      log.error("Error processing daily schedules", e);
    }
  }

  /**
   * Runs every hour to send reminder emails
   */
  @Scheduled(cron = "0 0 * * * *") // Every hour
  public void sendScheduleReminders() {
    log.info("Starting schedule reminder task at {}", LocalDateTime.now());
    try {
      scheduleService.sendAppointmentReminders();
      log.info("Successfully sent all reminders");
    } catch (Exception e) {
      log.error("Error sending reminders", e);
    }
  }
}