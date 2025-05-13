package com.uor.eng.service.impl;

import com.uor.eng.exceptions.BadRequestException;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.model.Feedback;
import com.uor.eng.payload.other.FeedbackDTO;
import com.uor.eng.repository.FeedbackRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class FeedbackServiceImplTest {

  @InjectMocks
  private FeedbackServiceImpl feedbackService;

  @Mock
  private FeedbackRepository feedbackRepository;

  @Mock
  private ModelMapper modelMapper;

  private Feedback feedback;
  private FeedbackDTO feedbackDTO;

  @BeforeEach
  void setUp() {
    feedback = new Feedback(1L, "John Doe", "john@example.com", 5, "Great service!", false);
    feedbackDTO = new FeedbackDTO(1L, "John Doe", "john@example.com", 5, "Great service!", false);
  }

  @AfterEach
  void tearDown() {
    reset(feedbackRepository, modelMapper);
  }

  @Test
  @DisplayName("Test save feedback - Success")
  @Order(1)
  void saveFeedback_ShouldSaveAndReturnFeedback() {
    when(feedbackRepository.save(feedback)).thenReturn(feedback);
    when(modelMapper.map(feedback, FeedbackDTO.class)).thenReturn(feedbackDTO);

    FeedbackDTO result = feedbackService.saveFeedback(feedback);

    assertNotNull(result);
    assertEquals(feedbackDTO.getId(), result.getId());
    verify(feedbackRepository, times(1)).save(feedback);
  }

  @Test
  @DisplayName("Test save feedback - Failure")
  @Order(2)
  void saveFeedback_ShouldThrowBadRequestException_WhenFeedbackIsNull() {
    assertThrows(BadRequestException.class, () -> feedbackService.saveFeedback(null));
    verify(feedbackRepository, never()).save(any(Feedback.class));
  }

  @Test
  @DisplayName("Test get all feedback - Success")
  @Order(3)
  void getAllFeedback_ShouldReturnListOfFeedback() {
    List<Feedback> feedbackList = Arrays.asList(feedback);
    List<FeedbackDTO> feedbackDTOList = Arrays.asList(feedbackDTO);

    when(feedbackRepository.findAll()).thenReturn(feedbackList);
    when(modelMapper.map(feedback, FeedbackDTO.class)).thenReturn(feedbackDTO);

    List<FeedbackDTO> result = feedbackService.getAllFeedback();

    assertNotNull(result);
    assertEquals(feedbackDTOList.size(), result.size());
    verify(feedbackRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Test get all feedback - Failure")
  @Order(4)
  void getAllFeedback_ShouldThrowResourceNotFoundException_WhenNoFeedbackExists() {
    when(feedbackRepository.findAll()).thenReturn(Arrays.asList());

    assertThrows(ResourceNotFoundException.class, () -> feedbackService.getAllFeedback());
    verify(feedbackRepository, times(1)).findAll();
  }

  @Test
  @DisplayName("Test get feedback by ID - Success")
  @Order(5)
  void getFeedbackById_ShouldReturnFeedback_WhenIdExists() {
    when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));
    when(modelMapper.map(feedback, FeedbackDTO.class)).thenReturn(feedbackDTO);

    FeedbackDTO result = feedbackService.getFeedbackById(1L);

    assertNotNull(result);
    assertEquals(feedbackDTO.getId(), result.getId());
    verify(feedbackRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Test get feedback by ID - Failure")
  @Order(6)
  void getFeedbackById_ShouldThrowResourceNotFoundException_WhenIdDoesNotExist() {
    when(feedbackRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> feedbackService.getFeedbackById(1L));
    verify(feedbackRepository, times(1)).findById(1L);
  }

  @Test
  @DisplayName("Test delete feedback - Success")
  @Order(7)
  void deleteFeedback_ShouldDeleteFeedback_WhenIdExists() {
    when(feedbackRepository.existsById(1L)).thenReturn(true);

    feedbackService.deleteFeedback(1L);

    verify(feedbackRepository, times(1)).deleteById(1L);
  }

  @Test
  @DisplayName("Test delete feedback - Failure")
  @Order(8)
  void deleteFeedback_ShouldThrowResourceNotFoundException_WhenIdDoesNotExist() {
    when(feedbackRepository.existsById(1L)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> feedbackService.deleteFeedback(1L));
    verify(feedbackRepository, never()).deleteById(anyLong());
  }

  @Test
  @DisplayName("Test get feedback to show on website - Success")
  @Order(9)
  void getFeedbackShowOnWebsite_ShouldReturnFeedbackList_WhenFeedbackExists() {
    List<Feedback> feedbackList = Arrays.asList(feedback);
    when(feedbackRepository.findByShowOnWebsite(true)).thenReturn(feedbackList);
    when(modelMapper.map(feedback, FeedbackDTO.class)).thenReturn(feedbackDTO);

    List<FeedbackDTO> result = feedbackService.getFeedbackShowOnWebsite();

    assertNotNull(result);
    assertEquals(1, result.size());
    verify(feedbackRepository, times(1)).findByShowOnWebsite(true);
  }

  @Test
  @DisplayName("Test get feedback to show on website - Failure")
  @Order(10)
  void getFeedbackShowOnWebsite_ShouldThrowResourceNotFoundException_WhenNoFeedbackExists() {
    when(feedbackRepository.findByShowOnWebsite(true)).thenReturn(Arrays.asList());

    assertThrows(ResourceNotFoundException.class, () -> feedbackService.getFeedbackShowOnWebsite());
    verify(feedbackRepository, times(1)).findByShowOnWebsite(true);
  }

  @Test
  @DisplayName("Test update feedback show on website - Success")
  @Order(11)
  void updateFeedbackShowOnWebsite_ShouldToggleShowOnWebsite_WhenFeedbackExists() {
    when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));
    when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation ->
        invocation.<Feedback>getArgument(0));
    when(modelMapper.map(any(Feedback.class), eq(FeedbackDTO.class))).thenAnswer(invocation -> {
      Feedback mappedFeedback = invocation.getArgument(0);
      return new FeedbackDTO(mappedFeedback.getId(), mappedFeedback.getName(),
          mappedFeedback.getEmail(), mappedFeedback.getRating(),
          mappedFeedback.getComments(), mappedFeedback.getShowOnWebsite());
    });

    FeedbackDTO result = feedbackService.updateFeedbackShowOnWebsite(1L);

    assertNotNull(result);
    assertTrue(result.getShowOnWebsite());
    verify(feedbackRepository).findById(1L);
    verify(feedbackRepository).save(any(Feedback.class));
  }


  @Test
  @DisplayName("Test update feedback show on website - Failure")
  @Order(12)
  void updateFeedbackShowOnWebsite_ShouldThrowResourceNotFoundException_WhenFeedbackNotFound() {
    when(feedbackRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> feedbackService.updateFeedbackShowOnWebsite(1L));
    verify(feedbackRepository, times(1)).findById(1L);
    verify(feedbackRepository, never()).save(any());
  }

}
