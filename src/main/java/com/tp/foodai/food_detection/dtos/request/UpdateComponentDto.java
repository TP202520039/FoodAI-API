package com.tp.foodai.food_detection.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateComponentDto {
    
    @NotNull(message = "Component ID is required")
    private Long id;
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Double quantityGrams;
}
