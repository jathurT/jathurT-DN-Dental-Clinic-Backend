package com.uor.eng.service;

import com.uor.eng.payload.ContactDTO;

import java.util.List;

public interface IContactService {
  ContactDTO saveContact(ContactDTO contactDTO);

  List<ContactDTO> getAllContacts();

  ContactDTO getContactById(Long id);

  void deleteContact(Long id);
}
