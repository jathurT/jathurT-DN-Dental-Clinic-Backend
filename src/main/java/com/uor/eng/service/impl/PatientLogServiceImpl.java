package com.uor.eng.service.impl;

import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.Dentist;
import com.uor.eng.model.Patient;
import com.uor.eng.model.PatientLog;
import com.uor.eng.model.PatientLogPhoto;
import com.uor.eng.payload.patient.logs.*;
import com.uor.eng.repository.DentistRepository;
import com.uor.eng.repository.PatientLogPhotoRepository;
import com.uor.eng.repository.PatientLogRepository;
import com.uor.eng.repository.PatientRepository;
import com.uor.eng.service.PatientLogService;
import com.uor.eng.util.S3Service;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PatientLogServiceImpl implements PatientLogService {

  @Autowired
  private PatientRepository patientRepository;

  @Autowired
  private PatientLogRepository patientLogRepository;

  @Autowired
  private PatientLogPhotoRepository patientLogPhotoRepository;

  @Autowired
  private DentistRepository dentistRepository;

  @Autowired
  private S3Service s3Service;

  @Transactional
  @Override
  public PatientLogResponse createPatientLog(Long patientId, @Valid PatientLogRequestNoPhotos request) {
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

    Dentist dentist = dentistRepository.findById(request.getDentistId())
        .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + request.getDentistId()));

    PatientLog patientLog = new PatientLog();
    patientLog.setPatient(patient);
    patientLog.setDentist(dentist);
    patientLog.setActionType(request.getActionType());
    patientLog.setDescription(request.getDescription());
    patientLog.setTimestamp(LocalDateTime.now());
    patientLog = patientLogRepository.save(patientLog);
    return mapToResponse(patientLog);
  }

  @Override
  @Transactional
  public List<PatientLogResponse> getPatientLogs(Long patientId) {
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

    List<PatientLog> patientLogs = patientLogRepository.findByPatientId(patient.getId());
    List<PatientLogResponse> responses = new ArrayList<>();
    for (PatientLog patientLog : patientLogs) {
      List<PatientLogPhoto> photos = patientLogPhotoRepository.findByPatientId(patientLog.getId());

      List<PatientLogPhotoResponse> photoResponses = new ArrayList<>();
      for (PatientLogPhoto photo : photos) {
        PatientLogPhotoResponse photoResponse = new PatientLogPhotoResponse();
        photoResponse.setId(photo.getId());
        photoResponse.setUrl(s3Service.getFileUrl(photo.getS3Key()));
        photoResponse.setDescription(photo.getDescription());
        photoResponse.setTimestamp(photo.getTimestamp());

        photoResponses.add(photoResponse);
      }
      PatientLogResponse response = mapToResponse(patientLog);
      response.getPhotos().addAll(photoResponses);
      responses.add(response);
    }

    return responses;
  }

  @Override
  @Transactional
  public PatientLogResponse getPatientLog(Long patientId, Long logId) {
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

    PatientLog patientLog = (PatientLog) patientLogRepository.findByIdAndPatientId(logId, patient.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Patient log not found with id: " + logId));

    List<PatientLogPhoto> photos = patientLogPhotoRepository.findByPatientId(patientLog.getId());
    List<PatientLogPhotoResponse> photoResponses = new ArrayList<>();
    for (PatientLogPhoto photo : photos) {
      PatientLogPhotoResponse photoResponse = new PatientLogPhotoResponse();
      photoResponse.setId(photo.getId());
      photoResponse.setUrl(s3Service.getFileUrl(photo.getS3Key()));
      photoResponse.setDescription(photo.getDescription());
      photoResponse.setTimestamp(photo.getTimestamp());
      photoResponses.add(photoResponse);
    }
    PatientLogResponse response = mapToResponse(patientLog);
    response.getPhotos().addAll(photoResponses);
    return response;
  }

  @Override
  @Transactional
  public void deletePatientLog(Long patientId, Long logId) {
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

    PatientLog patientLog = (PatientLog) patientLogRepository.findByIdAndPatientId(logId, patient.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Patient log not found with id: " + logId));

    List<PatientLogPhoto> photos = patientLogPhotoRepository.findByPatientLogId(patientLog.getId());
    for (PatientLogPhoto photo : photos) {
      s3Service.deleteFile(photo.getS3Key());
    }

    patientLogPhotoRepository.deleteAll(photos);
    patientLogRepository.delete(patientLog);
  }

  @Override
  @Transactional
  public PatientLogResponse updatePatientLog(Long patientId, Long logId, PatientLogUpdateRequest request) {
    PatientLog log = (PatientLog) patientLogRepository.findByIdAndPatientId(logId, patientId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "PatientLog not found with id: " + logId + " for patient id: " + patientId));

    if (request.getActionType() != null && !request.getActionType().isBlank()) {
      log.setActionType(request.getActionType());
    }
    if (request.getDescription() != null) {
      log.setDescription(request.getDescription());
    }

    if (request.getPhotosToDelete() != null && !request.getPhotosToDelete().isEmpty()) {
      for (Long photoId : request.getPhotosToDelete()) {
        Optional<PatientLogPhoto> photoOpt = patientLogPhotoRepository.findById(photoId);
        if (photoOpt.isPresent() && photoOpt.get().getPatientLog().getId().equals(logId)) {
          PatientLogPhoto photo = photoOpt.get();
          s3Service.deleteFile(photo.getS3Key());
          patientLogPhotoRepository.delete(photo);
        } else {
          throw new ResourceNotFoundException("Photo not found with id: " + photoId);
        }
      }
    }

    List<PatientLogPhotoResponse> photoResponses = new ArrayList<>();
    if (request.getNewPhotos() != null && !request.getNewPhotos().isEmpty()) {
      for (MultipartFile file : request.getNewPhotos()) {
        MapToPhotoResponse(log, photoResponses, file);
      }
    }

    log.setTimestamp(LocalDateTime.now());
    patientLogRepository.save(log);
    PatientLogResponse response = mapToResponse(log);
    response.getPhotos().addAll(photoResponses);

    return response;
  }

  @Override
  @Transactional
  public List<PatientLogPhotoResponse> associatePhotosWithLog(Long patientId, Long logId, AssociatePhotosRequest request) {
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

    PatientLog patientLog = (PatientLog) patientLogRepository.findByIdAndPatientId(logId, patient.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Patient log not found with id: " + logId));

    List<String> s3Keys = request.getS3Keys();
    List<String> descriptions = request.getDescriptions();

    if (s3Keys == null || s3Keys.isEmpty()) {
      throw new IllegalArgumentException("No S3 keys provided for photo association.");
    }

    List<PatientLogPhotoResponse> photoResponses = new ArrayList<>();

    for (int i = 0; i < s3Keys.size(); i++) {
      String s3Key = s3Keys.get(i);
      String description = (descriptions != null && i < descriptions.size()) ? descriptions.get(i) : "";

      PatientLogPhoto photo = new PatientLogPhoto();
      photo.setPatientLog(patientLog);
      photo.setS3Key(s3Key);
      photo.setDescription(description);
      photo.setTimestamp(LocalDateTime.now());

      patientLogPhotoRepository.save(photo);

      PatientLogPhotoResponse response = new PatientLogPhotoResponse();
      response.setId(photo.getId());
      response.setUrl(s3Service.getFileUrl(s3Key));
      response.setDescription(photo.getDescription());
      response.setTimestamp(photo.getTimestamp());

      photoResponses.add(response);
    }

    patientLog.setTimestamp(LocalDateTime.now());
    patientLogRepository.save(patientLog);

    return photoResponses;
  }

  @Override
  @Transactional
  public List<PatientLogPhotoResponse> getPhotos(Long patientId, Long logId) {
    Patient patient = patientRepository.findById(patientId)
        .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

    PatientLog patientLog = (PatientLog) patientLogRepository.findByIdAndPatientId(logId, patient.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Patient log not found with id: " + logId));

    List<PatientLogPhoto> photos = patientLogPhotoRepository.findByPatientLogId(patientLog.getId());
    List<PatientLogPhotoResponse> photoResponses = new ArrayList<>();
    for (PatientLogPhoto photo : photos) {
      PatientLogPhotoResponse photoResponse = new PatientLogPhotoResponse();
      photoResponse.setId(photo.getId());
      photoResponse.setUrl(s3Service.getFileUrl(photo.getS3Key()));
      photoResponse.setDescription(photo.getDescription());
      photoResponse.setTimestamp(photo.getTimestamp());
      photoResponses.add(photoResponse);
    }

    return photoResponses;
  }

  private PatientLogResponse mapToResponse(PatientLog log) {
    PatientLogResponse response = new PatientLogResponse();
    response.setId(log.getId());
    response.setActionType(log.getActionType());
    response.setDescription(log.getDescription());
    response.setTimestamp(log.getTimestamp());
    response.setDentistName(log.getDentist().getFirstName());

    List<PatientLogPhotoResponse> photoResponses = new ArrayList<>();
    if (log.getPatientLogPhotos() != null && !log.getPatientLogPhotos().isEmpty()) {
      for (PatientLogPhoto photo : log.getPatientLogPhotos()) {
        PatientLogPhotoResponse photoResponse = new PatientLogPhotoResponse();
        photoResponse.setId(photo.getId());
        photoResponse.setUrl(s3Service.getFileUrl(photo.getS3Key()));
        photoResponse.setDescription(photo.getDescription());
        photoResponse.setTimestamp(photo.getTimestamp());
        photoResponses.add(photoResponse);
      }
    } else {
      response.setPhotos(new ArrayList<>());
    }
    response.setPhotos(photoResponses);
    return response;
  }

  private void MapToPhotoResponse(PatientLog patientLog, List<PatientLogPhotoResponse> photoResponses, MultipartFile file) {
    if (!file.isEmpty()) {
      String s3Key = s3Service.uploadFile(file);

      PatientLogPhoto photo = new PatientLogPhoto();
      photo.setPatientLog(patientLog);
      photo.setS3Key(s3Key);
      photo.setDescription("");
      photo.setTimestamp(LocalDateTime.now());
      patientLogPhotoRepository.save(photo);

      PatientLogPhotoResponse photoResponse = new PatientLogPhotoResponse();
      photoResponse.setId(photo.getId());
      photoResponse.setUrl(s3Service.getFileUrl(s3Key));
      photoResponse.setDescription(photo.getDescription());
      photoResponse.setTimestamp(photo.getTimestamp());

      photoResponses.add(photoResponse);
    }
  }
}
