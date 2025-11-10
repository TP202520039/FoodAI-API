package com.tp.foodai.food_detection.dtos.response;

import com.tp.foodai.food_detection.value_objects.FoodCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodDetectionResponseDto {
    
    private Long id;
    private String foodName;
    private String imageUrl;
    private FoodCategory category;
    private Date detectionDate;
    private List<FoodComponentResponseDto> components;
    private NutritionalSummaryDto totals;
    private java.util.Date createdAt;
    private java.util.Date updatedAt;
}
