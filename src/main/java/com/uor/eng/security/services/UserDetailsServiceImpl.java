package com.uor.eng.security.services;

import com.uor.eng.model.User;
import com.uor.eng.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired
  private UserRepository userRepository;

  @Override
  @Transactional
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user = (User) userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + email));

    return UserDetailsImpl.build(user);
  }
}
