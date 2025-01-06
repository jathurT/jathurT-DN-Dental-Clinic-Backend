package com.uor.eng.controller;

import com.uor.eng.payload.patient.PatientLogRequest;
import com.uor.eng.payload.patient.PatientLogResponse;
import com.uor.eng.payload.patient.PatientLogUpdateRequest;
import com.uor.eng.service.PatientLogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@Validated
public class PatientLogController {

  @Autowired
  private PatientLogService patientLogService;

  @PostMapping("/{patientId}/logs")
  public ResponseEntity<PatientLogResponse> createPatientLog(
      @PathVariable Long patientId, @Valid PatientLogRequest request) {
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
}
