package com.uor.eng.Service;
import com.uor.eng.Entity.Feedback;
import org.springframework.stereotype.Service;

import java.util.List;

public interface FeedbackService {
  Feedback saveFeedback(Feedback feedback);

  List<Feedback> getAllFeedback();

  Feedback getFeedbackById(Long id);

  void deleteFeedback(Long id);
}
