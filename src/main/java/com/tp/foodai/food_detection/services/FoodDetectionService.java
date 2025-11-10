package com.tp.foodai.food_detection.services;

import com.tp.foodai.food_detection.dtos.request.UpdateComponentQuantityDto;
import com.tp.foodai.food_detection.dtos.response.DetectionHistoryDto;
import com.tp.foodai.food_detection.dtos.response.FoodDetectionResponseDto;
import com.tp.foodai.food_detection.value_objects.FoodCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;

public interface FoodDetectionService {
    
    /**
     * Analiza una imagen de comida: sube a Azure, detecta alimentos con IA,
     * enriquece con datos nutricionales y guarda en BD.
     * Asigna 100g por defecto a cada alimento detectado.
     */
    FoodDetectionResponseDto analyzeFood(
        MultipartFile file, 
        String firebaseUid, 
        FoodCategory category,
        Date detectionDate
    );
    
    Page<DetectionHistoryDto> getUserHistory(
        String firebaseUid, 
        FoodCategory category,
        Date startDate,
        Date endDate,
        Pageable pageable
    );
    
    FoodDetectionResponseDto getDetectionById(Long id, String firebaseUid);
    
    FoodDetectionResponseDto updateComponentQuantity(
        Long detectionId, 
        Long componentId, 
        String firebaseUid,
        UpdateComponentQuantityDto updateDto
    );
    
    void deleteDetection(Long id, String firebaseUid);
}
