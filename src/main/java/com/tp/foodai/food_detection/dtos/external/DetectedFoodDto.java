package com.tp.foodai.food_detection.dtos.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectedFoodDto {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("confidence")
    private Double confidence;
}
