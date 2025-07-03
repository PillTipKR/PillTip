package com.oauth2.User.dto;

import com.oauth2.User.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfilePregnantResponse {
    private Integer age;
    private Gender gender;
    private boolean pregnant;
} 