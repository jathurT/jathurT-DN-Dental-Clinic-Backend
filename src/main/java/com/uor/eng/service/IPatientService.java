package com.uor.eng.service;

import com.uor.eng.payload.CreatePatientRequest;
import com.uor.eng.payload.PatientResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface IPatientService {
  PatientResponse createPatient(@Valid CreatePatientRequest request);

  List<PatientResponse> getAllPatients();

  PatientResponse getPatientById(Long patientId);

  PatientResponse updatePatient(Long patientId, @Valid CreatePatientRequest request);

  void deletePatient(Long patientId);
}
