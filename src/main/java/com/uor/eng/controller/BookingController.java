package com.uor.eng.controller;

import com.uor.eng.payload.BookingResponseDTO;
import com.uor.eng.payload.CreateBookingDTO;
import com.uor.eng.service.IBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private IBookingService bookingService;

    @PostMapping("/create")
    public ResponseEntity<BookingResponseDTO> createBooking(@RequestBody CreateBookingDTO bookingDTO) {
        BookingResponseDTO createdBookingDTO = bookingService.createBooking(bookingDTO);
        return new ResponseEntity<>(createdBookingDTO, HttpStatus.CREATED);
    }

//    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings() {
        List<BookingResponseDTO> bookingsDTO = bookingService.getAllBookings();
        return new ResponseEntity<>(bookingsDTO, HttpStatus.OK);
    }

    @GetMapping("/{referenceId}/{contactNumber}")
    public ResponseEntity<BookingResponseDTO> getBookingByReferenceIdAndContactNumber(@PathVariable String referenceId,
                                                                              @PathVariable String contactNumber) {
        BookingResponseDTO bookingDTO = bookingService.getBookingByReferenceIdAndContactNumber(referenceId, contactNumber);
        return new ResponseEntity<>(bookingDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable String id) {
        bookingService.deleteBooking(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDTO> getBookingById(@PathVariable String id) {
        BookingResponseDTO bookingDTO = bookingService.getBookingById(id);
        return new ResponseEntity<>(bookingDTO, HttpStatus.OK);
    }
}
