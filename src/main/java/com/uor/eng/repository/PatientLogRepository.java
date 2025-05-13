package com.uor.eng.repository;

import com.uor.eng.model.PatientLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientLogRepository extends JpaRepository<PatientLog, Long> {
  List<PatientLog> findByPatientId(Long id);

  Optional<Object> findByIdAndPatientId(Long logId, Long id);

  List<PatientLog> findByDentistUserId(Long userId);
}
