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
  private  FeedbackService feedbackService;

  @Autowired
  private ModelMapper modelMapper;

  public FeedbackController(FeedbackService feedbackService) {
    this.feedbackService = feedbackService;
  }

  @PostMapping("/submit")
  public ResponseEntity<FeedbackDTO> submitFeedback(@RequestBody FeedbackDTO feedbackDTO) {
    FeedbackDTO savedFeedbackDTO = feedbackService.saveFeedback(feedbackDTO);
    return new ResponseEntity<>(savedFeedbackDTO, HttpStatus.CREATED);
  }

  @GetMapping("/all")
  public ResponseEntity<List<FeedbackDTO>> getAllFeedback() {
    List<FeedbackDTO> feedbacksDTO = feedbackService.getAllFeedback();
    return new ResponseEntity<>(feedbacksDTO, HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<FeedbackDTO> getFeedbackById(@PathVariable Long id) {
    FeedbackDTO feedbackDTO = feedbackService.getFeedbackById(id);
    if (feedbackDTO != null) {
      return new ResponseEntity<>(feedbackDTO, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteFeedback(@PathVariable Long id) {
    FeedbackDTO feedbackDTO = feedbackService.getFeedbackById(id);
    if (feedbackDTO != null) {
      feedbackService.deleteFeedback(id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }
}
