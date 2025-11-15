package com.tp.foodai.food_detection.mappers;

import com.tp.foodai.food_detection.dtos.response.*;
import com.tp.foodai.food_detection.entities.FoodComponent;
import com.tp.foodai.food_detection.entities.FoodDetection;
import com.tp.foodai.food_detection.entities.NutritionalData;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class FoodDetectionMapper {

    public FoodDetectionResponseDto toResponseDto(FoodDetection foodDetection) {
        return FoodDetectionResponseDto.builder()
                .id(foodDetection.getId())
                .foodName(foodDetection.getFoodName())
                .imageUrl(foodDetection.getImageUrl())
                .category(foodDetection.getCategory())
                .detectionDate(foodDetection.getDetectionDate())
                .components(foodDetection.getComponents().stream()
                        .map(this::toComponentResponseDto)
                        .collect(Collectors.toList()))
                .totals(toNutritionalSummaryDto(foodDetection))
                .createdAt(foodDetection.getCreatedAt())
                .updatedAt(foodDetection.getUpdatedAt())
                .build();
    }

    public DetectionHistoryDto toHistoryDto(FoodDetection foodDetection) {
        return DetectionHistoryDto.builder()
                .id(foodDetection.getId())
                .foodName(foodDetection.getFoodName())
                .imageUrl(foodDetection.getImageUrl())
                .category(foodDetection.getCategory())
                .detectionDate(foodDetection.getDetectionDate())
                .componentsCount(foodDetection.getComponents() != null ? foodDetection.getComponents().size() : 0)
                .totals(toNutritionalSummaryDto(foodDetection))
                .build();
    }

    public FoodComponentResponseDto toComponentResponseDto(FoodComponent component) {
        NutritionalData nutritionalData = component.getNutritionalData();
        
        return FoodComponentResponseDto.builder()
                .id(component.getId())
                .foodName(nutritionalData != null ? nutritionalData.getFoodName() : "Unknown")
                .quantityGrams(component.getQuantity())
                .confidenceScore(component.getConfidence_score())
                .nutritionalInfo(nutritionalData != null ? toNutritionalInfoDto(component) : null)
                .nutritionalDataFound(nutritionalData != null)
                .build();
    }

    private NutritionalInfoDto toNutritionalInfoDto(FoodComponent component) {
        return NutritionalInfoDto.builder()
                .calories(component.getCaloriesForQuantity())
                .protein(component.getProteinForQuantity())
                .fat(component.getFatForQuantity())
                .carbs(component.getCarbsForQuantity())
                .fiber(component.getFiberForQuantity())
                .iron(component.getIronForQuantity())
                .calcium(component.getCalciumForQuantity())
                .vitaminC(component.getVitaminCForQuantity())
                .zinc(component.getZincForQuantity())
                .potassium(component.getPotassiumForQuantity())
                .folicAcid(component.getFolicAcidForQuantity())
                .build();
    }

    private NutritionalSummaryDto toNutritionalSummaryDto(FoodDetection foodDetection) {
        return NutritionalSummaryDto.builder()
                .totalCalories(foodDetection.getTotalCalories())
                .totalProtein(foodDetection.getTotalProtein())
                .totalFat(foodDetection.getTotalFat())
                .totalCarbs(foodDetection.getTotalCarbs())
                .build();
    }
}
