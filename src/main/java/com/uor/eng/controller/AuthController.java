package com.uor.eng.controller;

import com.uor.eng.model.AppRole;
import com.uor.eng.model.Role;
import com.uor.eng.model.User;
import com.uor.eng.payload.ForgotPasswordRequest;
import com.uor.eng.payload.ResetPasswordRequest;
import com.uor.eng.repository.RoleRepository;
import com.uor.eng.repository.UserRepository;
import com.uor.eng.security.jwt.JwtUtils;
import com.uor.eng.security.request.LoginRequest;
import com.uor.eng.security.request.SignupRequest;
import com.uor.eng.security.response.MessageResponse;
import com.uor.eng.security.response.UserInfoResponse;
import com.uor.eng.security.services.UserDetailsImpl;
import com.uor.eng.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private PasswordEncoder encoder;

  @Autowired
  private PasswordResetService passwordResetService;

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    Authentication authentication;
    try {
      authentication = authenticationManager
          .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUserNameOrEmail(), loginRequest.getPassword()));
    } catch (AuthenticationException exception) {
      Map<String, Object> map = new HashMap<>();
      map.put("message", "Bad credentials");
      map.put("status", false);
      return new ResponseEntity<Object>(map, HttpStatus.UNAUTHORIZED);
    }

    SecurityContextHolder.getContext().setAuthentication(authentication);

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

    List<String> roles = userDetails.getAuthorities().stream()
        .map(item -> item.getAuthority())
        .collect(Collectors.toList());

    UserInfoResponse response = new UserInfoResponse(userDetails.getId(),
        userDetails.getUsername(), roles, jwtCookie.toString());

    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
            jwtCookie.toString())
        .body(response);
  }

  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    if (userRepository.existsByUserName(signUpRequest.getUsername())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
    }

    User user = new User(signUpRequest.getUsername(),
        signUpRequest.getEmail(),
        encoder.encode(signUpRequest.getPassword()));

    Set<String> strRoles = signUpRequest.getRole();
    Set<Role> roles = new HashSet<>();

    if (strRoles == null) {
      Role receiptionistRole = roleRepository.findByRoleName(AppRole.ROLE_RECEPTIONIST)
          .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
      roles.add(receiptionistRole);
    } else {
      strRoles.forEach(role -> {
        switch (role) {
          case "admin":
            Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(adminRole);

            break;
          case "doctor":
            Role doctorRole = roleRepository.findByRoleName(AppRole.ROLE_DENTIST)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(doctorRole);

            break;
          default:
            Role receiptionistRole = roleRepository.findByRoleName(AppRole.ROLE_RECEPTIONIST)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(receiptionistRole);
        }
      });
    }
    user.setRoles(roles);
    userRepository.save(user);
    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }

  @PostMapping("/signout")
  public ResponseEntity<?> signoutUser() {
    ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
            cookie.toString())
        .body(new MessageResponse("You've been signed out!"));
  }

  @GetMapping("/user")
  public ResponseEntity<?> getUserDetails(Authentication authentication) {
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    List<String> roles = userDetails.getAuthorities().stream()
        .map(item -> item.getAuthority())
        .collect(Collectors.toList());

    UserInfoResponse response = new UserInfoResponse(userDetails.getId(),
        userDetails.getUsername(), roles);

    return ResponseEntity.ok().body(response);
  }

  @GetMapping("/username")
  public ResponseEntity<String> currentUserName(Authentication authentication) {
    if (authentication != null)
      return ResponseEntity.ok(authentication.getName());
    else
      return ResponseEntity.ok("anonymousUser");
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
    passwordResetService.initiatePasswordReset(forgotPasswordRequest);
    return new ResponseEntity<>(new MessageResponse("Password reset link sent to your email."), HttpStatus.OK);
  }

  @PostMapping("/reset-password")
  public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    passwordResetService.resetPassword(request);
    return ResponseEntity.ok(new MessageResponse("Password has been reset successfully."));
  }
}
