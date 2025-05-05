package com.uor.eng.service.impl;

import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.AppRole;
import com.uor.eng.model.Receptionist;
import com.uor.eng.model.Role;
import com.uor.eng.payload.receiptionist.CreateReceptionistDTO;
import com.uor.eng.payload.receiptionist.ReceptionistResponseDTO;
import com.uor.eng.payload.receiptionist.UpdateReceptionistRequest;
import com.uor.eng.repository.ReceptionistRepository;
import com.uor.eng.repository.RoleRepository;
import com.uor.eng.repository.UserRepository;
import com.uor.eng.service.IReceptionistService;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReceptionistServiceImpl implements IReceptionistService {

  private final UserRepository userRepository;
  private final ReceptionistRepository receptionistRepository;
  private final ModelMapper modelMapper;
  private final PasswordEncoder passwordEncoder;
  private final RoleRepository roleRepository;

  public ReceptionistServiceImpl(UserRepository userRepository,
                                 ReceptionistRepository receptionistRepository,
                                 ModelMapper modelMapper,
                                 PasswordEncoder passwordEncoder,
                                 RoleRepository roleRepository) {
    this.userRepository = userRepository;
    this.receptionistRepository = receptionistRepository;
    this.modelMapper = modelMapper;
    this.passwordEncoder = passwordEncoder;
    this.roleRepository = roleRepository;
  }

  @Override
  @Transactional
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

    List<Receptionist> receptionists = receptionistRepository.findAll();
    if (receptionists.isEmpty()) {
      throw new ResourceNotFoundException("No receptionists found.");
    }
    return receptionists.stream()
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
  @Transactional
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
  @Transactional
  public void deleteReceptionist(Long id) {
    Receptionist receptionist = receptionistRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Receptionist not found with id: " + id + " to delete."));
    receptionistRepository.delete(receptionist);
  }

  @Override
  public ReceptionistResponseDTO editReceptionist(Long id, UpdateReceptionistRequest receptionistDTO) {
    Receptionist existingReceptionist = receptionistRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Receptionist not found with id: " + id));

    if (!existingReceptionist.getUserName().equals(receptionistDTO.getUserName()) &&
            userRepository.existsByUserName(receptionistDTO.getUserName())) {
      throw new BadRequestException("Username is already taken!");
    }

    if (!existingReceptionist.getEmail().equals(receptionistDTO.getEmail()) &&
            userRepository.existsByEmail(receptionistDTO.getEmail())) {
      throw new BadRequestException("Email is already in use!");
    }

    existingReceptionist.setUserName(receptionistDTO.getUserName());
    existingReceptionist.setEmail(receptionistDTO.getEmail());
    existingReceptionist.setGender(receptionistDTO.getGender());
    existingReceptionist.setFirstName(receptionistDTO.getFirstName());
    existingReceptionist.setNic(receptionistDTO.getNic());
    existingReceptionist.setPhoneNumber(receptionistDTO.getPhoneNumber());

    Receptionist updatedReceptionist = receptionistRepository.save(existingReceptionist);
    ReceptionistResponseDTO dto = modelMapper.map(updatedReceptionist, ReceptionistResponseDTO.class);
    return getReceptionistResponseDTO(updatedReceptionist, dto);
  }

  private static ReceptionistResponseDTO getReceptionistResponseDTO(Receptionist receptionist, ReceptionistResponseDTO dto) {
    Set<String> roles = receptionist.getRoles().stream()
            .map(roleEntity -> roleEntity.getRoleName().name())
            .collect(Collectors.toSet());
    dto.setRoles(roles);
    return dto;
  }

}
