package com.tp.foodai.food_detection.services;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.tp.foodai.food_detection.exceptions.ImageUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class AzureBlobStorageService {

    private static final Logger logger = LoggerFactory.getLogger(AzureBlobStorageService.class);
    
    private final BlobContainerClient blobContainerClient;

    public AzureBlobStorageService(BlobContainerClient blobContainerClient) {
        this.blobContainerClient = blobContainerClient;
    }

    /**
     * Sube una imagen a Azure Blob Storage
     * @param file Archivo de imagen
     * @param firebaseUid UID del usuario (para organizar por carpetas)
     * @return URL pública de la imagen subida
     */
    public String uploadImage(MultipartFile file, String firebaseUid) {
        try {
            // Generar nombre único para el blob
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
            
            String blobName = String.format("%s/%d-%s%s", 
                firebaseUid, 
                System.currentTimeMillis(),
                UUID.randomUUID().toString(),
                extension
            );

            logger.info("Uploading image to Azure Blob Storage: {}", blobName);

            // Obtener el blob client
            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

            // Subir el archivo
            blobClient.upload(file.getInputStream(), file.getSize(), true);

            // Retornar la URL pública
            String imageUrl = blobClient.getBlobUrl();
            logger.info("Image uploaded successfully: {}", imageUrl);

            return imageUrl;

        } catch (IOException e) {
            logger.error("Failed to upload image to Azure Blob Storage", e);
            throw new ImageUploadException("Failed to upload image", e);
        } catch (Exception e) {
            logger.error("Unexpected error during image upload", e);
            throw new ImageUploadException("Unexpected error uploading image", e);
        }
    }

    /**
     * Elimina una imagen de Azure Blob Storage
     * @param imageUrl URL completa de la imagen
     */
    public void deleteImage(String imageUrl) {
        try {
            // Extraer el nombre del blob desde la URL
            String blobName = extractBlobNameFromUrl(imageUrl);
            
            if (blobName == null) {
                logger.warn("Could not extract blob name from URL: {}", imageUrl);
                return;
            }

            BlobClient blobClient = blobContainerClient.getBlobClient(blobName);
            
            if (blobClient.exists()) {
                blobClient.delete();
                logger.info("Image deleted successfully: {}", blobName);
            } else {
                logger.warn("Blob does not exist: {}", blobName);
            }

        } catch (Exception e) {
            logger.error("Failed to delete image from Azure Blob Storage: {}", imageUrl, e);
            // No lanzamos excepción, solo logueamos el error
        }
    }

    private String extractBlobNameFromUrl(String imageUrl) {
        try {
            // Ejemplo de URL: https://account.blob.core.windows.net/container/firebaseUid/timestamp-uuid.jpg
            String containerName = blobContainerClient.getBlobContainerName();
            int containerIndex = imageUrl.indexOf("/" + containerName + "/");
            
            if (containerIndex != -1) {
                return imageUrl.substring(containerIndex + containerName.length() + 2);
            }
            
            return null;
        } catch (Exception e) {
            logger.error("Error extracting blob name from URL", e);
            return null;
        }
    }
}
