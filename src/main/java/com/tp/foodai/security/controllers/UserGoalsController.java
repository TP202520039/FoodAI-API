package com.tp.foodai.security.controllers;

import com.tp.foodai.security.dtos.UserGoalsRequestDto;
import com.tp.foodai.security.dtos.UserGoalsResponseDto;
import com.tp.foodai.security.entities.User;
import com.tp.foodai.security.services.UserGoalsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user/goals")
@Tag(name = "User Goals", description = "Endpoints para configurar metas diarias del usuario")
@SecurityRequirement(name = "Bearer Authentication")
public class UserGoalsController {

    private final UserGoalsService userGoalsService;

    public UserGoalsController(UserGoalsService userGoalsService) {
        this.userGoalsService = userGoalsService;
    }

    @GetMapping
    @Operation(summary = "Obtener metas diarias", description = "Retorna las metas diarias del usuario autenticado")
    public ResponseEntity<UserGoalsResponseDto> getGoals(@AuthenticationPrincipal User user) {
        UserGoalsResponseDto goals = userGoalsService.getGoals(user.getFirebaseUid());
        return ResponseEntity.ok(goals);
    }

    @PutMapping
    @Operation(summary = "Actualizar metas diarias", description = "Actualiza calorias y macronutrientes del usuario autenticado")
    public ResponseEntity<UserGoalsResponseDto> updateGoals(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserGoalsRequestDto requestDto) {
        UserGoalsResponseDto goals = userGoalsService.updateGoals(user.getFirebaseUid(), requestDto);
        return ResponseEntity.ok(goals);
    }
}
