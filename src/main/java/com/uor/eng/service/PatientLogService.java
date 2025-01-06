package com.uor.eng.service;

import com.uor.eng.payload.PatientLogRequest;
import com.uor.eng.payload.PatientLogResponse;
import com.uor.eng.payload.PatientLogUpdateRequest;
import jakarta.validation.Valid;

import java.util.List;

public interface PatientLogService {
  PatientLogResponse createPatientLog(Long patientId, PatientLogRequest request);

  List<PatientLogResponse> getPatientLogs(Long patientId);

  PatientLogResponse getPatientLog(Long patientId, Long logId);

  void deletePatientLog(Long patientId, Long logId);

  PatientLogResponse updatePatientLog(Long patientId, Long logId, @Valid PatientLogUpdateRequest request);
}
