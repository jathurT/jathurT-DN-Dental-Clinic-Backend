package com.uor.eng.util;

import com.uor.eng.service.IScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

  @Autowired
  private IScheduleService scheduleService;

  @Override
  public void run(String... args) throws Exception {
    scheduleService.initialUpdaterScheduleOnStartup();
  }
}
