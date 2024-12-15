package com.uor.eng.service.impl;

import com.uor.eng.model.Booking;
import com.uor.eng.model.Schedule;
import com.uor.eng.payload.BookingDTO;
import com.uor.eng.repository.BookingRepository;
import com.uor.eng.repository.ScheduleRepository;
import com.uor.eng.service.IBookingService;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.exceptions.BadRequestException;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements IBookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Override
    public BookingDTO createBooking(BookingDTO bookingDTO) {
        Schedule schedule = scheduleRepository.findById(bookingDTO.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Schedule with ID " + bookingDTO.getScheduleId() + " not found. Please select a valid schedule."));

        if ("unavailable".equalsIgnoreCase(schedule.getStatus())) {
            throw new BadRequestException("Cannot create booking. The selected schedule is currently unavailable.");
        }

        Booking booking = modelMapper.map(bookingDTO, Booking.class);
        Booking savedBooking = bookingRepository.save(booking);
        return modelMapper.map(savedBooking, BookingDTO.class);
    }

    @Override
    public List<BookingDTO> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        if (bookings.isEmpty()) {
            throw new ResourceNotFoundException("No bookings found. Please create a booking to view the list.");
        }

        return bookings.stream()
                .map(booking -> modelMapper.map(booking, BookingDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public BookingDTO getBookingByReferenceIdAndContactNumber(Long referenceId, String contactNumber) {
        return bookingRepository.findByReferenceIdAndContactNumber(referenceId, contactNumber)
                .map(value -> modelMapper.map(value, BookingDTO.class))
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference ID " + referenceId + " and contact number " + contactNumber + ". Please verify the details and try again."));
    }

    @Override
    public BookingDTO getBookingById(Long id) {
        return bookingRepository.findById(id)
                .map(booking -> {
                    modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
                    return modelMapper.map(booking, BookingDTO.class);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Booking with ID " + id + " not found. Please check the ID and try again."));
    }

    @Override
    public void deleteBooking(Long id) {
        Optional<Booking> booking = bookingRepository.findById(id);
        if (booking.isPresent()) {
            bookingRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("Booking with ID " + id + " does not exist. Unable to delete.");
        }
    }
}
