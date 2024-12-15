package com.autocc.autonomouscontentcreator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;
import java.time.Duration;
import java.io.File;

public class ImgurUploader {
    private static final Logger logger = LoggerFactory.getLogger(ImgurUploader.class);
    private static final String IMGUR_API_URL = "https://api.imgur.com/3/image";
    private final String clientId;
    private final HttpClient httpClient;

    public ImgurUploader(String clientId) {
        this.clientId = clientId;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Uploads an image to Imgur and returns its URL
     * @param imagePath Path to the local image file
     * @return The URL of the uploaded image, or null if upload fails
     */
    public String uploadImage(String imagePath) {
        try {
            // Read the image file and convert to Base64
            byte[] imageBytes = Files.readAllBytes(new File(imagePath).toPath());
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Prepare the upload request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(IMGUR_API_URL))
                    .header("Authorization", "Client-ID " + clientId)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            String.format("{\"image\":\"%s\",\"type\":\"base64\"}", base64Image)))
                    .build();

            // Send the request
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            // Check if request was successful
            if (response.statusCode() != 200) {
                logger.error("Imgur upload failed with status {}: {}",
                        response.statusCode(), response.body());
                return null;
            }

            // Parse the response to get the image URL
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse = mapper.readTree(response.body());

            if (!jsonResponse.path("success").asBoolean()) {
                logger.error("Imgur upload failed: {}",
                        jsonResponse.path("data").path("error").asText());
                return null;
            }

            String imageUrl = jsonResponse.path("data").path("link").asText();
            logger.info("Successfully uploaded image to Imgur: {}", imageUrl);
            return imageUrl;

        } catch (Exception e) {
            logger.error("Error uploading to Imgur: {}", e.getMessage());
            return null;
        }
    }
}