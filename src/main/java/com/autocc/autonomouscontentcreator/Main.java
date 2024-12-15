package com.autocc.autonomouscontentcreator;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final LocalTime EXECUTION_TIME = LocalTime.of(11, 00); // 11:00 AM

    public static void main(String[] args) {
        logger.info("Starting AutoCC Service. Will post daily at 11:00 AM");

        while (true) {
            try {
                // Calculate time until next execution
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime nextRun = now.with(EXECUTION_TIME);

                // If it's past 11 AM today, schedule for tomorrow
                if (now.toLocalTime().isAfter(EXECUTION_TIME)) {
                    nextRun = nextRun.plusDays(1);
                }

                // Calculate delay until next run
                Duration timeUntilNextRun = Duration.between(now, nextRun);
                long minutesUntilNextRun = timeUntilNextRun.toMinutes();
                logger.info("Next execution scheduled in {} minutes (at {})",
                        minutesUntilNextRun, nextRun.toLocalTime());

                // Sleep until next execution time
                Thread.sleep(timeUntilNextRun.toMillis());

                // Execute the task
                executeTask();

                // After execution, sleep for 1 minute to avoid multiple executions
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                logger.error("Sleep interrupted", e);
            } catch (Exception e) {
                logger.error("Error in main loop", e);
                try {
                    // If there's an error, wait 1 minute before retrying
                    Thread.sleep(60000);
                } catch (InterruptedException ie) {
                    logger.error("Error sleep interrupted", ie);
                }
            }
        }
    }

    private static void executeTask() {
        try {
            // Get credentials from properties file
            String openAiApiKey = PropertiesConfig.getProperty("openai.api.key");
            String facebookPageAccessToken = PropertiesConfig.getProperty("facebook.page.token");
            String facebookPageId = PropertiesConfig.getProperty("facebook.page.id");

            // Validate required properties
            if (!validateRequiredProperties(openAiApiKey, facebookPageAccessToken, facebookPageId)) {
                logger.error("Missing required properties. Please check application.properties file.");
                return;
            }

            // Initialize the AutoCC class
            AutoCC autoPoster = new AutoCC(openAiApiKey);

            // Step 1: Generate biology fact
            String fact = autoPoster.generateBiologyFact();
            if (fact == null || fact.isEmpty()) {
                logger.error("Failed to generate a biology fact");
                return;
            }
            logger.info("Generated biology fact: {}", fact);

            // Step 2: Generate image using DALL-E
            String imagePath = autoPoster.generateImage(fact);
            if (imagePath == null) {
                logger.warn("Failed to generate image. Proceeding with text-only post.");
            } else {
                logger.info("Generated image at: {}", imagePath);
            }

            // Step 3: Post to Facebook
            autoPoster.postToFacebook(facebookPageAccessToken, facebookPageId, fact, imagePath);
            logger.info("Successfully executed daily task");

            // Optional: Clean up old images (keeping last 7 days)
            autoPoster.cleanupOldImages(7);

        } catch (Exception e) {
            logger.error("Error executing task", e);
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