package com.uor.eng.service;

import com.uor.eng.payload.BookingDTO;

import java.util.List;

public interface BookingService {
  BookingDTO createBooking(BookingDTO bookingDTO);
  List<BookingDTO> getAllBookings();
  BookingDTO getBookingByReferenceIdAndContactNumber(Long referenceId, String contactNumber);
}
