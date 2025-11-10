package com.tp.foodai.food_detection.repositories;

import com.tp.foodai.food_detection.entities.FoodDetection;
import com.tp.foodai.food_detection.value_objects.FoodCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.Optional;

@Repository
public interface FoodDetectionRepository extends JpaRepository<FoodDetection, Long> {
    
    // Buscar todas las detecciones de un usuario
    Page<FoodDetection> findByFirebaseUidOrderByDetectionDateDesc(String firebaseUid, Pageable pageable);
    
    // Buscar por usuario y categoría
    Page<FoodDetection> findByFirebaseUidAndCategoryOrderByDetectionDateDesc(
        String firebaseUid, 
        FoodCategory category, 
        Pageable pageable
    );
    
    // Buscar por usuario en un rango de fechas
    @Query("SELECT fd FROM FoodDetection fd WHERE fd.firebaseUid = :firebaseUid " +
           "AND fd.detectionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY fd.detectionDate DESC")
    Page<FoodDetection> findByFirebaseUidAndDateRange(
        @Param("firebaseUid") String firebaseUid,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate,
        Pageable pageable
    );
    
    // Buscar por usuario, categoría y rango de fechas
    @Query("SELECT fd FROM FoodDetection fd WHERE fd.firebaseUid = :firebaseUid " +
           "AND fd.category = :category " +
           "AND fd.detectionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY fd.detectionDate DESC")
    Page<FoodDetection> findByFirebaseUidAndCategoryAndDateRange(
        @Param("firebaseUid") String firebaseUid,
        @Param("category") FoodCategory category,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate,
        Pageable pageable
    );
    
    // Buscar detección por ID y usuario (para validar ownership)
    Optional<FoodDetection> findByIdAndFirebaseUid(Long id, String firebaseUid);
    
    // Contar detecciones de un usuario
    long countByFirebaseUid(String firebaseUid);
    
    // Obtener última detección de un usuario
    Optional<FoodDetection> findFirstByFirebaseUidOrderByDetectionDateDesc(String firebaseUid);
}
