package com.uor.eng.util;

import com.uor.eng.service.IScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StartupRunner implements CommandLineRunner {

  private final IScheduleService scheduleService;

  public StartupRunner(IScheduleService scheduleService) {
    this.scheduleService = scheduleService;
  }

  @Override
  public void run(String... args) throws Exception {
    log.info("Running startup tasks...");
    try {
      scheduleService.initialUpdaterScheduleOnStartup();
      scheduleService.updateExpiredSchedules();
      log.info("Startup tasks completed successfully");
    } catch (Exception e) {
      log.error("Error running startup tasks", e);
    }
  }
}