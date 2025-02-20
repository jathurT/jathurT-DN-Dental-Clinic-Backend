package com.uor.eng.controller;

import com.uor.eng.payload.booking.BookingResponseDTO;
import com.uor.eng.payload.booking.CreateBookingDTO;
import com.uor.eng.payload.dashboard.BookingCountResponse;
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

  @PutMapping("/{id}")
  public ResponseEntity<BookingResponseDTO> updateBooking(@PathVariable String id, @RequestBody CreateBookingDTO bookingDTO) {
    BookingResponseDTO updatedBooking = bookingService.updateBooking(id, bookingDTO);
    return new ResponseEntity<>(updatedBooking, HttpStatus.OK);
  }

  @PutMapping("/updateStatus/{id}")
  public ResponseEntity<BookingResponseDTO> updateBookingStatus(@PathVariable String id, @RequestParam String status) {
    BookingResponseDTO updatedBooking = bookingService.updateBookingStatus(id, status);
    return new ResponseEntity<>(updatedBooking, HttpStatus.OK);
  }

  @GetMapping("/currentMonth/finished")
  public ResponseEntity<BookingCountResponse> getFinishedBookingsOfCurrentMonth() {
    BookingCountResponse finishedBookings = bookingService.getFinishedBookingsOfCurrentMonth();
    return new ResponseEntity<>(finishedBookings, HttpStatus.OK);
  }

  @GetMapping("/currentMonth/cancelled")
  public ResponseEntity<BookingCountResponse> getCancelledBookingsOfCurrentMonth() {
    BookingCountResponse cancelledBookings = bookingService.getCancelledBookingsOfCurrentMonth();
    return new ResponseEntity<>(cancelledBookings, HttpStatus.OK);
  }

  @GetMapping("/currentMonth/total")
  public ResponseEntity<BookingCountResponse> getTotalBookingsOfCurrentMonth() {
    BookingCountResponse totalBookings = bookingService.getTotalBookingsOfCurrentMonth();
    return new ResponseEntity<>(totalBookings, HttpStatus.OK);
  }

  @GetMapping("/currentMonth/pending")
  public ResponseEntity<BookingCountResponse> getPendingBookingsOfCurrentMonth() {
    BookingCountResponse pendingBookings = bookingService.getPendingBookingsOfCurrentMonth();
    return new ResponseEntity<>(pendingBookings, HttpStatus.OK);
  }
}
