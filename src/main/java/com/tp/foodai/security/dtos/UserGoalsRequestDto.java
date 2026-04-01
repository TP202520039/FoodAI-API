package com.tp.foodai.security.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGoalsRequestDto {

    @NotNull(message = "Daily calories goal is required")
    @Min(value = 500, message = "Daily calories goal must be at least 500 kcal")
    @Max(value = 5000, message = "Daily calories goal must be at most 5000 kcal")
    private Integer dailyCaloriesGoal;

    @NotNull(message = "Daily protein goal is required")
    @Min(value = 10, message = "Daily protein goal must be at least 10 g")
    @Max(value = 400, message = "Daily protein goal must be at most 400 g")
    private Integer dailyProteinGoal;

    @NotNull(message = "Daily fat goal is required")
    @Min(value = 10, message = "Daily fat goal must be at least 10 g")
    @Max(value = 300, message = "Daily fat goal must be at most 300 g")
    private Integer dailyFatGoal;

    @NotNull(message = "Daily carbs goal is required")
    @Min(value = 10, message = "Daily carbs goal must be at least 10 g")
    @Max(value = 600, message = "Daily carbs goal must be at most 600 g")
    private Integer dailyCarbsGoal;
}
