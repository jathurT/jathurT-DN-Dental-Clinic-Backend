package com.uor.eng.service;

import com.uor.eng.payload.BookingDTO;

import java.util.List;

public interface IBookingService {
    BookingDTO createBooking(BookingDTO bookingDTO);

    List<BookingDTO> getAllBookings();

    BookingDTO getBookingByReferenceIdAndContactNumber(String referenceId, String contactNumber);

    BookingDTO getBookingById(String id);

    void deleteBooking(String id);
}
