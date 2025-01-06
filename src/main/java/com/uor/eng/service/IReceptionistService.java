package com.uor.eng.service;

import com.uor.eng.payload.receiptionist.CreateReceptionistDTO;
import com.uor.eng.payload.receiptionist.ReceptionistResponseDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface IReceptionistService {
  ReceptionistResponseDTO createReceptionist(@Valid CreateReceptionistDTO receptionistDTO);

  List<ReceptionistResponseDTO> getAllReceptionists();

  ReceptionistResponseDTO getReceptionistById(Long id);

  ReceptionistResponseDTO updateReceptionist(Long id, @Valid CreateReceptionistDTO receptionistDTO);

  void deleteReceptionist(Long id);
}
