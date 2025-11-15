package com.tp.foodai.food_detection.controllers;

import com.tp.foodai.food_detection.dtos.request.UpdateComponentQuantityDto;
import com.tp.foodai.food_detection.dtos.request.UpdateFoodDetectionDto;
import com.tp.foodai.food_detection.dtos.response.DetectionHistoryDto;
import com.tp.foodai.food_detection.dtos.response.FoodDetectionGroupedResponseDto;
import com.tp.foodai.food_detection.dtos.response.FoodDetectionResponseDto;
import com.tp.foodai.food_detection.exceptions.InvalidImageFormatException;
import com.tp.foodai.food_detection.services.FoodDetectionService;
import com.tp.foodai.food_detection.value_objects.FoodCategory;
import com.tp.foodai.security.entities.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Tag(name = "Food Detection", description = "Endpoints para análisis de comida con IA")
@RestController
@RequestMapping("/api/v1/food-detections")
@SecurityRequirement(name = "Bearer Authentication")
public class FoodDetectionController {

    private static final List<String> ALLOWED_FORMATS = Arrays.asList("image/jpeg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final FoodDetectionService foodDetectionService;

    public FoodDetectionController(FoodDetectionService foodDetectionService) {
        this.foodDetectionService = foodDetectionService;
    }

    @Operation(summary = "Analizar imagen de comida", 
               description = "Sube una imagen, detecta alimentos con IA, asigna 100g por defecto y calcula valores nutricionales")
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodDetectionResponseDto> analyzeFoodImage(
            @RequestPart("image") MultipartFile image,
            @RequestParam("category") FoodCategory category,
            @RequestParam("detectionDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate detectionDate,
            Authentication authentication) {

        // Validar imagen
        validateImage(image);

        // Obtener firebaseUid del usuario autenticado (ya validado por FirebaseAuthFilter)
        User user = (User) authentication.getPrincipal();
        String firebaseUid = user.getFirebaseUid();
        
        System.out.println("User " + firebaseUid + " is analyzing a food image");

        FoodDetectionResponseDto response = foodDetectionService.analyzeFood(
                image, firebaseUid, category, java.sql.Date.valueOf(detectionDate));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Listar detecciones por usuario y fecha agrupados por categoría",
               description = "Obtiene todas las detecciones realizadas por el usuario en una fecha específica")
    @GetMapping("/group-by-category")
    public ResponseEntity<List<FoodDetectionGroupedResponseDto>> getDetectionsByUserAndDate(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        String firebaseUid = user.getFirebaseUid();

        List<FoodDetectionGroupedResponseDto> detections = foodDetectionService.getDetectionsByUserAndDate(
                firebaseUid, java.sql.Date.valueOf(date));

        return ResponseEntity.ok(detections);
    }

    @Operation(summary = "Obtener historial de detecciones",
               description = "Lista todas las detecciones del usuario con filtros opcionales")
    @GetMapping("/history")
    public ResponseEntity<Page<DetectionHistoryDto>> getUserHistory(
            @RequestParam(required = false) FoodCategory category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        String firebaseUid = user.getFirebaseUid();
        Pageable pageable = PageRequest.of(page, size);

        Page<DetectionHistoryDto> history = foodDetectionService.getUserHistory(
                firebaseUid, category, 
                startDate != null ? java.sql.Date.valueOf(startDate) : null,
                endDate != null ? java.sql.Date.valueOf(endDate) : null,
                pageable);

        return ResponseEntity.ok(history);
    }

    @Operation(summary = "Obtener detección por ID",
               description = "Obtiene el detalle completo de una detección específica")
    @GetMapping("/{id}")
    public ResponseEntity<FoodDetectionResponseDto> getDetectionById(
            @PathVariable Long id,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        String firebaseUid = user.getFirebaseUid();
        FoodDetectionResponseDto detection = foodDetectionService.getDetectionById(id, firebaseUid);

        return ResponseEntity.ok(detection);
    }

    @Operation(summary = "Actualizar cantidad de un componente",
               description = "Permite al usuario editar la cantidad en gramos de un alimento detectado")
    @PatchMapping("/{detectionId}/components/{componentId}")
    public ResponseEntity<FoodDetectionResponseDto> updateComponentQuantity(
            @PathVariable Long detectionId,
            @PathVariable Long componentId,
            @Valid @RequestBody UpdateComponentQuantityDto updateDto,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        String firebaseUid = user.getFirebaseUid();
        FoodDetectionResponseDto updated = foodDetectionService.updateComponentQuantity(
                detectionId, componentId, firebaseUid, updateDto);

        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Actualizar detección completa",
               description = "Permite actualizar el nombre, categoría, fecha y componentes de una detección")
    @PutMapping("/{id}")
    public ResponseEntity<FoodDetectionResponseDto> updateDetection(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFoodDetectionDto updateDto,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        String firebaseUid = user.getFirebaseUid();
        FoodDetectionResponseDto updated = foodDetectionService.updateDetection(
                id, firebaseUid, updateDto);

        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Eliminar detección",
               description = "Elimina una detección y su imagen de Azure Blob Storage")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDetection(
            @PathVariable Long id,
            Authentication authentication) {

        User user = (User) authentication.getPrincipal();
        String firebaseUid = user.getFirebaseUid();
        foodDetectionService.deleteDetection(id, firebaseUid);

        return ResponseEntity.noContent().build();
    }

    // --- Métodos auxiliares ---

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageFormatException("La imagen no puede estar vacía");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidImageFormatException(
                    "La imagen excede el tamaño máximo permitido de 5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_FORMATS.contains(contentType)) {
            throw new InvalidImageFormatException(
                    "Formato de imagen no válido. Formatos permitidos: JPG, PNG, WEBP");
        }
    }
}
