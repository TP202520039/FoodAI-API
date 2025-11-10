package com.tp.foodai.food_detection.exceptions;

public class AiDetectionException extends RuntimeException {
    
    public AiDetectionException(String message) {
        super(message);
    }
    
    public AiDetectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
