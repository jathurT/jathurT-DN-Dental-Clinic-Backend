package com.uor.eng.controller;

import com.uor.eng.payload.CreateReceptionistDTO;
import com.uor.eng.payload.ReceptionistResponseDTO;
import com.uor.eng.service.IReceptionistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/receptionist")
public class ReceptionistController {

  @Autowired
  private IReceptionistService receptionistService;

  @PostMapping("/create")
  public ResponseEntity<ReceptionistResponseDTO> createDentist(@Valid @RequestBody CreateReceptionistDTO receptionistDTO) {
    ReceptionistResponseDTO createdReceptionistDTO = receptionistService.createReceptionist(receptionistDTO);
    return new ResponseEntity<>(createdReceptionistDTO, HttpStatus.CREATED);
  }

  @GetMapping("/all")
  public ResponseEntity<List<ReceptionistResponseDTO>> getAllReceptionists() {
    List<ReceptionistResponseDTO> receptionistResponseDTOS = receptionistService.getAllReceptionists();
    return ResponseEntity.ok(receptionistResponseDTOS);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ReceptionistResponseDTO> getReceptionistById(@PathVariable Long id) {
    ReceptionistResponseDTO receptionistDTO = receptionistService.getReceptionistById(id);
    return ResponseEntity.ok(receptionistDTO);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ReceptionistResponseDTO> updateReceptionist(@PathVariable Long id, @Valid @RequestBody CreateReceptionistDTO receptionistDTO) {
    ReceptionistResponseDTO updatedReceptionistDTO = receptionistService.updateReceptionist(id, receptionistDTO);
    return ResponseEntity.ok(updatedReceptionistDTO);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteReceptionist(@PathVariable Long id) {
    receptionistService.deleteReceptionist(id);
    return ResponseEntity.noContent().build();
  }
}
