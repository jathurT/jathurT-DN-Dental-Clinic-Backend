package com.uor.eng.Service;

import com.uor.eng.Entity.Feedback;
import com.uor.eng.Repository.FeedbackRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FeedbackServiceImpl implements FeedbackService {

  private final FeedbackRepository feedbackRepository;

  public FeedbackServiceImpl(FeedbackRepository feedbackRepository) {
    this.feedbackRepository = feedbackRepository;
  }

  @Override
  public Feedback saveFeedback(Feedback feedback) {
    return feedbackRepository.save(feedback);
  }

  @Override
  public List<Feedback> getAllFeedback() {
    return feedbackRepository.findAll();
  }

  @Override
  public Feedback getFeedbackById(Long id) {
    Optional<Feedback> feedback = feedbackRepository.findById(id);
    return feedback.orElse(null);
  }

  @Override
  public void deleteFeedback(Long id) {
    feedbackRepository.deleteById(id);
  }
}

