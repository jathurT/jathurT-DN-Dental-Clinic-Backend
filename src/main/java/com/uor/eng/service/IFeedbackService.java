package com.uor.eng.service;
import com.uor.eng.model.Feedback;
import com.uor.eng.payload.other.FeedbackDTO;

import java.util.List;

public interface IFeedbackService {
  FeedbackDTO saveFeedback(Feedback feedback);

  List<FeedbackDTO> getAllFeedback();

  FeedbackDTO getFeedbackById(Long id);

  void deleteFeedback(Long id);

  List<FeedbackDTO> getFeedbackShowOnWebsite();

  FeedbackDTO updateFeedbackShowOnWebsite(Long id);
}
