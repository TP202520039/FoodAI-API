package com.tp.foodai.security.controllers;

import com.tp.foodai.security.dtos.AuthRequestDto;
import com.tp.foodai.security.dtos.UserResponseDto;
import com.tp.foodai.security.entities.User;
import com.tp.foodai.security.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints de autenticación con Firebase")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/sync")
    @Operation(summary = "Sincronizar usuario de Firebase", 
               description = "Verifica el token de Firebase y sincroniza/crea el usuario en la base de datos local")
    public ResponseEntity<UserResponseDto> syncUser(@RequestBody AuthRequestDto request) {
        UserResponseDto user = userService.verifyAndSyncUser(request.getIdToken());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener usuario actual", 
               description = "Retorna la información del usuario autenticado")
    public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal User user) {
        UserResponseDto dto = UserResponseDto.builder()
                .id(user.getId())
                .firebaseUid(user.getFirebaseUid())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .photoUrl(user.getPhotoUrl())
                .provider(user.getProvider())
                .isActive(user.getIsActive())
                .build();
        return ResponseEntity.ok(dto);
    }
}
