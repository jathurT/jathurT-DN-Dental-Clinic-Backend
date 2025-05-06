package com.uor.eng.util;

import com.uor.eng.exceptions.EmailSendingException;
import com.uor.eng.model.BookingStatus;
import com.uor.eng.payload.booking.BookingResponseDTO;
import com.uor.eng.payload.other.ContactDTO;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

  @Mock
  private JavaMailSender mailSender;

  private EmailService emailService;
  private EmailService spyEmailService;
  private BookingResponseDTO bookingDTO;
  private ContactDTO contactDTO;

  @BeforeEach
  void setUp() {
    // Create EmailService with the mocked JavaMailSender
    emailService = new EmailService(mailSender);

    // Create a spy of the emailService to test specific behaviors
    spyEmailService = spy(emailService);

    // Set up the fromEmail field using reflection
    ReflectionTestUtils.setField(emailService, "fromEmail", "test@dentistry.com");
    ReflectionTestUtils.setField(spyEmailService, "fromEmail", "test@dentistry.com");

    // Create a mock MimeMessage
    MimeMessage mimeMessage = mock(MimeMessage.class);

    // Set up the mailSender to return our mock MimeMessage
    lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

    // Create a mock booking DTO
    bookingDTO = BookingResponseDTO.builder()
            .referenceId("REF123456")
            .appointmentNumber(1)
            .name("John Doe")
            .contactNumber("1234567890")
            .email("john.doe@example.com")
            .address("123 Main St")
            .scheduleId(1L)
            .scheduleDate(LocalDate.now().plusDays(7))
            .scheduleDayOfWeek("Monday")
            .scheduleStartTime(LocalTime.of(10, 0))
            .doctorName("Dr. Smith")
            .status(BookingStatus.PENDING)
            .date(LocalDate.now())
            .dayOfWeek("Tuesday")
            .createdAt(LocalDateTime.now())
            .build();

    // Create a mock contact DTO
    contactDTO = new ContactDTO();
    contactDTO.setId(1L);
    contactDTO.setName("Jane Smith");
    contactDTO.setEmail("jane.smith@example.com");
    contactDTO.setContactNumber("9876543210");
    contactDTO.setSubject("Dental Inquiry");
    contactDTO.setMessage("I'd like to know more about your services.");
  }

  @Test
  void testSendBookingConfirmation() {
    // Arrange
    doNothing().when(mailSender).send(any(MimeMessage.class));

    // Act & Assert - Check that no exception is thrown
    assertDoesNotThrow(() -> {
      emailService.sendBookingConfirmation(bookingDTO);
    });

    // Verify mailSender.send was called once
    verify(mailSender, times(1)).send(any(MimeMessage.class));
  }

  @Test
  void testSendBookingCancellation() {
    // Arrange
    doNothing().when(mailSender).send(any(MimeMessage.class));

    // Act & Assert
    assertDoesNotThrow(() -> {
      emailService.sendBookingCancellation(bookingDTO);
    });

    // Verify
    verify(mailSender, times(1)).send(any(MimeMessage.class));
  }

  @Test
  void testSendBookingActivation() {
    // Arrange
    doNothing().when(mailSender).send(any(MimeMessage.class));

    // Act & Assert
    assertDoesNotThrow(() -> {
      emailService.sendBookingActivation(bookingDTO);
    });

    // Verify
    verify(mailSender, times(1)).send(any(MimeMessage.class));
  }

  @Test
  void testSendAppointmentReminder() {
    // Arrange
    doNothing().when(mailSender).send(any(MimeMessage.class));

    // Act & Assert
    assertDoesNotThrow(() -> {
      emailService.sendAppointmentReminder(bookingDTO);
    });

    // Verify
    verify(mailSender, times(1)).send(any(MimeMessage.class));
  }

  @Test
  void testSendResponseForContactUs() {
    // Arrange
    doNothing().when(mailSender).send(any(MimeMessage.class));
    String reply = "Thank you for your inquiry. We offer comprehensive dental services.";

    // Act & Assert
    assertDoesNotThrow(() -> {
      emailService.sendResponseForContactUs(contactDTO, reply);
    });

    // Verify
    verify(mailSender, times(1)).send(any(MimeMessage.class));
  }

  @Test
  void testHandleMailSenderException() throws Exception {
    // Arrange - Create a spy of EmailService to use doThrow properly
    EmailSendingException expectedException = new EmailSendingException("Failed to send email");

    // Make sendEmail throw our expected exception
    doThrow(expectedException).when(spyEmailService).sendEmail(anyString(), anyString(), any(BookingResponseDTO.class));

    // Act & Assert - Expect our specific EmailSendingException
    Exception exception = assertThrows(EmailSendingException.class, () -> {
      spyEmailService.sendBookingConfirmation(bookingDTO);
    });

    // Verify that it's the same exception
    assertSame(expectedException, exception);
  }

  @Test
  void testHandleTemplateLoadException() throws Exception {
    // Arrange - Make loadTemplate throw an IOException
    IOException ioException = new IOException("Failed to load template");
    doThrow(ioException).when(spyEmailService).loadTemplate(anyString());

    // Act & Assert - Expect an EmailSendingException
    assertThrows(EmailSendingException.class, () -> {
      spyEmailService.sendBookingConfirmation(bookingDTO);
    });

    // Verify that send was never called
    verify(mailSender, never()).send(any(MimeMessage.class));
  }

  @Test
  void testHandleNullBookingValues() {
    // Arrange - Create a booking with minimal required fields
    BookingResponseDTO nullBooking = BookingResponseDTO.builder()
            .email("john.doe@example.com") // Only email is required for sending
            .build();

    doNothing().when(mailSender).send(any(MimeMessage.class));

    // Act & Assert - Check that it handles null values gracefully
    assertDoesNotThrow(() -> {
      emailService.sendBookingConfirmation(nullBooking);
    });

    // Verify send was called
    verify(mailSender, times(1)).send(any(MimeMessage.class));
  }

  @Test
  void testHandleNullContactValues() {
    // Arrange - Create contact with minimal required fields
    ContactDTO nullContact = new ContactDTO();
    nullContact.setEmail("jane@example.com"); // Only email is required

    doNothing().when(mailSender).send(any(MimeMessage.class));

    // Act & Assert
    assertDoesNotThrow(() -> {
      emailService.sendResponseForContactUs(nullContact, "Reply");
    });

    // Verify
    verify(mailSender, times(1)).send(any(MimeMessage.class));
  }
}