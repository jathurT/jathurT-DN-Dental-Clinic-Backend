package com.uor.eng.controller;

import com.uor.eng.payload.CreatePatientRequest;
import com.uor.eng.payload.PatientResponse;
import com.uor.eng.service.IPatientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {
  @Autowired
  private IPatientService patientService;

  @PostMapping("/create")
  public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody CreatePatientRequest request) {
    PatientResponse response = patientService.createPatient(request);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @GetMapping("/{patientId}")
  public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long patientId) {
    PatientResponse response = patientService.getPatientById(patientId);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @GetMapping("/all")
  public ResponseEntity<List<PatientResponse>> getAllPatients() {
    List<PatientResponse> response = patientService.getAllPatients();
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @PutMapping("/{patientId}")
  public ResponseEntity<PatientResponse> updatePatient(@PathVariable Long patientId, @Valid @RequestBody CreatePatientRequest request) {
    PatientResponse response = patientService.updatePatient(patientId, request);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @DeleteMapping("/{patientId}")
  public ResponseEntity<Void> deletePatient(@PathVariable Long patientId) {
    patientService.deletePatient(patientId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
