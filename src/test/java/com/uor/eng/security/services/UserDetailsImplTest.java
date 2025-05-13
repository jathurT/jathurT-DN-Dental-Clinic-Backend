package com.uor.eng.security.services;

import com.uor.eng.model.AppRole;
import com.uor.eng.model.Role;
import com.uor.eng.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsImplTest {

  private User user;
  private Set<Role> roles;
  private UserDetailsImpl userDetails;

  @BeforeEach
  void setUp() {
    // Create roles
    roles = new HashSet<>();
    Role role = new Role();
    role.setRoleId(1L);
    role.setRoleName(AppRole.ROLE_DENTIST);
    roles.add(role);

    // Create user
    user = new User();
    user.setUserId(1L);
    user.setUserName("testUser");
    user.setEmail("test@example.com");
    user.setPassword("password123");
    user.setRoles(roles);

    // Create userDetails
    userDetails = UserDetailsImpl.build(user);
  }

  @Test
  void testBuild() {
    // Test UserDetailsImpl.build method
    UserDetailsImpl result = UserDetailsImpl.build(user);

    assertEquals(user.getUserId(), result.getId());
    assertEquals(user.getUserName(), result.getUsername());
    assertEquals(user.getEmail(), result.getEmail());
    assertEquals(user.getPassword(), result.getPassword());

    Collection<? extends GrantedAuthority> authorities = result.getAuthorities();
    Set<String> authorityNames = authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

    assertEquals(1, authorityNames.size());
    assertTrue(authorityNames.contains(AppRole.ROLE_DENTIST.name()));
  }

  @Test
  void testAccountMethods() {
    // Test default implementations of UserDetails interface methods
    assertTrue(userDetails.isAccountNonExpired());
    assertTrue(userDetails.isAccountNonLocked());
    assertTrue(userDetails.isCredentialsNonExpired());
    assertTrue(userDetails.isEnabled());
  }

  @Test
  void testGetAuthorities() {
    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
    assertNotNull(authorities);
    assertEquals(1, authorities.size());

    boolean hasExpectedRole = authorities.stream()
            .anyMatch(authority -> authority.getAuthority().equals(AppRole.ROLE_DENTIST.name()));
    assertTrue(hasExpectedRole);
  }

  @Test
  void testGettersAndSetters() {
    // Test all getters
    assertEquals(1L, userDetails.getId());
    assertEquals("testUser", userDetails.getUsername());
    assertEquals("test@example.com", userDetails.getEmail());
    assertEquals("password123", userDetails.getPassword());

    // Create new UserDetailsImpl instance for testing setters
    UserDetailsImpl newUserDetails = new UserDetailsImpl();
    newUserDetails.setId(2L);
    newUserDetails.setUsername("newUser");
    newUserDetails.setEmail("new@example.com");
    newUserDetails.setPassword("newPassword");

    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(AppRole.ROLE_ADMIN.name());
    Set<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(authority);
    newUserDetails.setAuthorities(authorities);

    assertEquals(2L, newUserDetails.getId());
    assertEquals("newUser", newUserDetails.getUsername());
    assertEquals("new@example.com", newUserDetails.getEmail());
    assertEquals("newPassword", newUserDetails.getPassword());

    Collection<? extends GrantedAuthority> retrievedAuthorities = newUserDetails.getAuthorities();
    assertEquals(1, retrievedAuthorities.size());
    assertEquals(AppRole.ROLE_ADMIN.name(), retrievedAuthorities.iterator().next().getAuthority());
  }

  @Test
  void testEquals() {
    // Create same user (same id)
    User sameUser = new User();
    sameUser.setUserId(1L);
    sameUser.setUserName("differentName");
    sameUser.setEmail("different@example.com");
    sameUser.setRoles(roles);
    UserDetailsImpl sameUserDetails = UserDetailsImpl.build(sameUser);

    // Create different user (different id)
    User differentUser = new User();
    differentUser.setUserId(2L);
    differentUser.setUserName("testUser");
    differentUser.setEmail("test@example.com");
    differentUser.setRoles(roles);
    UserDetailsImpl differentUserDetails = UserDetailsImpl.build(differentUser);

    // Test equals and hashCode
    assertEquals(userDetails, userDetails); // Self equality
    assertEquals(userDetails, sameUserDetails); // Same ID
    assertNotEquals(userDetails, differentUserDetails); // Different ID
    assertNotEquals(null, userDetails); // Null check
    assertNotEquals(new Object(), userDetails); // Different class

    // Hash code tests
    assertEquals(userDetails.hashCode(), sameUserDetails.hashCode());
    assertNotEquals(userDetails.hashCode(), differentUserDetails.hashCode());
  }
}