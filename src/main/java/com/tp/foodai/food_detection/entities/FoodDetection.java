package com.tp.foodai.food_detection.entities;

import com.tp.foodai.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.sql.Date;
import java.util.List;

import com.tp.foodai.food_detection.value_objects.FoodCategory;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "food_detections")
public class FoodDetection extends AuditableAbstractAggregateRoot<FoodDetection> {
    
    @Column(name = "firebase_uid", unique = true, nullable = false, length = 128)
    private String firebaseUid;

    @Column(name = "food_name", nullable = false, length = 255)
    private String foodName;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private FoodCategory category;

    @Column(name = "detection_date", nullable = false)
    private Date detectionDate;

    @OneToMany(mappedBy = "foodDetection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FoodComponent> components;

    // Campos agregados (opcionales, pero recomendados)
    @Column(name = "total_calories")
    private Double totalCalories;

    @Column(name = "total_protein")
    private Double totalProtein;

    @Column(name = "total_fat")
    private Double totalFat;

    @Column(name = "total_carbs")
    private Double totalCarbs;

        // --- Cálculo de totales ---
    public void recalculateTotals() {
        if (components == null || components.isEmpty()) {
            totalCalories = 0.0;
            totalProtein = 0.0;
            totalFat = 0.0;
            totalCarbs = 0.0;
            return;
        }

        totalCalories = components.stream().mapToDouble(FoodComponent::getCaloriesForQuantity).sum();
        totalProtein  = components.stream().mapToDouble(FoodComponent::getProteinForQuantity).sum();
        totalFat      = components.stream().mapToDouble(FoodComponent::getFatForQuantity).sum();
        totalCarbs    = components.stream().mapToDouble(FoodComponent::getCarbsForQuantity).sum();
    }

    // Se recalculan los totales automáticamente antes de guardar o actualizar
    @PrePersist
    @PreUpdate
    public void beforeSave() {
        recalculateTotals();
    }

}
