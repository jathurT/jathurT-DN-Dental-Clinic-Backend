//package com.uor.eng.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.uor.eng.model.Feedback;
//import com.uor.eng.payload.other.FeedbackDTO;
//import com.uor.eng.service.IFeedbackService;
//import jakarta.validation.ConstraintViolation;
//import jakarta.validation.Validation;
//import jakarta.validation.ValidatorFactory;
//import jakarta.validation.Validator;
//import org.junit.jupiter.api.*;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(FeedbackController.class)
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Ensure test methods are run in order
//public class FeedbackControllerTest {
//
//  @Autowired
//  private MockMvc mockMvc;
//
//  @Mock
//  private IFeedbackService feedbackService;
//
//  @InjectMocks
//  private FeedbackController feedbackController;
//
//  @Autowired
//  private ObjectMapper objectMapper;
//
//  private Feedback feedback;
//  private FeedbackDTO feedbackDTO;
//
//  private Validator validator;
//
//  @BeforeEach
//  public void setUp() {
//    MockitoAnnotations.openMocks(this);
//
//    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
//    validator = factory.getValidator();
//
//    feedback = new Feedback();
//    feedback.setId(1L);
//    feedback.setName("John Doe");
//    feedback.setEmail("john.doe@example.com");
//    feedback.setRating(5);
//    feedback.setComments("Great feedback!");
//
//    feedbackDTO = new FeedbackDTO();
//    feedbackDTO.setId(1L);
//    feedbackDTO.setName("John Doe");
//    feedbackDTO.setEmail("john.doe@example.com");
//    feedbackDTO.setRating(5);
//    feedbackDTO.setComments("Great feedback!");
//  }
//
//  @Test
//  @Order(1) // This test will run first
//  @DisplayName("Test: Submit Feedback with valid data")
//  public void testSubmitFeedback() throws Exception {
//    when(feedbackService.saveFeedback(any(Feedback.class))).thenReturn(feedbackDTO);
//
//    mockMvc.perform(post("/api/feedback/submit")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(feedback)))
//        .andExpect(status().isCreated())
//        .andExpect(jsonPath("$.name").value("John Doe"))
//        .andExpect(jsonPath("$.email").value("john.doe@example.com"))
//        .andExpect(jsonPath("$.rating").value(5))
//        .andExpect(jsonPath("$.comments").value("Great feedback!"));
//
//    verify(feedbackService, times(1)).saveFeedback(any(Feedback.class));
//  }
//
//  @Test
//  @Order(2)
//  @DisplayName("Test: Submit Feedback with invalid email")
//  public void testSubmitFeedback_withInvalidEmail() throws Exception {
//    feedback.setEmail("invalid-email");
//
//    mockMvc.perform(post("/api/feedback/submit")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(feedback)))
//        .andExpect(status().isBadRequest())
//        .andExpect(jsonPath("$.errors[0]").value("Please provide a valid email address"));
//  }
//
//  @Test
//  @Order(3)
//  @DisplayName("Test: Submit Feedback with name exceeding length")
//  public void testSubmitFeedback_withTooLongName() throws Exception {
//    feedback.setName("A very long name that exceeds the fifty characters limit to cause validation failure");
//
//    mockMvc.perform(post("/api/feedback/submit")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(feedback)))
//        .andExpect(status().isBadRequest())
//        .andExpect(jsonPath("$.errors[0]").value("Name should not exceed 50 characters"));
//  }
//
//  @Test
//  @Order(4)
//  @DisplayName("Test: Get All Feedback")
//  public void testGetAllFeedback() throws Exception {
//    List<FeedbackDTO> feedbackList = Arrays.asList(feedbackDTO);
//    when(feedbackService.getAllFeedback()).thenReturn(feedbackList);
//
//    mockMvc.perform(get("/api/feedback/all"))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$[0].name").value("John Doe"))
//        .andExpect(jsonPath("$[0].rating").value(5));
//
//    verify(feedbackService, times(1)).getAllFeedback();
//  }
//
//  @Test
//  @Order(5)
//  @DisplayName("Test: Get Feedback by ID")
//  public void testGetFeedbackById() throws Exception {
//    when(feedbackService.getFeedbackById(1L)).thenReturn(feedbackDTO);
//
//    mockMvc.perform(get("/api/feedback/1"))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.name").value("John Doe"))
//        .andExpect(jsonPath("$.rating").value(5));
//
//    verify(feedbackService, times(1)).getFeedbackById(1L);
//  }
//
//  @Test
//  @Order(6)
//  @DisplayName("Test: Delete Feedback by ID")
//  public void testDeleteFeedback() throws Exception {
//    doNothing().when(feedbackService).deleteFeedback(1L);
//
//    mockMvc.perform(delete("/api/feedback/1"))
//        .andExpect(status().isNoContent());
//
//    verify(feedbackService, times(1)).deleteFeedback(1L);
//  }
//
//  @Test
//  @Order(7) // This test will run seventh
//  @DisplayName("Test: Show Feedback on Website")
//  public void testShowFeedback() throws Exception {
//    List<FeedbackDTO> feedbackList = Arrays.asList(feedbackDTO);
//    when(feedbackService.getFeedbackShowOnWebsite()).thenReturn(feedbackList);
//
//    mockMvc.perform(get("/api/feedback/show"))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$[0].name").value("John Doe"))
//        .andExpect(jsonPath("$[0].rating").value(5));
//
//    verify(feedbackService, times(1)).getFeedbackShowOnWebsite();
//  }
//
//  @Test
//  @Order(8) // This test will run eighth
//  @DisplayName("Test: Update Feedback to Show on Website")
//  public void testUpdateFeedbackShowOnWebsite() throws Exception {
//    when(feedbackService.updateFeedbackShowOnWebsite(1L)).thenReturn(feedbackDTO);
//
//    mockMvc.perform(put("/api/feedback/show/1"))
//        .andExpect(status().isOk())
//        .andExpect(jsonPath("$.name").value("John Doe"))
//        .andExpect(jsonPath("$.rating").value(5));
//
//    verify(feedbackService, times(1)).updateFeedbackShowOnWebsite(1L);
//  }
//
//  @Test
//  @Order(9) // This test will run last
//  @DisplayName("Test: Feedback Validation with Invalid Rating")
//  public void testFeedbackValidation()  {
//    // Set invalid rating (greater than 5)
//    feedback.setRating(6);
//
//    // Validate the feedback object
//    Set<ConstraintViolation<Feedback>> violations = validator.validate(feedback);
//
//    // Assert that there is a violation
//    assertFalse(violations.isEmpty(), "There should be a validation error for the rating.");
//
//    // Assert that the size of violations is 1
//    assertEquals(1, violations.size(), "There should be exactly one validation error.");
//
//    // Assert that the message is the expected one
//    assertEquals("Rating should not be more than 5", violations.iterator().next().getMessage());
//  }
//}
