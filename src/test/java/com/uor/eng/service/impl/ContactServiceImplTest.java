package com.uor.eng.service.impl;

import com.uor.eng.model.Contact;
import com.uor.eng.payload.other.ContactDTO;
import com.uor.eng.repository.ContactRepository;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.util.EmailService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class ContactServiceImplTest {

  @InjectMocks
  private ContactServiceImpl contactService;

  @Mock
  private ContactRepository contactRepository;

  @Mock
  private ModelMapper modelMapper;

  @Mock
  private EmailService emailService;

  private Contact contact;
  private ContactDTO contactDTO;

  @BeforeEach
  void setUp() {
    contact = new Contact(1L, "John Doe", "john.doe@example.com", "1234567890", "Subject", "Message");
    contactDTO = new ContactDTO(1L, "John Doe", "john.doe@example.com", "1234567890", "Subject", "Message");
  }

  @AfterEach
  void tearDown() {
    reset(contactRepository, modelMapper);
  }

  @Test
  @DisplayName("Test save contact - Success")
  @Order(1)
  void saveContact_shouldSaveAndReturnContactDTO() {
    when(modelMapper.map(contactDTO, Contact.class)).thenReturn(contact);
    when(contactRepository.save(contact)).thenReturn(contact);
    when(modelMapper.map(contact, ContactDTO.class)).thenReturn(contactDTO);

    ContactDTO savedContactDTO = contactService.saveContact(contactDTO);

    assertNotNull(savedContactDTO);
    assertEquals(contactDTO.getId(), savedContactDTO.getId());
    verify(contactRepository, times(1)).save(contact);
  }

  @Test
  @DisplayName("Test save contact - Null")
  @Order(2)
  void saveContact_shouldThrowBadRequestExceptionWhenNull() {
    assertThrows(BadRequestException.class, () -> contactService.saveContact(null));
    verify(contactRepository, never()).save(any());
  }

  @Test
  @DisplayName("Test get all contacts - Success")
  @Order(3)
  void getAllContacts_shouldReturnListOfContacts() {
    List<Contact> contacts = List.of(contact);
    when(contactRepository.findAll()).thenReturn(contacts);
    when(modelMapper.map(contact, ContactDTO.class)).thenReturn(contactDTO);

    List<ContactDTO> contactDTOList = contactService.getAllContacts();

    assertNotNull(contactDTOList);
    assertEquals(1, contactDTOList.size());
    assertEquals(contactDTO.getName(), contactDTOList.get(0).getName());
    verify(contactRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Test get all contacts - Empty")
  @Order(4)
  void getAllContacts_shouldThrowResourceNotFoundExceptionWhenEmpty() {
    when(contactRepository.findAll()).thenReturn(new ArrayList<>());

    assertThrows(ResourceNotFoundException.class, () -> contactService.getAllContacts());
    verify(contactRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Test get contact by ID - Success")
  @Order(5)
  void getContactById_shouldReturnContactDTO() {
    when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));
    when(modelMapper.map(contact, ContactDTO.class)).thenReturn(contactDTO);

    ContactDTO foundContact = contactService.getContactById(1L);

    assertNotNull(foundContact);
    assertEquals(contactDTO.getId(), foundContact.getId());
    verify(contactRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Test get contact by ID - Not Found")
  @Order(6)
  void getContactById_shouldThrowResourceNotFoundExceptionWhenNotFound() {
    when(contactRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> contactService.getContactById(1L));
    verify(contactRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Test delete contact - Success")
  @Order(7)
  void deleteContact_shouldDeleteContact() {
    when(contactRepository.existsById(1L)).thenReturn(true);

    contactService.deleteContact(1L);

    verify(contactRepository, times(1)).deleteById(1L);
  }

  @Test
  @DisplayName("Test delete contact - Not Found")
  @Order(8)
  void deleteContact_shouldThrowResourceNotFoundExceptionWhenNotFound() {
    when(contactRepository.existsById(1L)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> contactService.deleteContact(1L));
    verify(contactRepository, times(1)).existsById(1L);
  }

  @Test
  @DisplayName("Test send reply - Success")
  @Order(9)
  void sendReply_shouldSendEmailSuccessfully() {
    Long contactId = 1L;
    String replyMessage = "Thank you for reaching out!";
    when(contactRepository.findById(contactId)).thenReturn(Optional.of(contact));
    when(modelMapper.map(contact, ContactDTO.class)).thenReturn(contactDTO);

    contactService.sendReply(contactId, replyMessage);

    verify(contactRepository, times(1)).findById(contactId);
    verify(modelMapper, times(1)).map(contact, ContactDTO.class);
    verify(emailService, times(1)).sendResponseForContactUs(contactDTO, replyMessage);
  }

  @Test
  @DisplayName("Test send reply - Contact Not Found")
  @Order(10)
  void sendReply_shouldThrowResourceNotFoundExceptionWhenContactNotFound() {
    Long contactId = 1L;
    String replyMessage = "Thank you for reaching out!";
    when(contactRepository.findById(contactId)).thenReturn(Optional.empty());

    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
        () -> contactService.sendReply(contactId, replyMessage));

    assertEquals("Contact with ID 1 not found. Please check the ID and try again.", exception.getMessage());
    verify(contactRepository, times(1)).findById(contactId);
    verify(emailService, never()).sendResponseForContactUs(any(), any());
  }

}
