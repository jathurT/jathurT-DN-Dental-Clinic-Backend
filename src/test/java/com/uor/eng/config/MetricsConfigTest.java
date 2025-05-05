package com.uor.eng.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MetricsConfigTest {

  @InjectMocks
  private MetricsConfig metricsConfig;

  @Mock
  private MeterRegistry meterRegistry;

  private SimpleMeterRegistry simpleMeterRegistry;

  @BeforeEach
  public void setup() {
    // Create a real SimpleMeterRegistry for tests that need actual metric behavior
    simpleMeterRegistry = new SimpleMeterRegistry();
  }

  @Test
  public void testCreateBookingCounter() {
    // Arrange
    Counter.Builder mockBuilder = mock(Counter.Builder.class);
    Counter mockCounter = mock(Counter.class);

    try (var counterStaticMock = mockStatic(Counter.class)) {
      counterStaticMock.when(() -> Counter.builder("app.booking.create.count"))
              .thenReturn(mockBuilder);
      when(mockBuilder.description("Number of bookings created")).thenReturn(mockBuilder);
      when(mockBuilder.register(meterRegistry)).thenReturn(mockCounter);

      // Act
      Counter result = metricsConfig.createBookingCounter(meterRegistry);

      // Assert
      assertSame(mockCounter, result, "Should return the mocked counter");
      verify(mockBuilder).description("Number of bookings created");
      verify(mockBuilder).register(meterRegistry);
    }
  }

  @Test
  public void testCreateBookingErrorCounter() {
    // Arrange
    Counter.Builder mockBuilder = mock(Counter.Builder.class);
    Counter mockCounter = mock(Counter.class);

    try (var counterStaticMock = mockStatic(Counter.class)) {
      counterStaticMock.when(() -> Counter.builder("app.booking.create.error.count"))
              .thenReturn(mockBuilder);
      when(mockBuilder.description("Number of booking creation errors")).thenReturn(mockBuilder);
      when(mockBuilder.register(meterRegistry)).thenReturn(mockCounter);

      // Act
      Counter result = metricsConfig.createBookingErrorCounter(meterRegistry);

      // Assert
      assertSame(mockCounter, result, "Should return the mocked counter");
      verify(mockBuilder).description("Number of booking creation errors");
      verify(mockBuilder).register(meterRegistry);
    }
  }

  @Test
  public void testCreateBookingTimer() {
    // Arrange
    Timer.Builder mockBuilder = mock(Timer.Builder.class);
    Timer mockTimer = mock(Timer.class);

    try (var timerStaticMock = mockStatic(Timer.class)) {
      timerStaticMock.when(() -> Timer.builder("app.booking.create.time"))
              .thenReturn(mockBuilder);
      when(mockBuilder.description("Time taken to create bookings")).thenReturn(mockBuilder);
      when(mockBuilder.register(meterRegistry)).thenReturn(mockTimer);

      // Act
      Timer result = metricsConfig.createBookingTimer(meterRegistry);

      // Assert
      assertSame(mockTimer, result, "Should return the mocked timer");
      verify(mockBuilder).description("Time taken to create bookings");
      verify(mockBuilder).register(meterRegistry);
    }
  }

  @Test
  public void testCreateBookingCounterWithRealRegistry() {
    // Act
    Counter counter = metricsConfig.createBookingCounter(simpleMeterRegistry);

    // Assert
    assertNotNull(counter, "Counter should not be null");
    assertEquals("app.booking.create.count", counter.getId().getName(),
            "Counter should have the correct name");
    assertEquals("Number of bookings created", counter.getId().getDescription(),
            "Counter should have the correct description");

    // Test functionality
    counter.increment();
    assertEquals(1.0, counter.count(), "Counter should increment properly");
  }

  @Test
  public void testCreateBookingErrorCounterWithRealRegistry() {
    // Act
    Counter counter = metricsConfig.createBookingErrorCounter(simpleMeterRegistry);

    // Assert
    assertNotNull(counter, "Counter should not be null");
    assertEquals("app.booking.create.error.count", counter.getId().getName(),
            "Counter should have the correct name");
    assertEquals("Number of booking creation errors", counter.getId().getDescription(),
            "Counter should have the correct description");

    // Test functionality
    counter.increment(3);
    assertEquals(3.0, counter.count(), "Counter should increment by the specified amount");
  }

  @Test
  public void testCreateBookingTimerWithRealRegistry() {
    // Act
    Timer timer = metricsConfig.createBookingTimer(simpleMeterRegistry);

    // Assert
    assertNotNull(timer, "Timer should not be null");
    assertEquals("app.booking.create.time", timer.getId().getName(),
            "Timer should have the correct name");
    assertEquals("Time taken to create bookings", timer.getId().getDescription(),
            "Timer should have the correct description");

    // Test functionality
    timer.record(() -> {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });
    assertTrue(timer.count() > 0, "Timer should record the execution");
    assertTrue(timer.totalTime(timer.baseTimeUnit()) > 0,
            "Timer should record a non-zero execution time");
  }

  @Test
  public void testCounterRegistryIntegration() {
    // Arrange
    MeterRegistry spyRegistry = spy(simpleMeterRegistry);

    // Act
    Counter counter = metricsConfig.createBookingCounter(spyRegistry);

    // Assert
    assertNotNull(counter, "Counter should not be null");
    assertNotNull(spyRegistry.find("app.booking.create.count").counter(),
            "Registry should contain the counter");
  }

  @Test
  public void testErrorCounterRegistryIntegration() {
    // Arrange
    MeterRegistry spyRegistry = spy(simpleMeterRegistry);

    // Act
    Counter counter = metricsConfig.createBookingErrorCounter(spyRegistry);

    // Assert
    assertNotNull(counter, "Counter should not be null");
    assertNotNull(spyRegistry.find("app.booking.create.error.count").counter(),
            "Registry should contain the error counter");
  }

  @Test
  public void testTimerRegistryIntegration() {
    // Arrange
    MeterRegistry spyRegistry = spy(simpleMeterRegistry);

    // Act
    Timer timer = metricsConfig.createBookingTimer(spyRegistry);

    // Assert
    assertNotNull(timer, "Timer should not be null");
    assertNotNull(spyRegistry.find("app.booking.create.time").timer(),
            "Registry should contain the timer");
  }

  @Test
  public void testCounterIncrement() {
    // Arrange
    Counter counter = metricsConfig.createBookingCounter(simpleMeterRegistry);

    // Act
    counter.increment();
    counter.increment(2.5);

    // Assert
    assertEquals(3.5, counter.count(), "Counter should accumulate the correct count");
  }

  @Test
  public void testTimerRecording() {
    // Arrange
    Timer timer = metricsConfig.createBookingTimer(simpleMeterRegistry);

    // Act - Record multiple timings
    timer.record(() -> {
      try {
        Thread.sleep(5);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });

    timer.record(() -> {
      try {
        Thread.sleep(5);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    });

    // Assert
    assertEquals(2, timer.count(), "Timer should record 2 executions");
    assertTrue(timer.totalTime(timer.baseTimeUnit()) >= 0.01,
            "Timer should record at least 10ms of execution time");
    assertTrue(timer.max(timer.baseTimeUnit()) > 0,
            "Timer should record a maximum execution time");
  }
}