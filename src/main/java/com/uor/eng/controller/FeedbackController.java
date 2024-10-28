package com.uor.eng.controller;

import com.uor.eng.service.FeedbackService;
import com.uor.eng.payload.FeedbackDTO;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

  @Autowired
  private FeedbackService feedbackService;

  @Autowired
  private ModelMapper modelMapper;

  public FeedbackController(FeedbackService feedbackService) {
    this.feedbackService = feedbackService;
  }

  @PostMapping("/submit")
  public ResponseEntity<?> submitFeedback(@RequestBody FeedbackDTO feedbackDTO) {
    try {
      FeedbackDTO savedFeedbackDTO = feedbackService.saveFeedback(feedbackDTO);
      return new ResponseEntity<>(savedFeedbackDTO, HttpStatus.CREATED);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }

  @GetMapping("/all")
  public ResponseEntity<?> getAllFeedback() {
    try {
      List<FeedbackDTO> feedbacksDTO = feedbackService.getAllFeedback();
      return new ResponseEntity<>(feedbacksDTO, HttpStatus.OK);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NO_CONTENT);
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getFeedbackById(@PathVariable Long id) {
    try {
      FeedbackDTO feedbackDTO = feedbackService.getFeedbackById(id);
      return new ResponseEntity<>(feedbackDTO, HttpStatus.OK);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteFeedback(@PathVariable Long id) {
    try {
      feedbackService.deleteFeedback(id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } catch (RuntimeException e) {
      return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
  }
}
