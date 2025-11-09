package com.tp.foodai.security.services;

import com.tp.foodai.security.dtos.UserResponseDto;
import com.tp.foodai.security.entities.User;

public interface UserService {
    UserResponseDto verifyAndSyncUser(String idToken);
    User findByFirebaseUid(String firebaseUid);
    User verifyTokenAndGetUser(String idToken);
}
