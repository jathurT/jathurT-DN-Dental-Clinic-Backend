package com.uor.eng.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

  @Bean
  public Counter createBookingCounter(MeterRegistry registry) {
    return Counter.builder("app.booking.create.count")
        .description("Number of bookings created")
        .register(registry);
  }

  @Bean
  public Counter createBookingErrorCounter(MeterRegistry registry) {
    return Counter.builder("app.booking.create.error.count")
        .description("Number of booking creation errors")
        .register(registry);
  }

  @Bean
  public Timer createBookingTimer(MeterRegistry registry) {
    return Timer.builder("app.booking.create.time")
        .description("Time taken to create bookings")
        .register(registry);
  }
}