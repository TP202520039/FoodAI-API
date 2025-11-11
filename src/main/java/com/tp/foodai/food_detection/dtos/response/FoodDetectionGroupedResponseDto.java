package com.tp.foodai.food_detection.dtos.response;

import java.util.List;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodDetectionGroupedResponseDto {
    private String category;
    private Long count;
    private List<FoodDetectionResponseDto> items;

}
