package com.uor.eng.repository;

import com.uor.eng.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
  List<Contact> findByReplySent(boolean b);
}
