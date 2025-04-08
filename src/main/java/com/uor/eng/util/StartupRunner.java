package com.uor.eng.util;

import com.uor.eng.service.IScheduleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

  private final IScheduleService scheduleService;

  public StartupRunner(IScheduleService scheduleService) {
    this.scheduleService = scheduleService;
  }

  @Override
  public void run(String... args) {
    scheduleService.initialUpdaterScheduleOnStartup();
  }
}
