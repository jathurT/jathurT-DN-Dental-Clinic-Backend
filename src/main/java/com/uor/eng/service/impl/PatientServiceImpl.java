package com.uor.eng.service.impl;

import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.Patient;
import com.uor.eng.payload.patient.CreatePatientRequest;
import com.uor.eng.payload.patient.logs.PatientLogResponse;
import com.uor.eng.payload.patient.PatientResponse;
import com.uor.eng.repository.PatientRepository;
import com.uor.eng.service.IPatientService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientServiceImpl implements IPatientService {

  @Autowired
  private ModelMapper modelMapper;

  @Autowired
  private PatientRepository patientRepository;

  @Override
  @Transactional
  public PatientResponse createPatient(CreatePatientRequest request) {
    if (patientRepository.existsByEmail(request.getEmail())) {
      throw new BadRequestException("Email is already in use!");
    }
    if (patientRepository.existsByNic(request.getNic())) {
      throw new BadRequestException("NIC is already in use!");
    }

    Patient patient = new Patient();
    patient.setName(request.getName());
    patient.setEmail(request.getEmail());
    patient.setNic(request.getNic());
    patient.setContactNumbers(request.getContactNumbers());

    Patient savedPatient = patientRepository.save(patient);

    return mapToResponse(savedPatient);
  }

  @Transactional(readOnly = true)
  @Override
  public List<PatientResponse> getAllPatients() {
    List<Patient> patients = patientRepository.findAll();
    return patients.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  @Override
  public PatientResponse getPatientById(Long patientId) {
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
    return mapToResponse(patient);
  }

  @Transactional
  @Override
  public PatientResponse updatePatient(Long patientId, CreatePatientRequest request) {
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

    if (request.getName() != null && !request.getName().isBlank()) {
      patient.setName(request.getName());
    }

    if (request.getEmail() != null && !request.getEmail().isBlank() &&
        !request.getEmail().equals(patient.getEmail())) {
      if (patientRepository.existsByEmail(request.getEmail())) {
        throw new BadRequestException("Email is already in use!");
      }
      patient.setEmail(request.getEmail());
    }

    if (request.getNic() != null && !request.getNic().isBlank() &&
        !request.getNic().equals(patient.getNic())) {
      if (patientRepository.existsByNic(request.getNic())) {
        throw new BadRequestException("NIC is already in use!");
      }
      patient.setNic(request.getNic());
    }

    if (request.getContactNumbers() != null && !request.getContactNumbers().isEmpty()) {
      patient.setContactNumbers(request.getContactNumbers());
    }

    Patient updatedPatient = patientRepository.save(patient);
    return mapToResponse(updatedPatient);
  }

  @Transactional
  @Override
  public void deletePatient(Long patientId) {
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

    patientRepository.delete(patient);
  }

  private PatientResponse mapToResponse(Patient patient) {
    PatientResponse response = new PatientResponse();
    response.setId(patient.getId());
    response.setName(patient.getName());
    response.setEmail(patient.getEmail());
    response.setNic(patient.getNic());
    response.setContactNumbers(patient.getContactNumbers());
    List<PatientLogResponse> patientLogResponses = Optional.ofNullable(patient.getPatientLogs())
        .orElseGet(ArrayList::new)
        .stream()
        .map(log -> modelMapper.map(log, PatientLogResponse.class))
        .collect(Collectors.toList());

    response.setLogs(patientLogResponses);
    return response;
  }
}

