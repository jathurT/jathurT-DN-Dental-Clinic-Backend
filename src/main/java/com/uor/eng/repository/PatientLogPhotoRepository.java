package com.uor.eng.repository;

import com.uor.eng.model.PatientLogPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientLogPhotoRepository extends JpaRepository<PatientLogPhoto, Long> {
  @Query("SELECT p FROM PatientLogPhoto p WHERE p.patientLog.patient.id = ?1")
  List<PatientLogPhoto> findByPatientId(Long id);

  List<PatientLogPhoto> findByPatientLogId(Long id);

}
