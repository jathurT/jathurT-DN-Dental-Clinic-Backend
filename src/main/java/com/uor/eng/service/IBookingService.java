package com.uor.eng.service;

import com.uor.eng.payload.BookingResponseDTO;
import com.uor.eng.payload.CreateBookingDTO;

import java.util.List;

public interface IBookingService {
    BookingResponseDTO createBooking(CreateBookingDTO bookingDTO);

    List<BookingResponseDTO> getAllBookings();

    BookingResponseDTO getBookingByReferenceIdAndContactNumber(String referenceId, String contactNumber);

    BookingResponseDTO getBookingById(String id);

    void deleteBooking(String id);
}
