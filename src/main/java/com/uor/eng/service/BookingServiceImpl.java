package com.uor.eng.service;

import com.uor.eng.model.Booking;
import com.uor.eng.model.Schedule;
import com.uor.eng.payload.BookingDTO;
import com.uor.eng.repository.BookingRepository;
import com.uor.eng.repository.ScheduleRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

  @Autowired
  private BookingRepository bookingRepository;

  @Autowired
  private ModelMapper modelMapper;

  @Autowired
  private ScheduleRepository scheduleRepository;

  @Override
  public BookingDTO createBooking(BookingDTO bookingDTO) {
    Schedule schedule = scheduleRepository.findById(bookingDTO.getScheduleId())
            .orElseThrow(() -> new RuntimeException("Schedule with ID " + bookingDTO.getScheduleId() + " not found. Please select a valid schedule."));

    if ("unavailable".equalsIgnoreCase(schedule.getStatus())) {
      throw new RuntimeException("Cannot create booking. The selected schedule is currently unavailable.");
    }
    Booking booking = modelMapper.map(bookingDTO, Booking.class);
    Booking savedBooking = bookingRepository.save(booking);
    return modelMapper.map(savedBooking, BookingDTO.class);
  }

  @Override
  public List<BookingDTO> getAllBookings() {
    List<Booking> bookings = bookingRepository.findAll();
    if (bookings.isEmpty()) {
      throw new RuntimeException("No bookings found. Please create a booking to view the list.");
    }
    return bookings.stream()
            .map(booking -> modelMapper.map(booking, BookingDTO.class))
            .collect(Collectors.toList());
  }

  @Override
  public BookingDTO getBookingByReferenceIdAndContactNumber(Long referenceId, String contactNumber) {
    return bookingRepository.findByReferenceIdAndContactNumber(referenceId, contactNumber)
            .map(value -> modelMapper.map(value, BookingDTO.class))
            .orElseThrow(() -> new RuntimeException("Booking not found with reference ID " + referenceId + " and contact number " + contactNumber + ". Please verify the details and try again."));
  }

  @Override
  public BookingDTO getBookingById(Long id) {
    return bookingRepository.findById(id)
            .map(value -> modelMapper.map(value, BookingDTO.class))
            .orElseThrow(() -> new RuntimeException("Booking with ID " + id + " not found. Please check the ID and try again."));
  }

  @Override
  public void deleteBooking(Long id) {
    Optional<Booking> booking = bookingRepository.findById(id);
    if (booking.isPresent()) {
      bookingRepository.deleteById(id);
    } else {
      throw new RuntimeException("Booking with ID " + id + " does not exist. Unable to delete.");
    }
  }
}
