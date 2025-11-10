package com.tp.foodai.food_detection.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionalInfoDto {
    
    private Double calories;
    private Double protein;
    private Double fat;
    private Double carbs;
    private Double fiber;
    private Double iron;
    private Double calcium;
    private Double vitaminC;
    private Double zinc;
    private Double potassium;
    private Double folicAcid;
}
