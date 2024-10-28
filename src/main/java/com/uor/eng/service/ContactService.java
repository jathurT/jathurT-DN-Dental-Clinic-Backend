package com.uor.eng.service;

import com.uor.eng.payload.ContactDTO;

import java.util.List;

public interface ContactService {
  ContactDTO saveContact(ContactDTO contactDTO);
  List<ContactDTO> getAllContacts();
  ContactDTO getContactById(Long id);
  void deleteContact(Long id);
}
