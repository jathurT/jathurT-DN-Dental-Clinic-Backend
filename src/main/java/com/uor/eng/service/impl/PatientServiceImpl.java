package com.uor.eng.service.impl;

import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.Patient;
import com.uor.eng.model.PatientLog;
import com.uor.eng.model.PatientLogPhoto;
import com.uor.eng.payload.patient.CreatePatientRequest;
import com.uor.eng.payload.patient.logs.PatientLogPhotoResponse;
import com.uor.eng.payload.patient.logs.PatientLogResponse;
import com.uor.eng.payload.patient.PatientResponse;
import com.uor.eng.repository.PatientRepository;
import com.uor.eng.service.IPatientService;
import com.uor.eng.util.S3Service;
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

  private final ModelMapper modelMapper;
  private final PatientRepository patientRepository;
  private final S3Service s3Service;

  public PatientServiceImpl(ModelMapper modelMapper,
                            PatientRepository patientRepository,
                            S3Service s3Service) {
    this.modelMapper = modelMapper;
    this.patientRepository = patientRepository;
    this.s3Service = s3Service;
  }

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
        .map(log -> {
          List<PatientLogPhoto> photos = log.getPatientLogPhotos();
          List<PatientLogPhotoResponse> photoResponses = new ArrayList<>();
          for (PatientLogPhoto photo : photos) {
            PatientLogPhotoResponse photoResponse = new PatientLogPhotoResponse();
            photoResponse.setId(photo.getId());
            photoResponse.setUrl(s3Service.getFileUrl(photo.getS3Key()));
            photoResponse.setDescription(photo.getDescription());
            photoResponse.setTimestamp(photo.getTimestamp());
            photoResponses.add(photoResponse);
          }
          PatientLogResponse logResponse = mapLogToResponse(log);
          logResponse.setPhotos(photoResponses);
          return logResponse;
        })
        .collect(Collectors.toList());

    response.setLogs(patientLogResponses);
    return response;
  }

  private PatientLogResponse mapLogToResponse(PatientLog log) {
    PatientLogResponse response = new PatientLogResponse();
    response.setId(log.getId());
    response.setActionType(log.getActionType());
    response.setDescription(log.getDescription());
    response.setTimestamp(log.getTimestamp());
    response.setDentistName(log.getDentist().getFirstName());
    return response;
  }
}

