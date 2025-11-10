package com.tp.foodai.food_detection.entities;

import com.tp.foodai.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class FoodComponent extends AuditableAbstractAggregateRoot<FoodComponent> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_detection_id", nullable = false)
    private FoodDetection foodDetection;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutrional_data_id", nullable = false)
    private NutritionalData nutritionalData;

    @Column(name = "quantity", nullable = false)
    private Double quantity; // in grams

    @Column(name = "confidence_score", nullable = false)
    private Double confidence_score;

    // --- Cálculos de valores nutricionales con regla de tres ---
    public double getCaloriesForQuantity() {
        return ruleOfThree(nutritionalData.getCalories(), quantity);
    }

    public double getProteinForQuantity() {
        return ruleOfThree(nutritionalData.getProtein(), quantity);
    }

    public double getFatForQuantity() {
        return ruleOfThree(nutritionalData.getFat(), quantity);
    }

    public double getCarbsForQuantity() {
        return ruleOfThree(nutritionalData.getCarbohydrates(), quantity);
    }

    private double ruleOfThree(double per100gValue, double grams) {
        if (per100gValue <= 0 || grams <= 0) return 0.0;
        return (grams / 100.0) * per100gValue;
    }

}