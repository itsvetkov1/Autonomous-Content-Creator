package com.autocc.autonomouscontentcreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// File and IO imports
import java.io.IOException;
import java.io.InputStream;

// Network imports
import java.net.URL;

// NIO imports for file operations
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class LocalImageHandler {
    private static final Logger logger = LoggerFactory.getLogger(LocalImageHandler.class);
    private final String imageFolderPath;

    public LocalImageHandler() {
        // Create a folder named 'generated_images' in your project directory
        this.imageFolderPath = "generated_images";
        createImageFolder();
    }

    private void createImageFolder() {
        try {
            Files.createDirectories(Path.of(imageFolderPath));
            logger.info("Image storage folder created/verified at: {}", imageFolderPath);
        } catch (IOException e) {
            logger.error("Failed to create image storage folder", e);
        }
    }

    public String saveImage(String imageUrl) {
        try {
            // Generate a unique filename using timestamp
            String fileName = "biology_image_" + System.currentTimeMillis() + ".png";
            Path targetPath = Path.of(imageFolderPath, fileName);

            // Download and save the image
            InputStream in = new URL(imageUrl).openStream();
            Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);

            logger.info("Image saved successfully at: {}", targetPath);
            return targetPath.toString();
        } catch (IOException e) {
            logger.error("Failed to save image", e);
            return null;
        }
    }

    // This method helps manage older images to prevent disk space issues
    public void cleanupOldImages(int daysToKeep) {
        try {
            long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L);

            Files.list(Path.of(imageFolderPath))
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            logger.info("Deleted old image: {}", path);
                        } catch (IOException e) {
                            logger.error("Failed to delete old image: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            logger.error("Error during cleanup of old images", e);
        }
    }
}