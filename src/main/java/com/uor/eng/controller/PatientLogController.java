package com.uor.eng.controller;

import com.uor.eng.payload.patient.logs.*;
import com.uor.eng.service.PatientLogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientLogController {

  @Autowired
  private PatientLogService patientLogService;

  @PostMapping("/{patientId}/logs")
  public ResponseEntity<PatientLogResponse> createPatientLog(
      @PathVariable Long patientId, @Valid @RequestBody PatientLogRequestNoPhotos request) {
    PatientLogResponse response = patientLogService.createPatientLog(patientId, request);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping("/{patientId}/logs")
  public ResponseEntity<List<PatientLogResponse>> getPatientLogs(@PathVariable Long patientId) {
    List<PatientLogResponse> response = patientLogService.getPatientLogs(patientId);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @GetMapping("/{patientId}/logs/{logId}")
  public ResponseEntity<PatientLogResponse> getPatientLog(
      @PathVariable Long patientId,
      @PathVariable Long logId) {
    PatientLogResponse log = patientLogService.getPatientLog(patientId, logId);
    return new ResponseEntity<>(log, HttpStatus.OK);
  }

  @PutMapping("/{patientId}/logs/{logId}")
  public ResponseEntity<PatientLogResponse> updatePatientLog(
      @PathVariable Long patientId,
      @PathVariable Long logId,
      @Valid @ModelAttribute PatientLogUpdateRequest request) {
    PatientLogResponse updatedLog = patientLogService.updatePatientLog(patientId, logId, request);
    return new ResponseEntity<>(updatedLog, HttpStatus.OK);
  }

  @DeleteMapping("/{patientId}/logs/{logId}")
  public ResponseEntity<Void> deletePatientLog(
      @PathVariable Long patientId,
      @PathVariable Long logId) {
    patientLogService.deletePatientLog(patientId, logId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  @PostMapping("/{patientId}/logs/{logId}/photos")
  public ResponseEntity<List<PatientLogPhotoResponse>> associatePhotos(
      @PathVariable Long patientId,
      @PathVariable Long logId,
      @RequestBody AssociatePhotosRequest request) {
    List<PatientLogPhotoResponse> responses = patientLogService.associatePhotosWithLog(patientId, logId, request);
    return new ResponseEntity<>(responses, HttpStatus.OK);
  }

  @GetMapping("/{patientId}/logs/{logId}/photos")
  public ResponseEntity<List<PatientLogPhotoResponse>> getPhotos(
      @PathVariable Long patientId,
      @PathVariable Long logId) {
    List<PatientLogPhotoResponse> responses = patientLogService.getPhotos(patientId, logId);
    return new ResponseEntity<>(responses, HttpStatus.OK);
  }
}
