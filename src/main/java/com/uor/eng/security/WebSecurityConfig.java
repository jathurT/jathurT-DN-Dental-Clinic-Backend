package com.uor.eng.security;

import com.uor.eng.model.AppRole;
import com.uor.eng.model.Role;
import com.uor.eng.model.User;
import com.uor.eng.repository.RoleRepository;
import com.uor.eng.repository.UserRepository;
import com.uor.eng.security.jwt.AuthEntryPointJwt;
import com.uor.eng.security.jwt.AuthTokenFilter;
import com.uor.eng.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Set;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

  @Value("${app.default.admin.username}")
  private String adminUsername;

  @Value("${app.default.admin.email}")
  private String adminEmail;

  @Value("${app.default.admin.password}")
  private String adminPassword;

  @Value("${app.default.doctor.username}")
  private String doctorUsername;

  @Value("${app.default.doctor.email}")
  private String doctorEmail;

  @Value("${app.default.doctor.password}")
  private String doctorPassword;

  @Value("${app.default.receptionist.username}")
  private String receptionistUsername;

  @Value("${app.default.receptionist.email}")
  private String receptionistEmail;

  @Value("${app.default.receptionist.password}")
  private String receptionistPassword;

  private final UserDetailsServiceImpl userDetailsService;
  private final AuthEntryPointJwt unauthorizedHandler;
  private final CorsConfigurationSource corsConfigurationSource;

  public WebSecurityConfig(UserDetailsServiceImpl userDetailsService,
                           AuthEntryPointJwt unauthorizedHandler,
                           CorsConfigurationSource corsConfigurationSource) {
    this.userDetailsService = userDetailsService;
    this.unauthorizedHandler = unauthorizedHandler;
    this.corsConfigurationSource = corsConfigurationSource;
  }

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth ->
                    auth.requestMatchers("/api/auth/**").permitAll()
                            .requestMatchers("/v3/api-docs/**").permitAll()
                            .requestMatchers("/swagger-ui/**").permitAll()
                            .requestMatchers("/api/test/**").permitAll()
                            .requestMatchers("/images/**").permitAll()
                            .requestMatchers("/api/bookings/create").permitAll()
                            .requestMatchers("/api/bookings/{referenceId}/{contactNumber}").permitAll()
                            .requestMatchers("/api/schedules/{id}").permitAll()
                            .requestMatchers("/api/schedules/getSeven").permitAll()
                            .requestMatchers("/api/feedback/submit").permitAll()
                            .requestMatchers("/api/feedback/show").permitAll()
                            .requestMatchers("/api/contacts/submit").permitAll()
                            .requestMatchers("/h2-console/**").permitAll()
                            .requestMatchers("/actuator/**").permitAll()
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            .anyRequest().authenticated()
            );

    http.authenticationProvider(authenticationProvider());

    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    http.headers(headers -> headers.frameOptions(
            HeadersConfigurer.FrameOptionsConfig::sameOrigin));

    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web.ignoring().requestMatchers(
            "/v2/api-docs",
            "/configuration/ui",
            "/swagger-resources/**",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**"
    );
  }

  @Bean
  public CommandLineRunner initData(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
    return args -> {
      // Retrieve or create roles
      Role doctorRole = roleRepository.findByRoleName(AppRole.ROLE_DENTIST)
              .orElseGet(() -> {
                Role newDoctorRole = new Role(AppRole.ROLE_DENTIST);
                return roleRepository.save(newDoctorRole);
              });

      Role receiptoinistRole = roleRepository.findByRoleName(AppRole.ROLE_RECEPTIONIST)
              .orElseGet(() -> {
                Role newReceiptoinistRole = new Role(AppRole.ROLE_RECEPTIONIST);
                return roleRepository.save(newReceiptoinistRole);
              });

      Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
              .orElseGet(() -> {
                Role newAdminRole = new Role(AppRole.ROLE_ADMIN);
                return roleRepository.save(newAdminRole);
              });

      Set<Role> doctorRoles = Set.of(doctorRole);
      Set<Role> receptionistRoles = Set.of(receiptoinistRole);
      Set<Role> adminRoles = Set.of(doctorRole, receiptoinistRole, adminRole);

      // Create users if not already present, using environment variables without defaults
      if (!userRepository.existsByUserName(doctorUsername)) {
        User doctor = new User(doctorUsername, doctorEmail, passwordEncoder.encode(doctorPassword));
        userRepository.save(doctor);
      }

      if (!userRepository.existsByUserName(receptionistUsername)) {
        User receptionist = new User(receptionistUsername, receptionistEmail, passwordEncoder.encode(receptionistPassword));
        userRepository.save(receptionist);
      }

      if (!userRepository.existsByUserName(adminUsername)) {
        User admin = new User(adminUsername, adminEmail, passwordEncoder.encode(adminPassword));
        userRepository.save(admin);
      }

      // Update roles for existing users
      userRepository.findByUserName(doctorUsername).ifPresent(doctor -> {
        doctor.setRoles(doctorRoles);
        userRepository.save(doctor);
      });

      userRepository.findByUserName(receptionistUsername).ifPresent(receptionist -> {
        receptionist.setRoles(receptionistRoles);
        userRepository.save(receptionist);
      });

      userRepository.findByUserName(adminUsername).ifPresent(admin -> {
        admin.setRoles(adminRoles);
        userRepository.save(admin);
      });
    };
  }
}