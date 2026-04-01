package com.tp.foodai.security.services.impl;

import com.tp.foodai.security.dtos.UserGoalsRequestDto;
import com.tp.foodai.security.dtos.UserGoalsResponseDto;
import com.tp.foodai.security.entities.UserGoals;
import com.tp.foodai.security.repositories.UserGoalsRepository;
import com.tp.foodai.security.services.UserGoalsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserGoalsServiceImpl implements UserGoalsService {

    private final UserGoalsRepository userGoalsRepository;

    public UserGoalsServiceImpl(UserGoalsRepository userGoalsRepository) {
        this.userGoalsRepository = userGoalsRepository;
    }

    @Override
    @Transactional
    public UserGoalsResponseDto getGoals(String firebaseUid) {
        UserGoals userGoals = userGoalsRepository.findByFirebaseUid(firebaseUid)
                .orElseGet(() -> userGoalsRepository.save(UserGoals.builder()
                        .firebaseUid(firebaseUid)
                        .build()));

        return mapToDto(userGoals);
    }

    @Override
    @Transactional
    public UserGoalsResponseDto updateGoals(String firebaseUid, UserGoalsRequestDto requestDto) {
        UserGoals userGoals = userGoalsRepository.findByFirebaseUid(firebaseUid)
                .orElseGet(() -> UserGoals.builder().firebaseUid(firebaseUid).build());

        userGoals.setDailyCaloriesGoal(requestDto.getDailyCaloriesGoal());
        userGoals.setDailyProteinGoal(requestDto.getDailyProteinGoal());
        userGoals.setDailyFatGoal(requestDto.getDailyFatGoal());
        userGoals.setDailyCarbsGoal(requestDto.getDailyCarbsGoal());

        UserGoals savedGoals = userGoalsRepository.save(userGoals);
        return mapToDto(savedGoals);
    }

    private UserGoalsResponseDto mapToDto(UserGoals userGoals) {
        return UserGoalsResponseDto.builder()
                .id(userGoals.getId())
                .firebaseUid(userGoals.getFirebaseUid())
                .dailyCaloriesGoal(userGoals.getDailyCaloriesGoal())
                .dailyProteinGoal(userGoals.getDailyProteinGoal())
                .dailyFatGoal(userGoals.getDailyFatGoal())
                .dailyCarbsGoal(userGoals.getDailyCarbsGoal())
                .createdAt(userGoals.getCreatedAt())
                .updatedAt(userGoals.getUpdatedAt())
                .build();
    }
}
