package com.uor.eng.controller;

import com.uor.eng.payload.receiptionist.CreateReceptionistDTO;
import com.uor.eng.payload.receiptionist.ReceptionistResponseDTO;
import com.uor.eng.payload.receiptionist.UpdateReceptionistRequest;
import com.uor.eng.service.IReceptionistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/receptionist")
public class ReceptionistController {

  private final IReceptionistService receptionistService;

  public ReceptionistController(IReceptionistService receptionistService) {
    this.receptionistService = receptionistService;
  }

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

  @PutMapping("/edit/{id}")
  public ResponseEntity<ReceptionistResponseDTO> editReceptionist(@PathVariable Long id, @Valid @RequestBody UpdateReceptionistRequest receptionistDTO) {
    ReceptionistResponseDTO updatedReceptionistDTO = receptionistService.editReceptionist(id, receptionistDTO);
    return ResponseEntity.ok(updatedReceptionistDTO);
  }
}
