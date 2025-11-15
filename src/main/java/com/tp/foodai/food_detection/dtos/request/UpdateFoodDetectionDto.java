package com.tp.foodai.food_detection.dtos.request;

import com.tp.foodai.food_detection.value_objects.FoodCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFoodDetectionDto {
    
    @NotBlank(message = "Food name is required")
    private String foodName;
    
    @NotNull(message = "Category is required")
    private FoodCategory category;
    
    @NotNull(message = "Detection date is required")
    private LocalDate detectionDate;
    
    @NotNull(message = "Components are required")
    private List<UpdateComponentDto> components;
}
