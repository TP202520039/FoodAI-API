package com.tp.foodai.food_detection.dtos.response;

import com.tp.foodai.food_detection.value_objects.FoodCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectionHistoryDto {
    
    private Long id;
    private String foodName;
    private String imageUrl;
    private FoodCategory category;
    private Date detectionDate;
    private Integer componentsCount;
    private NutritionalSummaryDto totals;
}
