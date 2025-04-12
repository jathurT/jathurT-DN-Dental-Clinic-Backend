package com.uor.eng.controller;

import com.uor.eng.model.Feedback;
import com.uor.eng.payload.other.FeedbackDTO;
import com.uor.eng.service.IFeedbackService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

  private final IFeedbackService feedbackService;

  public FeedbackController(IFeedbackService feedbackService) {
    this.feedbackService = feedbackService;
  }

  @PostMapping("/submit")
  public ResponseEntity<FeedbackDTO> submitFeedback(@Valid @RequestBody Feedback feedback) {
    FeedbackDTO savedFeedbackDTO = feedbackService.saveFeedback(feedback);
    return ResponseEntity.status(201).body(savedFeedbackDTO);
  }

  @GetMapping("/all")
  public ResponseEntity<List<FeedbackDTO>> getAllFeedback() {
    List<FeedbackDTO> feedbacksDTO = feedbackService.getAllFeedback();
    return ResponseEntity.ok(feedbacksDTO);
  }

  @GetMapping("/{id}")
  public ResponseEntity<FeedbackDTO> getFeedbackById(@PathVariable Long id) {
    FeedbackDTO feedbackDTO = feedbackService.getFeedbackById(id);
    return ResponseEntity.ok(feedbackDTO);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
    feedbackService.deleteFeedback(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/show")
  public ResponseEntity<List<FeedbackDTO>> showFeedback() {
    List<FeedbackDTO> feedbacksDTO = feedbackService.getFeedbackShowOnWebsite();
    return ResponseEntity.ok(feedbacksDTO);
  }

  @PutMapping("/show/{id}")
  public ResponseEntity<FeedbackDTO> showFeedback(@PathVariable Long id) {
    FeedbackDTO feedbackDTO = feedbackService.updateFeedbackShowOnWebsite(id);
    return ResponseEntity.ok(feedbackDTO);
  }
}
