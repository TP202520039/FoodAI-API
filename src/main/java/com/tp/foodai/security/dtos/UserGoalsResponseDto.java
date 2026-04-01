package com.tp.foodai.security.dtos;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGoalsResponseDto {

    private Long id;
    private String firebaseUid;
    private Integer dailyCaloriesGoal;
    private Integer dailyProteinGoal;
    private Integer dailyFatGoal;
    private Integer dailyCarbsGoal;
    private Date createdAt;
    private Date updatedAt;
}
