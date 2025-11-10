package com.tp.foodai.food_detection.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodComponentResponseDto {
    
    private Long id;
    private String foodName;
    private Double quantityGrams;
    private Double confidenceScore;
    private NutritionalInfoDto nutritionalInfo;
    private Boolean nutritionalDataFound; // true si se encontró en BD, false si es null
}
