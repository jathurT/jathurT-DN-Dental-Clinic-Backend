package com.uor.eng.util;

import com.uor.eng.model.User;
import com.uor.eng.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {

  @Autowired
  private UserRepository userRepository;

  public String loggedInEmail() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userRepository.findByUserName(authentication.getName())
            .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + authentication.getName()));

    return user.getEmail();
  }

  public Long loggedInUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = userRepository.findByUserName(authentication.getName())
            .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + authentication.getName()));

    return user.getUserId();
  }

  public User loggedInUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return userRepository.findByUserName(authentication.getName())
            .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + authentication.getName()));

  }
}
