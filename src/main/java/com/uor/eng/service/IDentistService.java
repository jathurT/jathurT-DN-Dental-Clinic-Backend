package com.uor.eng.service;

import com.uor.eng.payload.dentist.CreateDentistDTO;
import com.uor.eng.payload.dentist.DentistResponseDTO;
import com.uor.eng.payload.dentist.UpdateDentistRequest;
import jakarta.validation.Valid;

import java.util.List;

public interface IDentistService {
  DentistResponseDTO createDentist(@Valid CreateDentistDTO dentistDTO);

  List<DentistResponseDTO> getAllDentists();

  DentistResponseDTO updateDentist(Long id, CreateDentistDTO updateDentistDTO);

  DentistResponseDTO getDentistById(Long id);

  void deleteDentist(Long id);

  DentistResponseDTO editDentist(Long id, UpdateDentistRequest updateDentistDTO);
}
