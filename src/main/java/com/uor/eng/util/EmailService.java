package com.uor.eng.util;

import com.uor.eng.payload.booking.BookingResponseDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

  @Autowired
  private JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String fromEmail;

  public void sendBookingConfirmation(BookingResponseDTO bookingDetails) throws MessagingException {
    try {
      String template = loadTemplate("templates/booking-confirmation.html");

      String htmlContent = populateTemplate(template, bookingDetails);

      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(bookingDetails.getEmail());
      helper.setSubject("Appointment Confirmation - Reference ID: " + bookingDetails.getReferenceId());
      helper.setText(htmlContent, true);

      mailSender.send(message);
    } catch (MessagingException | IOException e) {
      System.err.println("Failed to send booking confirmation email: " + e.getMessage());
    }
  }

  private String loadTemplate(String templatePath) throws IOException {
    ClassPathResource classPathResource = new ClassPathResource(templatePath);
    try (InputStream inputStream = classPathResource.getInputStream();
         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      StringBuilder content = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        content.append(line).append("\n");
      }
      return content.toString();
    }
  }

  private String populateTemplate(String template, BookingResponseDTO bookingDetails) {
    Map<String, String> placeholders = new HashMap<>();
    placeholders.put("{{name}}", bookingDetails.getName());
    placeholders.put("{{referenceId}}", bookingDetails.getReferenceId());
    placeholders.put("{{appointmentNumber}}", String.valueOf(bookingDetails.getAppointmentNumber()));
    placeholders.put("{{scheduleDate}}", bookingDetails.getScheduleDate().toString());
    placeholders.put("{{scheduleDayOfWeek}}", bookingDetails.getScheduleDayOfWeek());
    placeholders.put("{{scheduleStartTime}}", bookingDetails.getScheduleStartTime().toString());
    placeholders.put("{{doctorName}}", bookingDetails.getDoctorName());
    placeholders.put("{{status}}", bookingDetails.getStatus().toString());
    placeholders.put("{{bookingDate}}", bookingDetails.getDate().toString());
    placeholders.put("{{currentYear}}", LocalDate.now().getYear() + "");

    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
      template = template.replace(entry.getKey(), entry.getValue());
    }

    return template;
  }
}
