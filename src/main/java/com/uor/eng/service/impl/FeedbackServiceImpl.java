package com.uor.eng.service.impl;

import com.uor.eng.model.Feedback;
import com.uor.eng.repository.FeedbackRepository;
import com.uor.eng.payload.other.FeedbackDTO;
import com.uor.eng.service.IFeedbackService;
import com.uor.eng.exceptions.ResourceNotFoundException;
import com.uor.eng.exceptions.BadRequestException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackServiceImpl implements IFeedbackService {

  @Autowired
  private FeedbackRepository feedbackRepository;

  @Autowired
  private ModelMapper modelMapper;

  @Override
  public FeedbackDTO saveFeedback(FeedbackDTO feedbackDTO) {
    if (feedbackDTO == null) {
      throw new BadRequestException("Feedback data cannot be null.");
    }

    Feedback feedback = modelMapper.map(feedbackDTO, Feedback.class);
    Feedback savedFeedback = feedbackRepository.save(feedback);
    return modelMapper.map(savedFeedback, FeedbackDTO.class);
  }

  @Override
  public List<FeedbackDTO> getAllFeedback() {
    List<Feedback> feedbacks = feedbackRepository.findAll();
    if (feedbacks.isEmpty()) {
      throw new ResourceNotFoundException("No feedback entries found. Please add feedback to view the list.");
    }
    return feedbacks.stream()
            .map(feedback -> modelMapper.map(feedback, FeedbackDTO.class))
            .collect(Collectors.toList());
  }

  @Override
  public FeedbackDTO getFeedbackById(Long id) {
    Feedback feedback = feedbackRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Feedback with ID " + id + " not found. Please check the ID and try again."));
    return modelMapper.map(feedback, FeedbackDTO.class);
  }

  @Override
  public void deleteFeedback(Long id) {
    if (!feedbackRepository.existsById(id)) {
      throw new ResourceNotFoundException("Feedback with ID " + id + " does not exist. Unable to delete.");
    }
    feedbackRepository.deleteById(id);
  }

  @Override
  public List<FeedbackDTO> getFeedbackShowOnWebsite() {
    List<Feedback> feedbacks = feedbackRepository.findByShowOnWebsite(true);
    if (feedbacks.isEmpty()) {
      throw new ResourceNotFoundException("No feedback entries found to show on display.");
    }
    return feedbacks.stream()
            .map(feedback -> modelMapper.map(feedback, FeedbackDTO.class))
            .collect(Collectors.toList());
  }

  @Override
  public FeedbackDTO updateFeedbackShowOnWebsite(Long id) {
    Feedback feedback = feedbackRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Feedback with ID " + id + " not found. Please check the ID and try again."));
    feedback.setShowOnWebsite(!feedback.getShowOnWebsite());
    Feedback updatedFeedback = feedbackRepository.save(feedback);
    return modelMapper.map(updatedFeedback, FeedbackDTO.class);
  }
}
