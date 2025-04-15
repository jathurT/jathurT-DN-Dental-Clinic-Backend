package com.uor.eng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.Feedback;
import com.uor.eng.payload.other.FeedbackDTO;
import com.uor.eng.service.IFeedbackService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FeedbackControllerTest {

  private MockMvc mockMvc;

  @Mock
  private IFeedbackService feedbackService;

  @InjectMocks
  private FeedbackController feedbackController;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(feedbackController)
            .setControllerAdvice(new TestExceptionHandler())
            .build();
  }

  @Test
  public void testSubmitFeedback_Success() throws Exception {
    // Arrange
    Feedback feedback = new Feedback();
    feedback.setName("John Doe");
    feedback.setEmail("john@example.com");
    feedback.setRating(5);
    feedback.setComments("Great service!");

    FeedbackDTO feedbackDTO = new FeedbackDTO();
    feedbackDTO.setId(1L);
    feedbackDTO.setName("John Doe");
    feedbackDTO.setEmail("john@example.com");
    feedbackDTO.setRating(5);
    feedbackDTO.setComments("Great service!");
    feedbackDTO.setShowOnWebsite(false);

    when(feedbackService.saveFeedback(any(Feedback.class))).thenReturn(feedbackDTO);

    // Act & Assert
    mockMvc.perform(post("/api/feedback/submit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(feedback)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("John Doe")))
            .andExpect(jsonPath("$.email", is("john@example.com")))
            .andExpect(jsonPath("$.rating", is(5)))
            .andExpect(jsonPath("$.comments", is("Great service!")))
            .andExpect(jsonPath("$.showOnWebsite", is(false)));
  }

  @Test
  public void testGetAllFeedback_Success() throws Exception {
    // Arrange
    List<FeedbackDTO> feedbacks = getFeedbackDTOS();

    when(feedbackService.getAllFeedback()).thenReturn(feedbacks);

    // Act & Assert
    mockMvc.perform(get("/api/feedback/all"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].name", is("John Doe")))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].name", is("Jane Doe")));
  }

  private static List<FeedbackDTO> getFeedbackDTOS() {
    FeedbackDTO feedback1 = new FeedbackDTO();
    feedback1.setId(1L);
    feedback1.setName("John Doe");
    feedback1.setEmail("john@example.com");
    feedback1.setRating(5);
    feedback1.setComments("Great service!");
    feedback1.setShowOnWebsite(false);

    FeedbackDTO feedback2 = new FeedbackDTO();
    feedback2.setId(2L);
    feedback2.setName("Jane Doe");
    feedback2.setEmail("jane@example.com");
    feedback2.setRating(4);
    feedback2.setComments("Good service!");
    feedback2.setShowOnWebsite(true);

    return Arrays.asList(feedback1, feedback2);
  }

  @Test
  public void testGetFeedbackById_Success() throws Exception {
    // Arrange
    FeedbackDTO feedback = new FeedbackDTO();
    feedback.setId(1L);
    feedback.setName("John Doe");
    feedback.setEmail("john@example.com");
    feedback.setRating(5);
    feedback.setComments("Great service!");
    feedback.setShowOnWebsite(false);

    when(feedbackService.getFeedbackById(1L)).thenReturn(feedback);

    // Act & Assert
    mockMvc.perform(get("/api/feedback/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("John Doe")))
            .andExpect(jsonPath("$.email", is("john@example.com")))
            .andExpect(jsonPath("$.rating", is(5)))
            .andExpect(jsonPath("$.comments", is("Great service!")))
            .andExpect(jsonPath("$.showOnWebsite", is(false)));
  }

  @Test
  public void testGetFeedbackById_NotFound() throws Exception {
    // Arrange
    when(feedbackService.getFeedbackById(999L))
            .thenThrow(new ResourceNotFoundException("Feedback with ID 999 not found"));

    // Act & Assert
    mockMvc.perform(get("/api/feedback/999"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.details.error").value("Feedback with ID 999 not found"));
  }

  @Test
  public void testDeleteFeedback_Success() throws Exception {
    // Arrange
    doNothing().when(feedbackService).deleteFeedback(1L);

    // Act & Assert
    mockMvc.perform(delete("/api/feedback/1"))
            .andDo(print())
            .andExpect(status().isNoContent());
  }

  @Test
  public void testDeleteFeedback_NotFound() throws Exception {
    // Arrange
    doThrow(new ResourceNotFoundException("Feedback with ID 999 not found"))
            .when(feedbackService).deleteFeedback(999L);

    // Act & Assert
    mockMvc.perform(delete("/api/feedback/999"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.details.error").value("Feedback with ID 999 not found"));
  }

  @Test
  public void testShowFeedback_Success() throws Exception {
    // Arrange
    List<FeedbackDTO> feedbacks = Arrays.asList(
            new FeedbackDTO(1L, "John Doe", "john@example.com", 5, "Great service!", true),
            new FeedbackDTO(2L, "Jane Doe", "jane@example.com", 4, "Good service!", true)
    );

    when(feedbackService.getFeedbackShowOnWebsite()).thenReturn(feedbacks);

    // Act & Assert
    mockMvc.perform(get("/api/feedback/show"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].showOnWebsite", is(true)))
            .andExpect(jsonPath("$[1].id", is(2)))
            .andExpect(jsonPath("$[1].showOnWebsite", is(true)));
  }

  @Test
  public void testShowFeedbackById_Success() throws Exception {
    // Arrange
    FeedbackDTO feedback = new FeedbackDTO();
    feedback.setId(1L);
    feedback.setName("John Doe");
    feedback.setEmail("john@example.com");
    feedback.setRating(5);
    feedback.setComments("Great service!");
    feedback.setShowOnWebsite(true);

    when(feedbackService.updateFeedbackShowOnWebsite(1L)).thenReturn(feedback);

    // Act & Assert
    mockMvc.perform(put("/api/feedback/show/1"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.showOnWebsite", is(true)));
  }

  @Test
  public void testShowFeedbackById_NotFound() throws Exception {
    // Arrange
    when(feedbackService.updateFeedbackShowOnWebsite(999L))
            .thenThrow(new ResourceNotFoundException("Feedback with ID 999 not found"));

    // Act & Assert
    mockMvc.perform(put("/api/feedback/show/999"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.details.error").value("Feedback with ID 999 not found"));
  }
}