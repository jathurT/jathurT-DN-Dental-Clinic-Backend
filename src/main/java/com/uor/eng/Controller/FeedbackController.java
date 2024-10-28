package com.uor.eng.Controller;

import com.uor.eng.Entity.Feedback;
import com.uor.eng.Service.FeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(origins = "http://localhost:5176/")
public class FeedbackController {

  private final FeedbackService feedbackService;

  public FeedbackController(FeedbackService feedbackService) {
    this.feedbackService = feedbackService;
  }

  @PostMapping("/submit")
  public ResponseEntity<Feedback> submitFeedback(@RequestBody Feedback feedback) {
    Feedback savedFeedback = feedbackService.saveFeedback(feedback);
    return new ResponseEntity<>(savedFeedback, HttpStatus.CREATED);
  }

  @GetMapping("/all")
  public ResponseEntity<List<Feedback>> getAllFeedback() {
    List<Feedback> feedbacks = feedbackService.getAllFeedback();
    return new ResponseEntity<>(feedbacks, HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Feedback> getFeedbackById(@PathVariable Long id) {
    Feedback feedback = feedbackService.getFeedbackById(id);
    if (feedback != null) {
      return new ResponseEntity<>(feedback, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
    Feedback feedback = feedbackService.getFeedbackById(id);
    if (feedback != null) {
      feedbackService.deleteFeedback(id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }
}
