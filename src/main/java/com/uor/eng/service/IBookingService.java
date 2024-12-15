package com.uor.eng.service;

import com.uor.eng.payload.BookingDTO;

import java.util.List;

public interface IBookingService {
    BookingDTO createBooking(BookingDTO bookingDTO);

    List<BookingDTO> getAllBookings();

    BookingDTO getBookingByReferenceIdAndContactNumber(Long referenceId, String contactNumber);

    BookingDTO getBookingById(Long id);

    void deleteBooking(Long id);
}
