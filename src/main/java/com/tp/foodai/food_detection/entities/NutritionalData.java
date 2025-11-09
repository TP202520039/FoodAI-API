package com.tp.foodai.food_detection.entities;

import com.tp.foodai.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class NutritionalData extends AuditableAbstractAggregateRoot<NutritionalData> {

    private String foodName;
    private Integer calories;
    private Double protein; // in grams
    private Double fat;     // in grams
    private Double carbohydrates; // in grams
    
}
