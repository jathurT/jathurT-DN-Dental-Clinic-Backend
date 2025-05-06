package com.uor.eng.service.impl;

import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.Contact;
import com.uor.eng.payload.other.ContactDTO;
import com.uor.eng.repository.ContactRepository;
import com.uor.eng.util.EmailService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
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
  private Contact repliedContact;
  private ContactDTO repliedContactDTO;

  @BeforeEach
  void setUp() {
    // Contact without reply
    contact = new Contact();
    contact.setId(1L);
    contact.setName("John Doe");
    contact.setEmail("john@example.com");
    contact.setContactNumber("1234567890");
    contact.setSubject("Test Subject");
    contact.setMessage("Test Message");
    contact.setReplySent(false);

    contactDTO = new ContactDTO();
    contactDTO.setId(1L);
    contactDTO.setName("John Doe");
    contactDTO.setEmail("john@example.com");
    contactDTO.setContactNumber("1234567890");
    contactDTO.setSubject("Test Subject");
    contactDTO.setMessage("Test Message");
    contactDTO.setReplySent(false);

    // Contact with reply
    repliedContact = new Contact();
    repliedContact.setId(2L);
    repliedContact.setName("Jane Doe");
    repliedContact.setEmail("jane@example.com");
    repliedContact.setContactNumber("0987654321");
    repliedContact.setSubject("Another Subject");
    repliedContact.setMessage("Another Message");
    repliedContact.setReplySent(true);

    repliedContactDTO = new ContactDTO();
    repliedContactDTO.setId(2L);
    repliedContactDTO.setName("Jane Doe");
    repliedContactDTO.setEmail("jane@example.com");
    repliedContactDTO.setContactNumber("0987654321");
    repliedContactDTO.setSubject("Another Subject");
    repliedContactDTO.setMessage("Another Message");
    repliedContactDTO.setReplySent(true);
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
    assertEquals(false, result.getReplySent());
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
    List<Contact> contacts = Arrays.asList(contact, repliedContact);
    when(contactRepository.findAll()).thenReturn(contacts);
    when(modelMapper.map(contact, ContactDTO.class)).thenReturn(contactDTO);
    when(modelMapper.map(repliedContact, ContactDTO.class)).thenReturn(repliedContactDTO);

    // Act
    List<ContactDTO> result = contactService.getAllContacts();

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("John Doe", result.get(0).getName());
    assertEquals(false, result.get(0).getReplySent());
    assertEquals("Jane Doe", result.get(1).getName());
    assertEquals(true, result.get(1).getReplySent());
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
    assertEquals(false, result.getReplySent());
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

    ArgumentCaptor<Contact> contactCaptor = ArgumentCaptor.forClass(Contact.class);

    // Act
    contactService.sendReply(1L, "Thank you for your message.");

    // Assert
    verify(contactRepository).save(contactCaptor.capture());
    Contact savedContact = contactCaptor.getValue();
    assertTrue(savedContact.getReplySent());
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
    verify(contactRepository, never()).save(any());
  }

  @Test
  @DisplayName("Get contacts with reply sent - Success")
  @Order(11)
  void getContactsWithReplySent_Success() {
    // Arrange
    List<Contact> repliedContacts = Collections.singletonList(repliedContact);
    when(contactRepository.findByReplySent(true)).thenReturn(repliedContacts);
    when(modelMapper.map(repliedContact, ContactDTO.class)).thenReturn(repliedContactDTO);

    // Act
    List<ContactDTO> result = contactService.getContactsWithReplySent();

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Jane Doe", result.get(0).getName());
    assertEquals(true, result.get(0).getReplySent());
    verify(contactRepository).findByReplySent(true);
  }

  @Test
  @DisplayName("Get contacts with reply sent - Empty")
  @Order(12)
  void getContactsWithReplySent_Empty() {
    // Arrange
    when(contactRepository.findByReplySent(true)).thenReturn(Collections.emptyList());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> contactService.getContactsWithReplySent());
    assertEquals("No contacts with replies found.", exception.getMessage());
    verify(contactRepository).findByReplySent(true);
  }

  @Test
  @DisplayName("Get contacts with no reply sent - Success")
  @Order(13)
  void getContactsWithNoReplySent_Success() {
    // Arrange
    List<Contact> unrepliedContacts = Collections.singletonList(contact);
    when(contactRepository.findByReplySent(false)).thenReturn(unrepliedContacts);
    when(modelMapper.map(contact, ContactDTO.class)).thenReturn(contactDTO);

    // Act
    List<ContactDTO> result = contactService.getContactsWithNoReplySent();

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("John Doe", result.get(0).getName());
    assertEquals(false, result.get(0).getReplySent());
    verify(contactRepository).findByReplySent(false);
  }

  @Test
  @DisplayName("Get contacts with no reply sent - Empty")
  @Order(14)
  void getContactsWithNoReplySent_Empty() {
    // Arrange
    when(contactRepository.findByReplySent(false)).thenReturn(Collections.emptyList());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> contactService.getContactsWithNoReplySent());
    assertEquals("No contacts without replies found.", exception.getMessage());
    verify(contactRepository).findByReplySent(false);
  }
}