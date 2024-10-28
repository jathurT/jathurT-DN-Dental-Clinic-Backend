package com.uor.eng.service;

import com.uor.eng.model.Booking;
import com.uor.eng.payload.BookingDTO;
import com.uor.eng.repository.BookingRepository;
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

  @Override
  public BookingDTO createBooking(BookingDTO bookingDTO) {
    Booking booking = modelMapper.map(bookingDTO, Booking.class);
    Booking savedBooking = bookingRepository.save(booking);
    return modelMapper.map(savedBooking, BookingDTO.class);
  }

  @Override
  public List<BookingDTO> getAllBookings() {
    List<Booking> bookings = bookingRepository.findAll();
    return bookings.stream()
            .map(booking -> modelMapper.map(booking, BookingDTO.class))
            .collect(Collectors.toList());
  }

  @Override
  public BookingDTO getBookingByReferenceIdAndContactNumber(Long referenceId, String contactNumber) {
    Optional<Booking> booking = bookingRepository.findByReferenceIdAndContactNumber(referenceId, contactNumber);
    return booking.map(value -> modelMapper.map(value, BookingDTO.class)).orElse(null);
  }

  @Override
  public BookingDTO getBookingById(Long id) {
    Optional<Booking> booking = bookingRepository.findById(id);
    return booking.map(value -> modelMapper.map(value, BookingDTO.class)).orElse(null);
  }

  @Override
  public void deleteBooking(Long id) {
    bookingRepository.deleteById(id);
  }
}
