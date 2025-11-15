package com.tp.foodai.food_detection.services.impl;

import com.tp.foodai.food_detection.dtos.external.AiDetectionResponseDto;
import com.tp.foodai.food_detection.dtos.external.DetectedFoodDto;
import com.tp.foodai.food_detection.dtos.request.UpdateComponentQuantityDto;
import com.tp.foodai.food_detection.dtos.request.UpdateFoodDetectionDto;
import com.tp.foodai.food_detection.dtos.request.UpdateComponentDto;
import com.tp.foodai.food_detection.dtos.response.DetectionHistoryDto;
import com.tp.foodai.food_detection.dtos.response.FoodDetectionGroupedResponseDto;
import com.tp.foodai.food_detection.dtos.response.FoodDetectionResponseDto;
import com.tp.foodai.food_detection.entities.FoodComponent;
import com.tp.foodai.food_detection.entities.FoodDetection;
import com.tp.foodai.food_detection.entities.NutritionalData;
import com.tp.foodai.food_detection.mappers.FoodDetectionMapper;
import com.tp.foodai.food_detection.repositories.FoodComponentRepository;
import com.tp.foodai.food_detection.repositories.FoodDetectionRepository;
import com.tp.foodai.food_detection.repositories.NutritionalDataRepository;
import com.tp.foodai.food_detection.services.AiDetectionService;
import com.tp.foodai.food_detection.services.AzureBlobStorageService;
import com.tp.foodai.food_detection.services.FoodDetectionService;
import com.tp.foodai.food_detection.value_objects.FoodCategory;
import com.tp.foodai.shared.domain.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FoodDetectionServiceImpl implements FoodDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(FoodDetectionServiceImpl.class);

    private final FoodDetectionRepository foodDetectionRepository;
    private final FoodComponentRepository foodComponentRepository;
    private final NutritionalDataRepository nutritionalDataRepository;
    private final AzureBlobStorageService azureBlobStorageService;
    private final AiDetectionService aiDetectionService;
    private final FoodDetectionMapper mapper;

    public FoodDetectionServiceImpl(FoodDetectionRepository foodDetectionRepository,
                                     FoodComponentRepository foodComponentRepository,
                                     NutritionalDataRepository nutritionalDataRepository,
                                     AzureBlobStorageService azureBlobStorageService,
                                     AiDetectionService aiDetectionService,
                                     FoodDetectionMapper mapper) {
        this.foodDetectionRepository = foodDetectionRepository;
        this.foodComponentRepository = foodComponentRepository;
        this.nutritionalDataRepository = nutritionalDataRepository;
        this.azureBlobStorageService = azureBlobStorageService;
        this.aiDetectionService = aiDetectionService;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public FoodDetectionResponseDto analyzeFood(MultipartFile file, 
                                                String firebaseUid, 
                                                FoodCategory category,
                                                Date detectionDate) {
        try {
            logger.info("Starting food analysis for user: {}", firebaseUid);

            // 1. Subir imagen a Azure Blob Storage
            logger.info("Uploading image to Azure Blob Storage...");
            String imageUrl = azureBlobStorageService.uploadImage(file, firebaseUid);

            // 2. Llamar a la API de detección de alimentos
            logger.info("Calling AI Detection API...");
            AiDetectionResponseDto aiResponse = aiDetectionService.detectFood(imageUrl);

            // 3. Verificar si se detectaron alimentos
            if (aiResponse.getDetectedFoods() == null || aiResponse.getDetectedFoods().isEmpty()) {
                logger.warn("No foods detected in the image for user: {}", firebaseUid);
                
                // Crear una detección vacía para mantener registro
                FoodDetection emptyDetection = FoodDetection.builder()
                        .firebaseUid(firebaseUid)
                        .foodName("Sin alimentos detectados")
                        .imageUrl(imageUrl)
                        .category(category)
                        .detectionDate(detectionDate)
                        .components(new ArrayList<>())
                        .totalCalories(0.0)
                        .totalProtein(0.0)
                        .totalFat(0.0)
                        .totalCarbs(0.0)
                        .build();
                
                FoodDetection savedDetection = foodDetectionRepository.save(emptyDetection);
                logger.info("Empty detection saved with ID: {}", savedDetection.getId());
                
                return mapper.toResponseDto(savedDetection);
            }

            // 4. Crear la entidad FoodDetection
            String foodName = generateFoodName(aiResponse.getDetectedFoods());
            
            FoodDetection foodDetection = FoodDetection.builder()
                    .firebaseUid(firebaseUid)
                    .foodName(foodName)
                    .imageUrl(imageUrl)
                    .category(category)
                    .detectionDate(detectionDate)
                    .components(new ArrayList<>())
                    .build();

            // 5. Crear los FoodComponents con 100g por defecto
            logger.info("Enriching detected foods with nutritional data...");
            List<FoodComponent> components = enrichComponents(
                    aiResponse.getDetectedFoods(), 
                    foodDetection
            );

            foodDetection.setComponents(components);

            // 6. Los totales se calculan automáticamente con @PrePersist
            FoodDetection savedDetection = foodDetectionRepository.save(foodDetection);

            logger.info("Food detection completed successfully. ID: {}", savedDetection.getId());

            return mapper.toResponseDto(savedDetection);

        } catch (Exception e) {
            logger.error("Error during food analysis", e);
            throw e;
        }
    }

    @Override
    public List<FoodDetectionGroupedResponseDto> getDetectionsByUserAndDate(String firebaseUid, Date detectionDate) {
        
        List<FoodDetection> detections = foodDetectionRepository.findByFirebaseUidAndDetectionDate(firebaseUid, detectionDate);
        
        // Agrupar por categoría
        Map<String, List<FoodDetectionResponseDto>> grouped = detections.stream()
                .map(mapper::toResponseDto)
                .collect(Collectors.groupingBy(dto -> dto.getCategory().toString()));
        
        // Construir la lista de DTOs agrupados
        List<FoodDetectionGroupedResponseDto> result = new ArrayList<>();

        // 🔹 Recorremos todas las categorías del enum
        for (FoodCategory category : FoodCategory.values()) {
            List<FoodDetectionResponseDto> detectionDtos = grouped.getOrDefault(category.toString(), new ArrayList<>());

            FoodDetectionGroupedResponseDto groupedDto = FoodDetectionGroupedResponseDto.builder()
                    .category(category.name()) // o category.toString() si prefieres
                    .count((long) detectionDtos.size())
                    .items(detectionDtos)
                    .build();

            result.add(groupedDto);
        }
        
        return result;
    }

    @Override
    public Page<DetectionHistoryDto> getUserHistory(String firebaseUid, 
                                                     FoodCategory category,
                                                     Date startDate, 
                                                     Date endDate, 
                                                     Pageable pageable) {
        Page<FoodDetection> detections;

        if (category != null && startDate != null && endDate != null) {
            detections = foodDetectionRepository.findByFirebaseUidAndCategoryAndDateRange(
                    firebaseUid, category, startDate, endDate, pageable);
        } else if (category != null) {
            detections = foodDetectionRepository.findByFirebaseUidAndCategoryOrderByDetectionDateDesc(
                    firebaseUid, category, pageable);
        } else if (startDate != null && endDate != null) {
            detections = foodDetectionRepository.findByFirebaseUidAndDateRange(
                    firebaseUid, startDate, endDate, pageable);
        } else {
            detections = foodDetectionRepository.findByFirebaseUidOrderByDetectionDateDesc(
                    firebaseUid, pageable);
        }

        return detections.map(mapper::toHistoryDto);
    }

    @Override
    public FoodDetectionResponseDto getDetectionById(Long id, String firebaseUid) {
        FoodDetection detection = foodDetectionRepository.findByIdAndFirebaseUid(id, firebaseUid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Food detection not found with id: " + id));

        return mapper.toResponseDto(detection);
    }

    @Override
    @Transactional
    public FoodDetectionResponseDto updateComponentQuantity(Long detectionId, 
                                                            Long componentId, 
                                                            String firebaseUid,
                                                            UpdateComponentQuantityDto updateDto) {
        // Verificar que la detección pertenece al usuario
        FoodDetection detection = foodDetectionRepository.findByIdAndFirebaseUid(detectionId, firebaseUid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Food detection not found with id: " + detectionId));

        // Buscar el componente
        FoodComponent component = foodComponentRepository.findByIdAndUserFirebaseUid(componentId, firebaseUid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Food component not found with id: " + componentId));

        // Actualizar la cantidad
        component.setQuantity(updateDto.getQuantityGrams());
        foodComponentRepository.save(component);

        // Los totales se recalculan automáticamente con @PreUpdate
        detection.recalculateTotals();
        FoodDetection updatedDetection = foodDetectionRepository.save(detection);

        logger.info("Updated component {} quantity to {} grams", componentId, updateDto.getQuantityGrams());

        return mapper.toResponseDto(updatedDetection);
    }

    @Override
    @Transactional
    public FoodDetectionResponseDto updateDetection(Long id, 
                                                    String firebaseUid,
                                                    UpdateFoodDetectionDto updateDto) {
        // Verificar que la detección pertenece al usuario
        FoodDetection detection = foodDetectionRepository.findByIdAndFirebaseUid(id, firebaseUid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Food detection not found with id: " + id));

        logger.info("Updating food detection ID: {} for user: {}", id, firebaseUid);

        // Actualizar campos básicos
        detection.setFoodName(updateDto.getFoodName());
        detection.setCategory(updateDto.getCategory());
        detection.setDetectionDate(java.sql.Date.valueOf(updateDto.getDetectionDate()));

        // Actualizar cantidades de componentes especificados
        for (UpdateComponentDto componentDto : updateDto.getComponents()) {
            // Buscar el componente que pertenece a esta detección
            FoodComponent component = detection.getComponents().stream()
                    .filter(c -> c.getId().equals(componentDto.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Component with id " + componentDto.getId() + 
                            " not found in detection " + id));

            // Actualizar solo la cantidad
            component.setQuantity(componentDto.getQuantityGrams());
            logger.info("Updated component {} quantity to {} grams", 
                       componentDto.getId(), componentDto.getQuantityGrams());
        }

        // Los totales se calculan automáticamente con @PreUpdate
        detection.recalculateTotals();
        FoodDetection updatedDetection = foodDetectionRepository.save(detection);

        logger.info("Successfully updated food detection ID: {}", id);

        return mapper.toResponseDto(updatedDetection);
    }

    @Override
    @Transactional
    public void deleteDetection(Long id, String firebaseUid) {
        FoodDetection detection = foodDetectionRepository.findByIdAndFirebaseUid(id, firebaseUid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Food detection not found with id: " + id));

        // Eliminar imagen de Azure Blob Storage
        azureBlobStorageService.deleteImage(detection.getImageUrl());

        // Eliminar de BD (los componentes se eliminan en cascada)
        foodDetectionRepository.delete(detection);

        logger.info("Deleted food detection with id: {}", id);
    }

    // --- Métodos auxiliares ---

    private List<FoodComponent> enrichComponents(List<DetectedFoodDto> detectedFoods,
                                                  FoodDetection foodDetection) {

        logger.info("Enriching {} detected foods with nutritional data", detectedFoods.size());
        logger.info("Detected foods: {}", detectedFoods);
                                                    
        return detectedFoods.stream().map(detected -> {
            // Buscar datos nutricionales en la BD
            NutritionalData nutritionalData = nutritionalDataRepository
                    .findByFoodNameIgnoreCase(detected.getName())
                    .orElse(null);

            if (nutritionalData == null) {
                logger.warn("Nutritional data not found for: {}", detected.getName());
            }

            // Crear el componente con 100g por defecto (puede tener nutritionalData null)
            return FoodComponent.builder()
                    .foodDetection(foodDetection)
                    .nutritionalData(nutritionalData)
                    .quantity(100.0) // Por defecto 100g
                    .confidence_score(detected.getConfidence())
                    .build();

        }).collect(Collectors.toList());
    }

    private String generateFoodName(List<DetectedFoodDto> detectedFoods) {
        if (detectedFoods.isEmpty()) {
            return "Plato desconocido";
        }

        if (detectedFoods.size() == 1) {
            return capitalize(detectedFoods.get(0).getName());
        }

        // Si hay múltiples alimentos, concatenar los primeros 3
        String names = detectedFoods.stream()
                .limit(3)
                .map(DetectedFoodDto::getName)
                .map(this::capitalize)
                .collect(Collectors.joining(", "));

        if (detectedFoods.size() > 3) {
            names += " y más";
        }

        return names;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

}
