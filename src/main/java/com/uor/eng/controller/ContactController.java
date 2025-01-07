package com.uor.eng.controller;

import com.uor.eng.payload.other.ContactDTO;
import com.uor.eng.service.IContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

  @Autowired
  private IContactService contactService;

  @PostMapping("/submit")
  public ResponseEntity<ContactDTO> submitContact(@RequestBody ContactDTO contactDTO) {
    ContactDTO savedContact = contactService.saveContact(contactDTO);
    return new ResponseEntity<>(savedContact, HttpStatus.CREATED);
  }

  @GetMapping("/all")
  public ResponseEntity<List<ContactDTO>> getAllContacts() {
    List<ContactDTO> contacts = contactService.getAllContacts();
    return new ResponseEntity<>(contacts, HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ContactDTO> getContactById(@PathVariable Long id) {
    ContactDTO contact = contactService.getContactById(id);
    return new ResponseEntity<>(contact, HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
    contactService.deleteContact(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
