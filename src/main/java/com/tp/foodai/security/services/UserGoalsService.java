package com.tp.foodai.security.services;

import com.tp.foodai.security.dtos.UserGoalsRequestDto;
import com.tp.foodai.security.dtos.UserGoalsResponseDto;

public interface UserGoalsService {

    UserGoalsResponseDto getGoals(String firebaseUid);

    UserGoalsResponseDto updateGoals(String firebaseUid, UserGoalsRequestDto requestDto);
}
