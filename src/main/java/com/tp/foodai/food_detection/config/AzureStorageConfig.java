package com.tp.foodai.food_detection.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureStorageConfig {

    private static final Logger logger = LoggerFactory.getLogger(AzureStorageConfig.class);

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    @Bean
    public BlobServiceClient blobServiceClient() {
        logger.info("Initializing Azure Blob Storage Service Client");
        return new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    @Bean
    public BlobContainerClient blobContainerClient(BlobServiceClient blobServiceClient) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        
        // Crear el container si no existe
        if (!containerClient.exists()) {
            logger.info("Container '{}' does not exist. Creating...", containerName);
            containerClient.create();
            logger.info("Container '{}' created successfully", containerName);
        } else {
            logger.info("Using existing container '{}'", containerName);
        }
        
        return containerClient;
    }
}
