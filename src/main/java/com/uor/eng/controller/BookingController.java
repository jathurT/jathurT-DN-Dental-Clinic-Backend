package com.uor.eng.controller;

import com.uor.eng.payload.BookingDTO;
import com.uor.eng.service.IBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private IBookingService bookingService;

    @PostMapping("/create")
    public ResponseEntity<BookingDTO> createBooking(@RequestBody BookingDTO bookingDTO) {
        BookingDTO createdBookingDTO = bookingService.createBooking(bookingDTO);
        return new ResponseEntity<>(createdBookingDTO, HttpStatus.CREATED);
    }

//    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        List<BookingDTO> bookingsDTO = bookingService.getAllBookings();
        return new ResponseEntity<>(bookingsDTO, HttpStatus.OK);
    }

    @GetMapping("/{referenceId}/{contactNumber}")
    public ResponseEntity<BookingDTO> getBookingByReferenceIdAndContactNumber(@PathVariable String referenceId,
                                                                              @PathVariable String contactNumber) {
        BookingDTO bookingDTO = bookingService.getBookingByReferenceIdAndContactNumber(referenceId, contactNumber);
        return new ResponseEntity<>(bookingDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable String id) {
        bookingService.deleteBooking(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable String id) {
        BookingDTO bookingDTO = bookingService.getBookingById(id);
        return new ResponseEntity<>(bookingDTO, HttpStatus.OK);
    }
}
