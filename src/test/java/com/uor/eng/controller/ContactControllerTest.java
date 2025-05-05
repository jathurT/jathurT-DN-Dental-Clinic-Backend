package com.uor.eng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.payload.other.ContactDTO;
import com.uor.eng.service.IContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ContactControllerTest {

  private MockMvc mockMvc;

  @Mock
  private IContactService contactService;

  @InjectMocks
  private ContactController contactController;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private ContactDTO contactDTO;
  private List<ContactDTO> contactList;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    // Set up MockMvc with TestExceptionHandler
    mockMvc = MockMvcBuilders.standaloneSetup(contactController)
            .setControllerAdvice(new TestExceptionHandler())
            .build();

    // Set up test data
    contactDTO = new ContactDTO();
    contactDTO.setId(1L);
    contactDTO.setName("John Doe");
    contactDTO.setEmail("john.doe@example.com");
    contactDTO.setContactNumber("1234567890");
    contactDTO.setSubject("Test Subject");
    contactDTO.setMessage("This is a test message");

    ContactDTO contactDTO2 = new ContactDTO();
    contactDTO2.setId(2L);
    contactDTO2.setName("Jane Smith");
    contactDTO2.setEmail("jane.smith@example.com");
    contactDTO2.setContactNumber("0987654321");
    contactDTO2.setSubject("Another Subject");
    contactDTO2.setMessage("This is another test message");

    contactList = Arrays.asList(contactDTO, contactDTO2);
  }

  @Test
  public void testSubmitContact_Success() throws Exception {
    // Arrange
    when(contactService.saveContact(any(ContactDTO.class))).thenReturn(contactDTO);

    // Act & Assert
    mockMvc.perform(post("/api/contacts/submit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(contactDTO)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("John Doe")))
            .andExpect(jsonPath("$.email", is("john.doe@example.com")))
            .andExpect(jsonPath("$.contactNumber", is("1234567890")))
            .andExpect(jsonPath("$.subject", is("Test Subject")))
            .andExpect(jsonPath("$.message", is("This is a test message")));

    verify(contactService, times(1)).saveContact(any(ContactDTO.class));
  }

  @Test
  public void testSubmitContact_BadRequest() throws Exception {
    // Arrange
    ContactDTO invalidContact = new ContactDTO();
    // ID is populated but other fields are left empty
    invalidContact.setId(1L);

    // Mock the service to throw a BadRequestException for invalid input
    when(contactService.saveContact(any(ContactDTO.class)))
            .thenThrow(new BadRequestException("Contact data cannot be null or empty"));

    // Act & Assert
    mockMvc.perform(post("/api/contacts/submit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidContact)))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.details.error", is("Contact data cannot be null or empty")));

    verify(contactService, times(1)).saveContact(any(ContactDTO.class));
  }

  @Test
  public void testGetAllContacts_Success() throws Exception {
    // Arrange
    when(contactService.getAllContacts()).thenReturn(contactList);

    // Act & Assert
    mockMvc.perform(get("/api/contacts/all"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].name", is("John Doe")))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].name", is("Jane Smith")));

    verify(contactService, times(1)).getAllContacts();
  }

  @Test
  public void testGetAllContacts_EmptyList() throws Exception {
    // Arrange
    when(contactService.getAllContacts()).thenReturn(List.of());

    // Act & Assert
    mockMvc.perform(get("/api/contacts/all"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));

    verify(contactService, times(1)).getAllContacts();
  }

  @Test
  public void testGetAllContacts_NotFound() throws Exception {
    // Arrange
    when(contactService.getAllContacts())
            .thenThrow(new ResourceNotFoundException("No contacts found"));

    // Act & Assert
    mockMvc.perform(get("/api/contacts/all"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.details.error", is("No contacts found")));

    verify(contactService, times(1)).getAllContacts();
  }

  @Test
  public void testGetContactById_Success() throws Exception {
    // Arrange
    when(contactService.getContactById(1L)).thenReturn(contactDTO);

    // Act & Assert
    mockMvc.perform(get("/api/contacts/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("John Doe")))
            .andExpect(jsonPath("$.email", is("john.doe@example.com")));

    verify(contactService, times(1)).getContactById(1L);
  }

  @Test
  public void testGetContactById_NotFound() throws Exception {
    // Arrange
    when(contactService.getContactById(999L))
            .thenThrow(new ResourceNotFoundException("Contact with ID 999 not found"));

    // Act & Assert
    mockMvc.perform(get("/api/contacts/999"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.details.error", is("Contact with ID 999 not found")));

    verify(contactService, times(1)).getContactById(999L);
  }

  @Test
  public void testDeleteContact_Success() throws Exception {
    // Arrange
    doNothing().when(contactService).deleteContact(1L);

    // Act & Assert
    mockMvc.perform(delete("/api/contacts/1"))
            .andDo(print())
            .andExpect(status().isNoContent());

    verify(contactService, times(1)).deleteContact(1L);
  }

  @Test
  public void testDeleteContact_NotFound() throws Exception {
    // Arrange
    doThrow(new ResourceNotFoundException("Contact with ID 999 not found"))
            .when(contactService).deleteContact(999L);

    // Act & Assert
    mockMvc.perform(delete("/api/contacts/999"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.details.error", is("Contact with ID 999 not found")));

    verify(contactService, times(1)).deleteContact(999L);
  }

  @Test
  public void testSendReply_Success() throws Exception {
    // Arrange
    String reply = "Thank you for your message. We will get back to you soon.";
    doNothing().when(contactService).sendReply(1L, reply);

    // Act & Assert
    mockMvc.perform(put("/api/contacts/sendReply/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reply))
            .andDo(print())
            .andExpect(status().isOk());

    verify(contactService, times(1)).sendReply(1L, reply);
  }

  @Test
  public void testSendReply_NotFound() throws Exception {
    // Arrange
    String reply = "Thank you for your message.";
    doThrow(new ResourceNotFoundException("Contact with ID 999 not found"))
            .when(contactService).sendReply(999L, reply);

    // Act & Assert
    mockMvc.perform(put("/api/contacts/sendReply/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reply))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.details.error", is("Contact with ID 999 not found")));

    verify(contactService, times(1)).sendReply(999L, reply);
  }

  @Test
  public void testSendReply_EmptyReply() throws Exception {
    // Arrange
    String emptyReply = "";

    // Act & Assert
    mockMvc.perform(put("/api/contacts/sendReply/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(emptyReply))
            .andDo(print())
            .andExpect(status().isBadRequest());

    // Since we expect a bad request before the service is called
    verify(contactService, never()).sendReply(anyLong(), anyString());
  }
}