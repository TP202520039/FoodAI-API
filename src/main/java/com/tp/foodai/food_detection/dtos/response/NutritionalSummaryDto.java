package com.tp.foodai.food_detection.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionalSummaryDto {
    
    private Double totalCalories;
    private Double totalProtein;
    private Double totalFat;
    private Double totalCarbs;
}
