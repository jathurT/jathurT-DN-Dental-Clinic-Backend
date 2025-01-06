package com.uor.eng.controller;

import com.uor.eng.payload.dentist.CreateDentistDTO;
import com.uor.eng.payload.dentist.DentistResponseDTO;
import com.uor.eng.payload.dentist.UpdateDentistRequest;
import com.uor.eng.service.IDentistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dentist")
public class DentistController {

  @Autowired
  private IDentistService dentistService;

  @PostMapping("/create")
  public ResponseEntity<DentistResponseDTO> createDentist(@Valid @RequestBody CreateDentistDTO dentistDTO) {
    DentistResponseDTO createdDentistDTO = dentistService.createDentist(dentistDTO);
    return new ResponseEntity<>(createdDentistDTO, HttpStatus.CREATED);
  }

  @GetMapping("/all")
  public ResponseEntity<List<DentistResponseDTO>> getAllDentists() {
    List<DentistResponseDTO> dentistDTOs = dentistService.getAllDentists();
    return ResponseEntity.ok(dentistDTOs);
  }

  @GetMapping("/{id}")
  public ResponseEntity<DentistResponseDTO> getDentistById(@PathVariable Long id) {
    DentistResponseDTO dentistDTO = dentistService.getDentistById(id);
    return ResponseEntity.ok(dentistDTO);
  }

  @PutMapping("/{id}")
  public ResponseEntity<DentistResponseDTO> updateDentist(@PathVariable Long id, @Validated @RequestBody CreateDentistDTO updateDentistDTO) {
    DentistResponseDTO updatedDentist = dentistService.updateDentist(id, updateDentistDTO);
    return ResponseEntity.ok(updatedDentist);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteDentist(@PathVariable Long id) {
    dentistService.deleteDentist(id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/edit/{id}")
  public ResponseEntity<DentistResponseDTO> editDentist(@PathVariable Long id, @Validated @RequestBody UpdateDentistRequest updateDentistDTO) {
    DentistResponseDTO updatedDentist = dentistService.editDentist(id, updateDentistDTO);
    return ResponseEntity.ok(updatedDentist);
  }


}
