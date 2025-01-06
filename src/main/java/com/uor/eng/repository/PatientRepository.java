package com.uor.eng.repository;

import com.uor.eng.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
  Optional<Patient> findByEmail(String email);

  boolean existsByEmail(String email);

  Optional<Patient> findByNic(String nic);

  boolean existsByNic(String nic);
}
