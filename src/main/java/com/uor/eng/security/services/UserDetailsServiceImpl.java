package com.uor.eng.security.services;

import com.uor.eng.model.User;
import com.uor.eng.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired
  private UserRepository userRepository;

  @Override
  @Transactional
  public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
    Optional<User> userOpt = userRepository.findByUserName(usernameOrEmail);

    if (userOpt.isEmpty()) {
      userOpt = userRepository.findByEmail(usernameOrEmail);
    }

    User user = userOpt.orElseThrow(() ->
        new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));

    return UserDetailsImpl.build(user);
  }
}
