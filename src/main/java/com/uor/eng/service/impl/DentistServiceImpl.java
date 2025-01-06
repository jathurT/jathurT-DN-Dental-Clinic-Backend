package com.uor.eng.service.impl;

import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.AppRole;
import com.uor.eng.model.Dentist;
import com.uor.eng.model.Role;
import com.uor.eng.payload.dentist.CreateDentistDTO;
import com.uor.eng.payload.dentist.DentistResponseDTO;
import com.uor.eng.payload.dentist.UpdateDentistRequest;
import com.uor.eng.repository.DentistRepository;
import com.uor.eng.repository.RoleRepository;
import com.uor.eng.repository.UserRepository;
import com.uor.eng.service.IDentistService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DentistServiceImpl implements IDentistService {

  @Autowired
  private DentistRepository dentistRepository;

  @Autowired
  private ModelMapper modelMapper;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private RoleRepository roleRepository;

  @Override
  public DentistResponseDTO createDentist(CreateDentistDTO createDentistDTO) {
    if (userRepository.existsByUserName(createDentistDTO.getUserName())) {
      throw new BadRequestException("Username is already taken!");
    }

    if (userRepository.existsByEmail(createDentistDTO.getEmail())) {
      throw new BadRequestException("Email is already in use!");
    }

    Dentist dentist = modelMapper.map(createDentistDTO, Dentist.class);
    dentist.setPassword(passwordEncoder.encode(createDentistDTO.getPassword()));

    Role role = roleRepository.findByRoleName(AppRole.ROLE_DENTIST)
        .orElseThrow(() -> new ResourceNotFoundException("Role ROLE_DENTIST not found."));
    dentist.setRoles(Collections.singleton(role));

    Dentist savedDentist = dentistRepository.save(dentist);
    DentistResponseDTO dentistResponseDTO = modelMapper.map(savedDentist, DentistResponseDTO.class);
    return getDentistResponseDTO(savedDentist, dentistResponseDTO);
  }

  @Override
  public List<DentistResponseDTO> getAllDentists() {
    if (dentistRepository.findAll().isEmpty()) {
      throw new ResourceNotFoundException("No dentists found.");
    }
    return dentistRepository.findAll().stream()
        .map(dentist ->
        {
          DentistResponseDTO dentistResponseDTO = modelMapper.map(dentist, DentistResponseDTO.class);
          return getDentistResponseDTO(dentist, dentistResponseDTO);
        })
        .collect(Collectors.toList());
  }

  @Override
  public DentistResponseDTO updateDentist(Long id, CreateDentistDTO updateDentistDTO) {
    Dentist existingDentist = dentistRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + id));

    if (!existingDentist.getUserName().equals(updateDentistDTO.getUserName()) &&
        userRepository.existsByUserName(updateDentistDTO.getUserName())) {
      throw new BadRequestException("Username is already taken!");
    }

    if (!existingDentist.getEmail().equals(updateDentistDTO.getEmail()) &&
        userRepository.existsByEmail(updateDentistDTO.getEmail())) {
      throw new BadRequestException("Email is already in use!");
    }

    existingDentist.setUserName(updateDentistDTO.getUserName());
    existingDentist.setEmail(updateDentistDTO.getEmail());
    existingDentist.setGender(updateDentistDTO.getGender());

    if (updateDentistDTO.getPassword() != null && !updateDentistDTO.getPassword().isEmpty()) {
      existingDentist.setPassword(passwordEncoder.encode(updateDentistDTO.getPassword()));
    }

    existingDentist.setFirstName(updateDentistDTO.getFirstName());
    existingDentist.setSpecialization(updateDentistDTO.getSpecialization());
    existingDentist.setLicenseNumber(updateDentistDTO.getLicenseNumber());

    Dentist updatedDentist = dentistRepository.save(existingDentist);
    DentistResponseDTO dentistResponseDTO = modelMapper.map(updatedDentist, DentistResponseDTO.class);
    return getDentistResponseDTO(updatedDentist, dentistResponseDTO);
  }

  @Override
  public DentistResponseDTO getDentistById(Long id) {
    Dentist dentist = dentistRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + id));
    DentistResponseDTO dentistResponseDTO = modelMapper.map(dentist, DentistResponseDTO.class);
    return getDentistResponseDTO(dentist, dentistResponseDTO);
  }

  @Override
  public void deleteDentist(Long id) {
    Dentist dentist = dentistRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + id + " to delete."));
    dentistRepository.delete(dentist);
  }

  @Override
  public DentistResponseDTO editDentist(Long id, UpdateDentistRequest updateDentistDTO) {
    Dentist existingDentist = dentistRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + id));

    if (!existingDentist.getUserName().equals(updateDentistDTO.getUserName()) &&
        userRepository.existsByUserName(updateDentistDTO.getUserName())) {
      throw new BadRequestException("Username is already taken!");
    }

    if (!existingDentist.getEmail().equals(updateDentistDTO.getEmail()) &&
        userRepository.existsByEmail(updateDentistDTO.getEmail())) {
      throw new BadRequestException("Email is already in use!");
    }

    existingDentist.setUserName(updateDentistDTO.getUserName());
    existingDentist.setEmail(updateDentistDTO.getEmail());
    existingDentist.setGender(updateDentistDTO.getGender());
    existingDentist.setFirstName(updateDentistDTO.getFirstName());
    existingDentist.setSpecialization(updateDentistDTO.getSpecialization());
    existingDentist.setLicenseNumber(updateDentistDTO.getLicenseNumber());

    Dentist updatedDentist = dentistRepository.save(existingDentist);
    DentistResponseDTO dentistResponseDTO = modelMapper.map(updatedDentist, DentistResponseDTO.class);
    return getDentistResponseDTO(updatedDentist, dentistResponseDTO);
  }

  private static DentistResponseDTO getDentistResponseDTO(Dentist dentist, DentistResponseDTO dto) {
    Set<String> roles = dentist.getRoles().stream()
        .map(roleEntity -> roleEntity.getRoleName().name())
        .collect(Collectors.toSet());
    dto.setRoles(roles);
    return dto;
  }
}
