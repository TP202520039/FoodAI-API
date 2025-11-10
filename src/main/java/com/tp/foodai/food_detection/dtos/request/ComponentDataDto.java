package com.tp.foodai.food_detection.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComponentDataDto {
    
    @NotNull(message = "Food name is required")
    private String foodName;
    
    @NotNull(message = "Quantity is required")
    private Double quantityGrams;
}
