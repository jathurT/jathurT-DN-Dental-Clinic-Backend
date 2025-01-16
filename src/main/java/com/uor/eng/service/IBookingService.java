package com.uor.eng.service;

import com.uor.eng.payload.booking.BookingResponseDTO;
import com.uor.eng.payload.booking.CreateBookingDTO;

import java.util.List;

public interface IBookingService {
  BookingResponseDTO createBooking(CreateBookingDTO bookingDTO);

  List<BookingResponseDTO> getAllBookings();

  BookingResponseDTO getBookingByReferenceIdAndContactNumber(String referenceId, String contactNumber);

  BookingResponseDTO getBookingById(String id);

  void deleteBooking(String id);

  BookingResponseDTO updateBooking(String id, CreateBookingDTO bookingDTO);

  BookingResponseDTO updateBookingStatus(String id, String status);
}
