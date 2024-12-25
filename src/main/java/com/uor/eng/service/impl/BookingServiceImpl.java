package com.uor.eng.service.impl;

import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.Booking;
import com.uor.eng.model.Schedule;
import com.uor.eng.model.ScheduleStatus;
import com.uor.eng.payload.BookingResponseDTO;
import com.uor.eng.payload.CreateBookingDTO;
import com.uor.eng.repository.BookingRepository;
import com.uor.eng.repository.ScheduleRepository;
import com.uor.eng.service.IBookingService;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Override
  public BookingResponseDTO createBooking(CreateBookingDTO bookingDTO) {
    Schedule schedule = scheduleRepository.findById(bookingDTO.getScheduleId())
        .orElseThrow(() -> new ResourceNotFoundException("Schedule with ID " + bookingDTO.getScheduleId() + " not found. Please select a valid schedule."));

    if (schedule.getStatus() == ScheduleStatus.UNAVAILABLE ||
        schedule.getStatus() == ScheduleStatus.FULL) {
      throw new BadRequestException("Cannot create booking. The selected schedule is currently unavailable.");
    }

    Booking booking = modelMapper.map(bookingDTO, Booking.class);

    Booking savedBooking = bookingRepository.save(booking);
    return modelMapper.map(savedBooking, BookingResponseDTO.class);
  }

  @Override
  public List<BookingResponseDTO> getAllBookings() {
    List<Booking> bookings = bookingRepository.findAll();
    if (bookings.isEmpty()) {
      throw new ResourceNotFoundException("No bookings found. Please create a booking to view the list.");
    }

    return bookings.stream()
        .map(booking -> modelMapper.map(booking, BookingResponseDTO.class))
        .collect(Collectors.toList());
  }

  @Override
  public BookingResponseDTO getBookingByReferenceIdAndContactNumber(String referenceId, String contactNumber) {
    return bookingRepository.findByReferenceIdAndContactNumber(referenceId, contactNumber)
        .map(value -> modelMapper.map(value, BookingResponseDTO.class))
        .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference ID " + referenceId + " and contact number " + contactNumber + ". Please verify the details and try again."));
  }

  @Override
  public BookingResponseDTO getBookingById(String id) {
    return bookingRepository.findById(id)
        .map(booking -> {
          modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
          return modelMapper.map(booking, BookingResponseDTO.class);
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
}
