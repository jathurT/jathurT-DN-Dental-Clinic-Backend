package com.uor.eng.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "feedbacks")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Feedback {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank(message = "Name is required")
  @Size(max = 50, message = "Name should not exceed 50 characters")
  private String name;

  @NotBlank(message = "Email is required")
  @Email(message = "Please provide a valid email address")
  private String email;

  @NotNull(message = "Rating is required")
  @Max(value = 5, message = "Rating should not be more than 5")
  private Integer rating;

  @Size(max = 500, message = "Comments should not exceed 500 characters")
  private String comments;

  private Boolean showOnWebsite = false;

  public Feedback(Long id,String name, String email, Integer rating, String comments) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.rating = rating;
    this.comments = comments;
    this.showOnWebsite = false;
  }
}
