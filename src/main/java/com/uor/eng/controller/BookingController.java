package com.uor.eng.controller;

import com.uor.eng.payload.BookingDTO;
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
  public ResponseEntity<?> createBooking(@RequestBody BookingDTO bookingDTO) {
    try {
      BookingDTO createdBookingDTO = bookingService.createBooking(bookingDTO);
      return new ResponseEntity<>(createdBookingDTO, HttpStatus.CREATED);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @GetMapping("/all")
  public ResponseEntity<?> getAllBookings() {
    try {
      List<BookingDTO> bookingsDTO = bookingService.getAllBookings();
      return new ResponseEntity<>(bookingsDTO, HttpStatus.OK);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NO_CONTENT);
    }
  }

  @GetMapping("/{referenceId}/{contactNumber}")
  public ResponseEntity<?> getBookingByReferenceIdAndContactNumber(@PathVariable Long referenceId,
                                                                   @PathVariable String contactNumber) {
    try {
      BookingDTO bookingDTO = bookingService.getBookingByReferenceIdAndContactNumber(referenceId, contactNumber);
      return new ResponseEntity<>(bookingDTO, HttpStatus.OK);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
    try {
      bookingService.deleteBooking(id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getBookingById(@PathVariable Long id) {
    try {
      BookingDTO bookingDTO = bookingService.getBookingById(id);
      return new ResponseEntity<>(bookingDTO, HttpStatus.OK);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
  }
}
