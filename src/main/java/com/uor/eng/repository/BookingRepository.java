package com.uor.eng.repository;

import com.uor.eng.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
  Optional<Booking> findByReferenceIdAndContactNumber(Long referenceId, String contactNumber);
}
