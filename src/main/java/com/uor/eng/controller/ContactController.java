package com.uor.eng.controller;

import com.uor.eng.payload.ContactDTO;
import com.uor.eng.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

  @Autowired
  private ContactService contactService;

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
    if (contact != null) {
      return new ResponseEntity<>(contact, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
    contactService.deleteContact(id);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
