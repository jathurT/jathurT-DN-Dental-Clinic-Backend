package com.uor.eng.payload.other;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDTO {
    private Long id;
    private String name;
    private String email;
    private Integer rating;
    private String comments;
    private Boolean showOnWebsite;
}
