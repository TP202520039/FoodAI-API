package com.tp.foodai.food_detection.repositories;

import com.tp.foodai.food_detection.entities.FoodComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodComponentRepository extends JpaRepository<FoodComponent, Long> {
    
    // Buscar componentes de una detección específica
    List<FoodComponent> findByFoodDetectionId(Long foodDetectionId);
    
    // Buscar componente por ID y verificar que pertenece a una detección del usuario
    @Query("SELECT fc FROM FoodComponent fc " +
           "JOIN fc.foodDetection fd " +
           "WHERE fc.id = :componentId " +
           "AND fd.firebaseUid = :firebaseUid")
    Optional<FoodComponent> findByIdAndUserFirebaseUid(
        @Param("componentId") Long componentId,
        @Param("firebaseUid") String firebaseUid
    );
    
    // Eliminar componentes de una detección
    void deleteByFoodDetectionId(Long foodDetectionId);
}
