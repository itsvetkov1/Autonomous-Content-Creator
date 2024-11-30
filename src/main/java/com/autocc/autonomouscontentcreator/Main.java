package com.autocc.autonomouscontentcreator;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final LocalTime EXECUTION_TIME = LocalTime.of(11, 0); // 11:00 AM

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
                Thread.sleep(Duration.between(now, nextRun).toMillis());

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

            // Initialize the AutoCC class
            AutoCC autoPoster = new AutoCC(openAiApiKey);

            // Generate biology fact
            String fact = autoPoster.generateBiologyFact();

            if (fact != null && !fact.isEmpty()) {
                logger.info("Generated Biology Fact: {}", fact);

                // Post to Facebook
                autoPoster.postToFacebook(facebookPageAccessToken, facebookPageId, fact);

                logger.info("Successfully executed daily task");
            } else {
                logger.error("Failed to generate a biology fact");
            }
        } catch (Exception e) {
            logger.error("Error executing task", e);
        }
    }
}