package com.uor.eng.repository;

import com.uor.eng.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository  extends JpaRepository<Feedback, Long> {

}
