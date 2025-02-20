package com.uor.eng.repository;

import com.uor.eng.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository  extends JpaRepository<Feedback, Long> {

  List<Feedback> findByShowOnWebsite(boolean b);
}
