package com.uor.eng.service;

import com.uor.eng.payload.patient.logs.*;
import jakarta.validation.Valid;

import java.util.List;

public interface PatientLogService {
  PatientLogResponse createPatientLog(Long patientId, @Valid PatientLogRequestNoPhotos request);

  List<PatientLogResponse> getPatientLogs(Long patientId);

  PatientLogResponse getPatientLog(Long patientId, Long logId);

  void deletePatientLog(Long patientId, Long logId);

  PatientLogResponse updatePatientLog(Long patientId, Long logId, @Valid PatientLogUpdateRequest request);

  List<PatientLogPhotoResponse> associatePhotosWithLog(Long patientId, Long logId, AssociatePhotosRequest request);

  List<PatientLogPhotoResponse> getPhotos(Long patientId, Long logId);
}
