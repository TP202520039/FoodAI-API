package com.tp.foodai.food_detection.services;

import com.tp.foodai.food_detection.dtos.external.AiDetectionRequestDto;
import com.tp.foodai.food_detection.dtos.external.AiDetectionResponseDto;
import com.tp.foodai.food_detection.exceptions.AiDetectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class AiDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(AiDetectionService.class);
    
    private final WebClient webClient;

    public AiDetectionService(WebClient.Builder webClientBuilder,
                              @Value("${ai.detection.api.url}") String aiApiUrl) {
        this.webClient = webClientBuilder
                .baseUrl(aiApiUrl)
                .build();
    }

    /**
     * Llama a la API de detección de alimentos con la URL de la imagen
     * @param imageUrl URL de la imagen en Azure Blob Storage
     * @return Respuesta con los alimentos detectados
     */
    public AiDetectionResponseDto detectFood(String imageUrl) {
        try {
            logger.info("Calling AI Detection API with image URL: {}", imageUrl);

            AiDetectionRequestDto request = AiDetectionRequestDto.builder()
                    .imageUrl(imageUrl)
                    .build();

            AiDetectionResponseDto response = webClient.post()
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AiDetectionResponseDto.class)
                    .timeout(Duration.ofSeconds(30)) // Timeout de 30 segundos
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        logger.error("AI API returned error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
                        return Mono.error(new AiDetectionException(
                                "AI Detection API returned error: " + ex.getStatusCode()));
                    })
                    .onErrorResume(Exception.class, ex -> {
                        logger.error("Error calling AI Detection API", ex);
                        return Mono.error(new AiDetectionException(
                                "Failed to call AI Detection API: " + ex.getMessage()));
                    })
                    .block();

            if (response == null) {
                logger.error("AI API returned null response");
                throw new AiDetectionException("AI Detection API returned null response");
            }

            // Si no hay alimentos detectados, devolver respuesta vacía (no es un error)
            if (response.getDetectedFoods() == null || response.getDetectedFoods().isEmpty()) {
                logger.warn("AI API returned no detected foods");
                response.setDetectedFoods(new java.util.ArrayList<>());
            } else {
                logger.info("AI Detection successful. Detected {} foods", response.getDetectedFoods().size());
            }

            return response;

        } catch (AiDetectionException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during AI detection", e);
            throw new AiDetectionException("Unexpected error during food detection", e);
        }
    }
}
