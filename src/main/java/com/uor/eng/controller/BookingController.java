package com.uor.eng.controller;

import com.uor.eng.payload.BookingDTO;
import com.uor.eng.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

  @Autowired
  private BookingService bookingService;

  @PostMapping("/create")
  public ResponseEntity<BookingDTO> createBooking(@RequestBody BookingDTO bookingDTO) {
    BookingDTO createdBookingDTO = bookingService.createBooking(bookingDTO);
    return new ResponseEntity<>(createdBookingDTO, HttpStatus.CREATED);
  }

  @GetMapping("/all")
  public ResponseEntity<List<BookingDTO>> getAllBookings() {
    List<BookingDTO> bookingsDTO = bookingService.getAllBookings();
    return new ResponseEntity<>(bookingsDTO, HttpStatus.OK);
  }

  @GetMapping("/{referenceId}/{contactNumber}")
  public ResponseEntity<BookingDTO> getBookingByReferenceIdAndContactNumber(@PathVariable Long referenceId,
                                                                            @PathVariable String contactNumber) {
    BookingDTO bookingDTO = bookingService.getBookingByReferenceIdAndContactNumber(referenceId, contactNumber);
    if (bookingDTO != null) {
      return new ResponseEntity<>(bookingDTO, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
    BookingDTO bookingDTO = bookingService.getBookingById(id);
    if (bookingDTO != null) {
      bookingService.deleteBooking(id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id) {
    BookingDTO bookingDTO = bookingService.getBookingById(id);
    if (bookingDTO != null) {
      return new ResponseEntity<>(bookingDTO, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }
}
