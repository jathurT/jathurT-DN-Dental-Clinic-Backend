package com.uor.eng.service.impl;

import com.uor.eng.model.Contact;
import com.uor.eng.payload.other.ContactDTO;
import com.uor.eng.repository.ContactRepository;
import com.uor.eng.service.IContactService;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.util.EmailService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactServiceImpl implements IContactService {

  @Autowired
  private ContactRepository contactRepository;

  @Autowired
  private ModelMapper modelMapper;

  @Autowired
  private EmailService emailService;

  @Override
  @Transactional
  public ContactDTO saveContact(ContactDTO contactDTO) {
    if (contactDTO == null) {
      throw new BadRequestException("Contact data cannot be null.");
    }

    Contact contact = modelMapper.map(contactDTO, Contact.class);
    Contact savedContact = contactRepository.save(contact);
    return modelMapper.map(savedContact, ContactDTO.class);
  }

  @Override
  public List<ContactDTO> getAllContacts() {
    List<Contact> contacts = contactRepository.findAll();
    if (contacts.isEmpty()) {
      throw new ResourceNotFoundException("No contacts found. Please add contacts to view the list.");
    }
    return contacts.stream()
            .map(contact -> modelMapper.map(contact, ContactDTO.class))
            .collect(Collectors.toList());
  }

  @Override
  public ContactDTO getContactById(Long id) {
    Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contact with ID " + id + " not found. Please check the ID and try again."));
    return modelMapper.map(contact, ContactDTO.class);
  }

  @Override
  @Transactional
  public void deleteContact(Long id) {
    if (!contactRepository.existsById(id)) {
      throw new ResourceNotFoundException("Contact with ID " + id + " does not exist. Unable to delete.");
    }
    contactRepository.deleteById(id);
  }

  @Override
  public void sendReply(Long id, String reply) {
    Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contact with ID " + id + " not found. Please check the ID and try again."));
    ContactDTO contactDTO=modelMapper.map(contact, ContactDTO.class);
    emailService.sendResponseForContactUs(contactDTO,reply);
  }
}
