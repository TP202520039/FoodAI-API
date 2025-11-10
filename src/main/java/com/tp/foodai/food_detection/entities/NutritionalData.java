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

    // Valores por 100 g
    private Integer calories; // in kcal
    private Double protein; // in grams
    private Double fat;     // in grams
    private Double carbohydrates; // in grams
    private Double fiber;  // in grams
    private Double iron;  // in mg
    private Double calcium; // in mg
    private Double vitaminC; // in mg
    private Double zinc; // in mg
    private Double potassium; // in mg
    private Double folicAcid; // in ug
}
