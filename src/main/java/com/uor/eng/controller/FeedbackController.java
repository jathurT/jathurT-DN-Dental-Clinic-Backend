package com.uor.eng.controller;

import com.uor.eng.payload.FeedbackDTO;
import com.uor.eng.service.IFeedbackService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

  @Autowired
  private IFeedbackService feedbackService;

  @PostMapping("/submit")
  public ResponseEntity<FeedbackDTO> submitFeedback(@Valid @RequestBody FeedbackDTO feedbackDTO) {
    FeedbackDTO savedFeedbackDTO = feedbackService.saveFeedback(feedbackDTO);
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
}
