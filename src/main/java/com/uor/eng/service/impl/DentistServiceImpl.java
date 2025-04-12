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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DentistServiceImpl implements IDentistService {

  private final DentistRepository dentistRepository;
  private final ModelMapper modelMapper;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final RoleRepository roleRepository;

  public DentistServiceImpl(DentistRepository dentistRepository,
                            ModelMapper modelMapper,
                            UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            RoleRepository roleRepository) {
    this.dentistRepository = dentistRepository;
    this.modelMapper = modelMapper;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.roleRepository = roleRepository;
  }

  @Override
  @Transactional
  public DentistResponseDTO createDentist(CreateDentistDTO createDentistDTO) {
    validateUniqueUsernameAndEmail(createDentistDTO.getUserName(), createDentistDTO.getEmail());

    Dentist dentist = modelMapper.map(createDentistDTO, Dentist.class);
    dentist.setPassword(passwordEncoder.encode(createDentistDTO.getPassword()));
    dentist.setRoles(Collections.singleton(getRole()));

    Dentist savedDentist = dentistRepository.save(dentist);
    return mapToDentistResponseDTO(savedDentist);
  }

  @Override
  public List<DentistResponseDTO> getAllDentists() {
    List<Dentist> dentists = dentistRepository.findAll();
    if (dentists.isEmpty()) {
      throw new ResourceNotFoundException("No dentists found.");
    }
    return dentists.stream()
            .map(this::mapToDentistResponseDTO)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public DentistResponseDTO updateDentist(Long id, CreateDentistDTO updateDentistDTO) {
    Dentist existingDentist = findDentistById(id);

    validateUniqueUsernameAndEmailForUpdate(existingDentist, updateDentistDTO.getUserName(), updateDentistDTO.getEmail());

    Dentist updatedDentist = updateDentistDetailsByAdmin(existingDentist, updateDentistDTO);
    updatedDentist = dentistRepository.save(updatedDentist);
    return mapToDentistResponseDTO(updatedDentist);
  }

  @Override
  public DentistResponseDTO getDentistById(Long id) {
    Dentist dentist = findDentistById(id);
    return mapToDentistResponseDTO(dentist);
  }

  @Override
  public void deleteDentist(Long id) {
    Dentist dentist = findDentistById(id);
    dentistRepository.delete(dentist);
  }

  @Override
  @Transactional
  public DentistResponseDTO editDentist(Long id, UpdateDentistRequest updateDentistDTO) {
    Dentist existingDentist = findDentistById(id);

    validateUniqueUsernameAndEmailForUpdate(existingDentist, updateDentistDTO.getUserName(), updateDentistDTO.getEmail());

    updateDentistDetailsByDoctor(existingDentist, updateDentistDTO);
    Dentist updatedDentist = dentistRepository.save(existingDentist);

    return mapToDentistResponseDTO(updatedDentist);
  }

  private Dentist findDentistById(Long id) {
    return dentistRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id: " + id));
  }

  private void validateUniqueUsernameAndEmail(String username, String email) {
    if (userRepository.existsByUserName(username)) {
      throw new BadRequestException("Username is already taken!");
    }
    if (userRepository.existsByEmail(email)) {
      throw new BadRequestException("Email is already in use!");
    }
  }

  private void validateUniqueUsernameAndEmailForUpdate(Dentist existingDentist, String newUsername, String newEmail) {
    if (!existingDentist.getUserName().equals(newUsername) && userRepository.existsByUserName(newUsername)) {
      throw new BadRequestException("Username is already taken!");
    }
    if (!existingDentist.getEmail().equals(newEmail) && userRepository.existsByEmail(newEmail)) {
      throw new BadRequestException("Email is already in use!");
    }
  }

  private Dentist updateDentistDetailsByAdmin(Dentist dentist, CreateDentistDTO dto) {
    dentist.setUserName(dto.getUserName());
    dentist.setEmail(dto.getEmail());
    dentist.setGender(dto.getGender());
    dentist.setFirstName(dto.getFirstName());
    dentist.setSpecialization(dto.getSpecialization());
    dentist.setLicenseNumber(dto.getLicenseNumber());
    dentist.setPhoneNumber(dto.getPhoneNumber());
    dentist.setNic(dto.getNic());
    dentist.setPassword(passwordEncoder.encode(dto.getPassword()));
    return dentist;
  }

  private void updateDentistDetailsByDoctor(Dentist dentist, UpdateDentistRequest dto) {
    dentist.setUserName(dto.getUserName());
    dentist.setEmail(dto.getEmail());
    dentist.setGender(dto.getGender());
    dentist.setFirstName(dto.getFirstName());
    dentist.setSpecialization(dto.getSpecialization());
    dentist.setLicenseNumber(dto.getLicenseNumber());
  }

  private Role getRole() {
    return roleRepository.findByRoleName(AppRole.ROLE_DENTIST)
            .orElseThrow(() -> new ResourceNotFoundException("Role " + AppRole.ROLE_DENTIST + " not found."));
  }

  private DentistResponseDTO mapToDentistResponseDTO(Dentist dentist) {
    DentistResponseDTO dto = modelMapper.map(dentist, DentistResponseDTO.class);
    dto.setRoles(dentist.getRoles().stream()
            .map(role -> role.getRoleName().name())
            .collect(Collectors.toSet()));
    return dto;
  }
}
