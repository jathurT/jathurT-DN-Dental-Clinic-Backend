package com.uor.eng.service;

import com.uor.eng.model.Feedback;
import com.uor.eng.repository.FeedbackRepository;
import com.uor.eng.payload.FeedbackDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FeedbackServiceImpl implements FeedbackService {

  @Autowired
  private FeedbackRepository feedbackRepository;

  @Autowired
  private ModelMapper modelMapper;

  public FeedbackServiceImpl(FeedbackRepository feedbackRepository) {
    this.feedbackRepository = feedbackRepository;
  }

  @Override
  public FeedbackDTO saveFeedback(FeedbackDTO feedbackDTO) {
    Feedback feedback = modelMapper.map(feedbackDTO, Feedback.class);
    Feedback savedFeedback = feedbackRepository.save(feedback);
    return modelMapper.map(savedFeedback, FeedbackDTO.class);
  }

  @Override
  public List<FeedbackDTO> getAllFeedback() {
    List<Feedback> feedbacks = feedbackRepository.findAll();
    if (feedbacks.isEmpty()) {
      throw new RuntimeException("No feedback entries found. Please add feedback to view the list.");
    }
    return feedbacks.stream()
            .map(feedback -> modelMapper.map(feedback, FeedbackDTO.class))
            .collect(Collectors.toList());
  }

  @Override
  public FeedbackDTO getFeedbackById(Long id) {
    Feedback feedback = feedbackRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Feedback with ID " + id + " not found. Please check the ID and try again."));
    return modelMapper.map(feedback, FeedbackDTO.class);
  }

  @Override
  public void deleteFeedback(Long id) {
    if (feedbackRepository.existsById(id)) {
      feedbackRepository.deleteById(id);
    } else {
      throw new RuntimeException("Feedback with ID " + id + " does not exist. Unable to delete.");
    }
  }
}
