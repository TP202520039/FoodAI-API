package com.tp.foodai.food_detection.repositories;

import com.tp.foodai.food_detection.entities.NutritionalData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NutritionalDataRepository extends JpaRepository<NutritionalData, Long> {
    
    // Buscar por nombre exacto (case-insensitive)
    @Query("SELECT nd FROM NutritionalData nd WHERE LOWER(nd.foodName) = LOWER(:foodName)")
    Optional<NutritionalData> findByFoodNameIgnoreCase(@Param("foodName") String foodName);
    
    // Buscar por nombre con LIKE (para búsquedas admin)
    @Query("SELECT nd FROM NutritionalData nd WHERE LOWER(nd.foodName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<NutritionalData> searchByFoodName(@Param("search") String search, Pageable pageable);
    
    // Verificar si existe por nombre
    boolean existsByFoodNameIgnoreCase(String foodName);
}
