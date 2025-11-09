package com.tp.foodai.security.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    
    private Long id;
    private String firebaseUid;
    private String email;
    private String displayName;
    private String photoUrl;
    private String provider;
    private Boolean isActive;
}
