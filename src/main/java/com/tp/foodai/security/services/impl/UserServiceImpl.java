package com.tp.foodai.security.services.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.tp.foodai.security.dtos.UserResponseDto;
import com.tp.foodai.security.entities.User;
import com.tp.foodai.security.services.UserService;
import com.tp.foodai.security.repositories.UserRepository;
import com.tp.foodai.shared.domain.exceptions.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Verifica el token de Firebase y sincroniza/actualiza el usuario en la BD local
     */
    @Transactional
    public UserResponseDto verifyAndSyncUser(String idToken) {
        try {
            // Log para debugging
            logger.info("Received token with length: {}", idToken != null ? idToken.length() : 0);
            logger.debug("Token preview: {}...", idToken != null && idToken.length() > 50 
                ? idToken.substring(0, 50) : idToken);
            
            if (idToken == null || idToken.trim().isEmpty()) {
                throw new UnauthorizedException("Token is null or empty");
            }
            
            // 1. Verificar token con Firebase
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            
            // 2. Extraer datos del token
            String firebaseUid = decodedToken.getUid();
            String email = decodedToken.getEmail();
            String name = decodedToken.getName();
            String picture = decodedToken.getPicture();
            
            // Obtener provider del sign_in_provider claim
            String provider = "password"; // default
            if (decodedToken.getClaims().containsKey("firebase")) {
                Object firebaseObj = decodedToken.getClaims().get("firebase");
                if (firebaseObj instanceof java.util.Map) {
                    Object signInProvider = ((java.util.Map<?, ?>) firebaseObj).get("sign_in_provider");
                    if (signInProvider != null) {
                        provider = signInProvider.toString();
                    }
                }
            }
            
            // 3. Buscar o crear usuario en BD local
            User user = userRepository.findByFirebaseUid(firebaseUid)
                    .orElse(User.builder()
                            .firebaseUid(firebaseUid)
                            .isActive(true)
                            .build());
            
            // 4. Actualizar datos (por si cambiaron en Firebase)
            user.setEmail(email);
            user.setDisplayName(name);
            user.setPhotoUrl(picture);
            user.setProvider(provider);
            
            // 5. Guardar
            User savedUser = userRepository.save(user);
            
            logger.info("User synced successfully: {} ({})", email, firebaseUid);
            
            // 6. Retornar DTO
            return mapToDto(savedUser);
            
        } catch (FirebaseAuthException e) {
            logger.error("Invalid Firebase token: {} - Message: {}", e.getErrorCode(), e.getMessage());
            logger.error("Full exception: ", e);
            throw new UnauthorizedException("Invalid or expired token: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error verifying token", e);
            throw new UnauthorizedException("Error verifying token: " + e.getMessage());
        }
    }

    /**
     * Busca un usuario por su Firebase UID
     */
    public User findByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new UnauthorizedException("User not found. Please sync first."));
    }

    /**
     * Verifica un token de Firebase y retorna el usuario local
     */
    public User verifyTokenAndGetUser(String idToken) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            String firebaseUid = decodedToken.getUid();
            return findByFirebaseUid(firebaseUid);
        } catch (FirebaseAuthException e) {
            logger.error("Invalid Firebase token", e);
            throw new UnauthorizedException("Invalid or expired token");
        }
    }

    private UserResponseDto mapToDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .firebaseUid(user.getFirebaseUid())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .photoUrl(user.getPhotoUrl())
                .provider(user.getProvider())
                .isActive(user.getIsActive())
                .build();
    }
}
