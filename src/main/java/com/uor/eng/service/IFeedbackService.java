package com.uor.eng.service;
import com.uor.eng.payload.FeedbackDTO;

import java.util.List;

public interface IFeedbackService {
  FeedbackDTO saveFeedback(FeedbackDTO FeedbackDTO);

  List<FeedbackDTO> getAllFeedback();

  FeedbackDTO getFeedbackById(Long id);

  void deleteFeedback(Long id);
}
