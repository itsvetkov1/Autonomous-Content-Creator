package com.autocc.autonomouscontentcreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            // Get credentials from properties file
            String openAiApiKey = PropertiesConfig.getProperty("openai.api.key");
            String facebookPageAccessToken = PropertiesConfig.getProperty("facebook.page.token");
            String facebookPageId = PropertiesConfig.getProperty("facebook.page.id");
            String instagramAccessToken = PropertiesConfig.getProperty("instagram.access.token");
            String instagramBusinessId = PropertiesConfig.getProperty("instagram.business.id");
            String imageUrl = "https://yourserver.com/path-to/biology_fact.jpg"; // This could also be moved to properties if needed

            // Validate required properties
            if (!validateRequiredProperties(openAiApiKey, facebookPageAccessToken, facebookPageId)) {
                logger.error("Missing required properties. Please check application.properties file.");
                return;
            }

            // Initialize the AutoCC class
            AutoCC autoPoster = new AutoCC(openAiApiKey);

            // Generate biology fact
            String fact = autoPoster.generateBiologyFact();

            // Check if fact generation was successful
            if (fact != null && !fact.isEmpty()) {
                logger.info("Generated Biology Fact: {}", fact);

                // Post to Facebook using the new RestFB implementation
                autoPoster.postToFacebook(facebookPageAccessToken, facebookPageId, fact);

                // Create image with the fact
                String imagePath = autoPoster.createImageWithText(fact);
                if (imagePath != null) {
                    logger.info("Image created at: {}", imagePath);

                    // Post to Instagram if credentials are available
                    if (instagramAccessToken != null && instagramBusinessId != null) {
                        autoPoster.postToInstagram(instagramAccessToken, instagramBusinessId, imageUrl, fact);
                    } else {
                        logger.warn("Instagram credentials not found - skipping Instagram post");
                    }
                }
            } else {
                logger.error("Failed to generate a biology fact.");
            }
        } catch (Exception e) {
            logger.error("An error occurred while running the application", e);
        }
    }

    private static boolean validateRequiredProperties(String... properties) {
        for (String property : properties) {
            if (property == null || property.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}