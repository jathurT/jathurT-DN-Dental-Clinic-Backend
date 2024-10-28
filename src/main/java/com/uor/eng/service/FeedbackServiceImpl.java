package com.uor.eng.service;

import com.uor.eng.model.Feedback;
import com.uor.eng.repository.FeedbackRepository;
import com.uor.eng.payload.FeedbackDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
  public FeedbackDTO saveFeedback(FeedbackDTO FeedbackDTO) {
    Feedback feedback = modelMapper.map(FeedbackDTO, Feedback.class);
    Feedback savedFeedback = feedbackRepository.save(feedback);
    return modelMapper.map(savedFeedback, FeedbackDTO.class);
  }

  @Override
  public List<FeedbackDTO> getAllFeedback() {
    List<Feedback> feedbacks = feedbackRepository.findAll();
    return feedbacks.stream()
            .map(feedback -> modelMapper.map(feedback, FeedbackDTO.class))
            .toList();
  }

  @Override
  public FeedbackDTO getFeedbackById(Long id) {
    Optional<Feedback> feedback = feedbackRepository.findById(id);
    return feedback.map(f -> modelMapper.map(f, FeedbackDTO.class)).orElse(null);
  }

  @Override
  public void deleteFeedback(Long id) {
    feedbackRepository.deleteById(id);
  }
}

