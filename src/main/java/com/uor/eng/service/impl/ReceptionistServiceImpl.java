package com.uor.eng.service.impl;

import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.AppRole;
import com.uor.eng.model.Dentist;
import com.uor.eng.model.Receptionist;
import com.uor.eng.model.Role;
import com.uor.eng.payload.CreateReceptionistDTO;
import com.uor.eng.payload.ReceptionistResponseDTO;
import com.uor.eng.repository.ReceptionistRepository;
import com.uor.eng.repository.RoleRepository;
import com.uor.eng.repository.UserRepository;
import com.uor.eng.service.IReceptionistService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReceptionistServiceImpl implements IReceptionistService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ReceptionistRepository receptionistRepository;

  @Autowired
  private ModelMapper modelMapper;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private RoleRepository roleRepository;

  @Override
  public ReceptionistResponseDTO createReceptionist(CreateReceptionistDTO createReceptionistDTO) {
    if (userRepository.existsByUserName(createReceptionistDTO.getUserName())) {
      throw new BadRequestException("Username is already taken!");
    }

    if (userRepository.existsByEmail(createReceptionistDTO.getEmail())) {
      throw new BadRequestException("Email is already in use!");
    }

    Receptionist receptionist = modelMapper.map(createReceptionistDTO, Receptionist.class);
    receptionist.setPassword(passwordEncoder.encode(createReceptionistDTO.getPassword()));

    Role role = roleRepository.findByRoleName(AppRole.ROLE_RECEPTIONIST)
        .orElseThrow(() -> new ResourceNotFoundException("Role ROLE_RECEPTIONIST not found."));
    receptionist.setRoles(Collections.singleton(role));

    Receptionist savedReceptionist = receptionistRepository.save(receptionist);

    ReceptionistResponseDTO receptionistResponseDTO = modelMapper.map(savedReceptionist, ReceptionistResponseDTO.class);
    return getReceptionistResponseDTO(savedReceptionist, receptionistResponseDTO);
  }

  @Override
  public List<ReceptionistResponseDTO> getAllReceptionists() {
    if (receptionistRepository.findAll().isEmpty()) {
      throw new ResourceNotFoundException("No receptionists found.");
    }
    return receptionistRepository.findAll().stream()
        .map(receptionist -> {
          ReceptionistResponseDTO dto = modelMapper.map(receptionist, ReceptionistResponseDTO.class);
          return getReceptionistResponseDTO(receptionist, dto);
        }).collect(Collectors.toList());
  }


  @Override
  public ReceptionistResponseDTO getReceptionistById(Long id) {
    Receptionist receptionist = receptionistRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + id));
    ReceptionistResponseDTO dto = modelMapper.map(receptionist, ReceptionistResponseDTO.class);
    return getReceptionistResponseDTO(receptionist, dto);
  }

  @Override
  public ReceptionistResponseDTO updateReceptionist(Long id, CreateReceptionistDTO updateReceptionistDTO) {
    Receptionist existingReceptionist = receptionistRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Receptionist not found with id: " + id));

    if (!existingReceptionist.getUserName().equals(updateReceptionistDTO.getUserName()) &&
        userRepository.existsByUserName(updateReceptionistDTO.getUserName())) {
      throw new BadRequestException("Username is already taken!");
    }

    if (!existingReceptionist.getEmail().equals(updateReceptionistDTO.getEmail()) &&
        userRepository.existsByEmail(updateReceptionistDTO.getEmail())) {
      throw new BadRequestException("Email is already in use!");
    }

    existingReceptionist.setUserName(updateReceptionistDTO.getUserName());
    existingReceptionist.setEmail(updateReceptionistDTO.getEmail());
    existingReceptionist.setGender(updateReceptionistDTO.getGender());

    if (updateReceptionistDTO.getPassword() != null && !updateReceptionistDTO.getPassword().isEmpty()) {
      existingReceptionist.setPassword(passwordEncoder.encode(updateReceptionistDTO.getPassword()));
    }

    existingReceptionist.setFirstName(updateReceptionistDTO.getFirstName());

    Receptionist updatedReceptionist = receptionistRepository.save(existingReceptionist);
    ReceptionistResponseDTO dto = modelMapper.map(updatedReceptionist, ReceptionistResponseDTO.class);
    return getReceptionistResponseDTO(updatedReceptionist, dto);
  }

  @Override
  public void deleteReceptionist(Long id) {
    Receptionist receptionist = receptionistRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Receptionist not found with id: " + id + " to delete."));
    receptionistRepository.delete(receptionist);
  }

  private static ReceptionistResponseDTO getReceptionistResponseDTO(Receptionist receptionist, ReceptionistResponseDTO dto) {
    Set<String> roles = receptionist.getRoles().stream()
        .map(roleEntity -> roleEntity.getRoleName().name())
        .collect(Collectors.toSet());
    dto.setRoles(roles);
    return dto;
  }

}
