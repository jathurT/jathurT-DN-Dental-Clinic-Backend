package com.uor.eng.service;

import com.uor.eng.model.Contact;
import com.uor.eng.payload.ContactDTO;
import com.uor.eng.repository.ContactRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactServiceImpl implements ContactService {

  @Autowired
  private ContactRepository contactRepository;

  @Autowired
  private ModelMapper modelMapper;

  @Override
  public ContactDTO saveContact(ContactDTO contactDTO) {
    Contact contact = modelMapper.map(contactDTO, Contact.class);
    Contact savedContact = contactRepository.save(contact);
    return modelMapper.map(savedContact, ContactDTO.class);
  }

  @Override
  public List<ContactDTO> getAllContacts() {
    List<Contact> contacts = contactRepository.findAll();
    if (contacts.isEmpty()) {
      throw new RuntimeException("No contacts found. Please add contacts to view the list.");
    }
    return contacts.stream()
            .map(contact -> modelMapper.map(contact, ContactDTO.class))
            .collect(Collectors.toList());
  }

  @Override
  public ContactDTO getContactById(Long id) {
    Contact contact = contactRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Contact with ID " + id + " not found. Please check the ID and try again."));
    return modelMapper.map(contact, ContactDTO.class);
  }

  @Override
  public void deleteContact(Long id) {
    if (contactRepository.existsById(id)) {
      contactRepository.deleteById(id);
    } else {
      throw new RuntimeException("Contact with ID " + id + " does not exist. Unable to delete.");
    }
  }
}
