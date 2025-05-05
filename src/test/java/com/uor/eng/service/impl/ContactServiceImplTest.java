package com.uor.eng.service.impl;

import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.Contact;
import com.uor.eng.payload.other.ContactDTO;
import com.uor.eng.repository.ContactRepository;
import com.uor.eng.util.EmailService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ContactServiceImplTest {

  @Mock
  private ContactRepository contactRepository;

  @Mock
  private ModelMapper modelMapper;

  @Mock
  private EmailService emailService;

  @InjectMocks
  private ContactServiceImpl contactService;

  private Contact contact;
  private ContactDTO contactDTO;

  @BeforeEach
  void setUp() {
    contact = new Contact(1L, "John Doe", "john@example.com", "1234567890", "Test Subject", "Test Message");
    contactDTO = new ContactDTO(1L, "John Doe", "john@example.com", "1234567890", "Test Subject", "Test Message");
  }

  @Test
  @DisplayName("Save contact - Success")
  @Order(1)
  void saveContact_Success() {
    // Arrange
    when(modelMapper.map(contactDTO, Contact.class)).thenReturn(contact);
    when(contactRepository.save(contact)).thenReturn(contact);
    when(modelMapper.map(contact, ContactDTO.class)).thenReturn(contactDTO);

    // Act
    ContactDTO result = contactService.saveContact(contactDTO);

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("John Doe", result.getName());
    verify(contactRepository).save(contact);
  }

  @Test
  @DisplayName("Save contact - Null input")
  @Order(2)
  void saveContact_NullInput() {
    // Act & Assert
    BadRequestException exception = assertThrows(BadRequestException.class,
            () -> contactService.saveContact(null));
    assertEquals("Contact data cannot be null.", exception.getMessage());
    verify(contactRepository, never()).save(any());
  }

  @Test
  @DisplayName("Get all contacts - Success")
  @Order(3)
  void getAllContacts_Success() {
    // Arrange
    List<Contact> contacts = Collections.singletonList(contact);
    when(contactRepository.findAll()).thenReturn(contacts);
    when(modelMapper.map(contact, ContactDTO.class)).thenReturn(contactDTO);

    // Act
    List<ContactDTO> result = contactService.getAllContacts();

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("John Doe", result.get(0).getName());
  }

  @Test
  @DisplayName("Get all contacts - Empty")
  @Order(4)
  void getAllContacts_Empty() {
    // Arrange
    when(contactRepository.findAll()).thenReturn(Collections.emptyList());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> contactService.getAllContacts());
    assertTrue(exception.getMessage().contains("No contacts found"));
  }

  @Test
  @DisplayName("Get contact by ID - Success")
  @Order(5)
  void getContactById_Success() {
    // Arrange
    when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));
    when(modelMapper.map(contact, ContactDTO.class)).thenReturn(contactDTO);

    // Act
    ContactDTO result = contactService.getContactById(1L);

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("John Doe", result.getName());
  }

  @Test
  @DisplayName("Get contact by ID - Not found")
  @Order(6)
  void getContactById_NotFound() {
    // Arrange
    when(contactRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> contactService.getContactById(1L));
    assertTrue(exception.getMessage().contains("Contact with ID 1 not found"));
  }

  @Test
  @DisplayName("Delete contact - Success")
  @Order(7)
  void deleteContact_Success() {
    // Arrange
    when(contactRepository.existsById(1L)).thenReturn(true);

    // Act
    contactService.deleteContact(1L);

    // Assert
    verify(contactRepository).deleteById(1L);
  }

  @Test
  @DisplayName("Delete contact - Not found")
  @Order(8)
  void deleteContact_NotFound() {
    // Arrange
    when(contactRepository.existsById(1L)).thenReturn(false);

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> contactService.deleteContact(1L));
    assertTrue(exception.getMessage().contains("does not exist"));
  }

  @Test
  @DisplayName("Send reply - Success")
  @Order(9)
  void sendReply_Success() {
    // Arrange
    when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));
    when(modelMapper.map(contact, ContactDTO.class)).thenReturn(contactDTO);

    // Act
    contactService.sendReply(1L, "Thank you for your message.");

    // Assert
    verify(emailService).sendResponseForContactUs(contactDTO, "Thank you for your message.");
  }

  @Test
  @DisplayName("Send reply - Contact not found")
  @Order(10)
  void sendReply_ContactNotFound() {
    // Arrange
    when(contactRepository.findById(1L)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> contactService.sendReply(1L, "Thank you"));
    assertTrue(exception.getMessage().contains("Contact with ID 1 not found"));
    verify(emailService, never()).sendResponseForContactUs(any(), any());
  }
}