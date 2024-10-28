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
  public ResponseEntity<?> submitContact(@RequestBody ContactDTO contactDTO) {
    try {
      ContactDTO savedContact = contactService.saveContact(contactDTO);
      return new ResponseEntity<>(savedContact, HttpStatus.CREATED);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @GetMapping("/all")
  public ResponseEntity<?> getAllContacts() {
    try {
      List<ContactDTO> contacts = contactService.getAllContacts();
      return new ResponseEntity<>(contacts, HttpStatus.OK);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NO_CONTENT);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getContactById(@PathVariable Long id) {
    try {
      ContactDTO contact = contactService.getContactById(id);
      return new ResponseEntity<>(contact, HttpStatus.OK);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteContact(@PathVariable Long id) {
    try {
      contactService.deleteContact(id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
  }
}
