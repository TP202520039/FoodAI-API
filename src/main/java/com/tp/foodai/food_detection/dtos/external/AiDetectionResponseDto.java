package com.tp.foodai.food_detection.dtos.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiDetectionResponseDto {
    
    @JsonProperty("detected_foods")
    private List<DetectedFoodDto> detectedFoods;
}
