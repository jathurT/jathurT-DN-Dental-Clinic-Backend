package com.uor.eng.service;

import com.uor.eng.payload.patient.CreatePatientRequest;
import com.uor.eng.payload.patient.PatientResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface IPatientService {
  PatientResponse createPatient(@Valid CreatePatientRequest request);

  List<PatientResponse> getAllPatients();

  PatientResponse getPatientById(Long patientId);

  PatientResponse updatePatient(Long patientId, @Valid CreatePatientRequest request);

  void deletePatient(Long patientId);
}
