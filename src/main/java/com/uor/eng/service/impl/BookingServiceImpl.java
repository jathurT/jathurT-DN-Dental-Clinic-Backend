package com.uor.eng.service.impl;

import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.Booking;
import com.uor.eng.model.Schedule;
import com.uor.eng.model.ScheduleStatus;
import com.uor.eng.payload.booking.BookingResponseDTO;
import com.uor.eng.payload.booking.CreateBookingDTO;
import com.uor.eng.repository.BookingRepository;
import com.uor.eng.repository.ScheduleRepository;
import com.uor.eng.service.IBookingService;
import com.uor.eng.util.EmailService;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements IBookingService {

  @Autowired
  private BookingRepository bookingRepository;

  @Autowired
  private ModelMapper modelMapper;

  @Autowired
  private ScheduleRepository scheduleRepository;

  @Autowired
  private EmailService emailService;

  @Override
  @Transactional
  public BookingResponseDTO createBooking(CreateBookingDTO bookingDTO) {
    try {
      Schedule schedule = scheduleRepository.findById(bookingDTO.getScheduleId())
          .orElseThrow(() -> new ResourceNotFoundException("Schedule with ID " + bookingDTO.getScheduleId() + " not found. Please select a valid schedule."));

      if (schedule.getAvailableSlots() == 0 || schedule.getStatus() == ScheduleStatus.FULL) {
        throw new BadRequestException("Cannot create booking. The selected schedule is currently full.");
      } else if (schedule.getStatus() == ScheduleStatus.UNAVAILABLE || schedule.getStatus() == ScheduleStatus.CANCELLED) {
        throw new BadRequestException("Cannot create booking. The selected schedule is currently unavailable.");
      }

      Booking booking = modelMapper.map(bookingDTO, Booking.class);
      schedule.setAvailableSlots(schedule.getAvailableSlots() - 1);
      booking.setAppointmentNumber(schedule.getCapacity() - schedule.getAvailableSlots());

      if (schedule.getAvailableSlots() == 0) {
        schedule.setStatus(ScheduleStatus.FULL);
      }
      Booking savedBooking = bookingRepository.save(booking);
      scheduleRepository.save(schedule);

      BookingResponseDTO bookingResponseDTO = mapToResponse(savedBooking);
      emailService.sendBookingConfirmation(bookingResponseDTO);
      return bookingResponseDTO;
    } catch (OptimisticLockingFailureException e) {
      throw new BadRequestException("Unable to create booking due to high demand. Please try again.");
    } catch (Exception e) {
      throw new BadRequestException("Unable to create booking. Please check the details and try again.");
    }
  }

  @Override
  public List<BookingResponseDTO> getAllBookings() {
    List<Booking> bookings = bookingRepository.findAll();
    if (bookings.isEmpty()) {
      throw new ResourceNotFoundException("No bookings found. Please create a booking to view the list.");
    }

    return bookings.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Override
  public BookingResponseDTO getBookingByReferenceIdAndContactNumber(String referenceId, String contactNumber) {
    return bookingRepository.findByReferenceIdAndContactNumber(referenceId, contactNumber)
        .map(this::mapToResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference ID " + referenceId + " and contact number " + contactNumber + ". Please verify the details and try again."));
  }

  @Override
  public BookingResponseDTO getBookingById(String id) {
    return bookingRepository.findById(id)
        .map(booking -> {
          modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
          return mapToResponse(booking);
        })
        .orElseThrow(() -> new ResourceNotFoundException("Booking with ID " + id + " not found. Please check the ID and try again."));
  }

  @Override
  public void deleteBooking(String id) {
    Optional<Booking> booking = bookingRepository.findById(id);
    if (booking.isPresent()) {
      bookingRepository.deleteById(id);
    } else {
      throw new ResourceNotFoundException("Booking with ID " + id + " does not exist. Unable to delete.");
    }
  }

  @Override
  public BookingResponseDTO updateBooking(String id, CreateBookingDTO bookingDTO) {
    Booking booking = bookingRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Booking with ID " + id + " not found. Please check the ID and try again."));
    modelMapper.map(bookingDTO, booking);
    Booking updatedBooking = bookingRepository.save(booking);
    return mapToResponse(updatedBooking);
  }

  private Schedule getSchedule(Long scheduleId) {
    return scheduleRepository.findById(scheduleId)
        .orElseThrow(() -> new ResourceNotFoundException("Schedule with ID " + scheduleId + " not found. Please select a valid schedule."));
  }

  private BookingResponseDTO mapToResponse(Booking booking) {
    BookingResponseDTO bookingResponseDTO = modelMapper.map(booking, BookingResponseDTO.class);
    Long scheduleId = booking.getSchedule().getId();
    Schedule schedule = getSchedule(scheduleId);
    bookingResponseDTO.setScheduleDate(schedule.getDate());
    bookingResponseDTO.setScheduleDayOfWeek(schedule.getDayOfWeek());
    bookingResponseDTO.setScheduleStartTime(schedule.getStartTime());
    bookingResponseDTO.setDayOfWeek(schedule.getDayOfWeek());
    bookingResponseDTO.setDoctorName(schedule.getDentist().getFirstName());
    bookingResponseDTO.setScheduleStatus(schedule.getStatus());
    return bookingResponseDTO;
  }
}
