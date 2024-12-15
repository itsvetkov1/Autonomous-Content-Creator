package com.autocc.autonomouscontentcreator;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.image.CreateImageRequest;
import com.facebook.ads.sdk.APIContext;
import com.facebook.ads.sdk.APIException;
import com.facebook.ads.sdk.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.time.Duration;
import java.nio.file.Files;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;

/**
 * Main class handling the autonomous content creation and posting process.
 * This class integrates OpenAI's GPT and DALL-E services with Facebook posting capabilities.
 */
public class AutoCC {
    private static final Logger logger = LoggerFactory.getLogger(AutoCC.class);
    private static final String DALLE_API_URL = "https://api.openai.com/v1/images/generations";
    private static final Duration TIMEOUT = Duration.ofSeconds(120);

    private final OpenAiService openAiService;
    private final TopicRotation topicRotation;
    private final LocalImageHandler imageHandler;
    private final ImgurUploader imgurUploader;
    private final String openAiApiKey;
    private final HttpClient httpClient;

    /**
     * Initializes the AutoCC system with necessary services and configurations.
     * @param openAiApiKey The API key for OpenAI services
     */
    public AutoCC(String openAiApiKey) {
        this.openAiApiKey = openAiApiKey;
        this.openAiService = new OpenAiService(openAiApiKey);
        this.topicRotation = new TopicRotation();
        this.imageHandler = new LocalImageHandler();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();

        // Initialize Imgur uploader with client ID from properties
        String imgurClientId = PropertiesConfig.getProperty("imgur.client.id");
        if (imgurClientId == null || imgurClientId.trim().isEmpty()) {
            logger.warn("Imgur client ID not configured. Image posting may not work correctly.");
        }
        this.imgurUploader = new ImgurUploader(imgurClientId);
    }

    /**
     * Generates a biology fact using GPT model and topic rotation system.
     * @return A generated biology fact, or null if generation fails
     */
    public String generateBiologyFact() {
        try {
            TopicRotation.PromptContent promptContent = topicRotation.getNextContent();

            ChatMessage systemMessage = new ChatMessage("system", promptContent.getSystemMessage());
            ChatMessage userMessage = new ChatMessage("user", promptContent.getUserMessage());

            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                    .model("gpt-4o-mini")
                    .messages(Arrays.asList(systemMessage, userMessage))
                    .maxTokens(400)
                    .temperature(0.7)
                    .build();

            String fact = openAiService.createChatCompletion(chatCompletionRequest)
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent()
                    .trim();

            logger.info("Generated Biology Fact: {}", fact);
            return fact;
        } catch (Exception e) {
            logger.error("Error generating biology fact.", e);
            return null;
        }
    }

    /**
     * Generates an image using DALL-E based on the provided prompt.
     * @param prompt The text prompt to generate an image from
     * @return Path to the saved image file, or null if generation fails
     */
    public String generateImage(String prompt) {
        try {
            String enhancedPrompt = createEnhancedPrompt(prompt);
            logger.info("Enhanced prompt for image generation: {}", enhancedPrompt);

            String requestBody = String.format("""
                {
                    "model": "dall-e-3",
                    "prompt": "%s",
                    "n": 1,
                    "size": "1024x1024",
                    "quality": "standard",
                    "style": "vivid",
                    "response_format": "url"
                }
                """, enhancedPrompt.replace("\"", "\\\""));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(DALLE_API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.error("DALL-E API request failed with status code: {} and message: {}",
                        response.statusCode(), response.body());
                return null;
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonResponse = mapper.readTree(response.body());
            String imageUrl = jsonResponse.get("data").get(0).get("url").asText();

            return imageHandler.saveImage(imageUrl);
        } catch (Exception e) {
            logger.error("Error generating image with DALL-E.", e);
            return null;
        }
    }

    /**
     * Posts content to Facebook, optionally including an image.
     * The image is first uploaded to Imgur to obtain a public URL.
     */
    public void postToFacebook(String pageAccessToken, String pageId, String message, String imagePath) {
        try {
            if (imagePath != null && !imagePath.isEmpty()) {
                File imageFile = new File(imagePath);
                if (!imageFile.exists()) {
                    logger.warn("Image file not found at path: {}. Falling back to text-only post.", imagePath);
                    createTextOnlyPost(pageAccessToken, pageId, message);
                    return;
                }

                // Upload to Imgur and get public URL
                String imgurUrl = imgurUploader.uploadImage(imagePath);
                if (imgurUrl == null) {
                    logger.error("Failed to upload image to Imgur. Falling back to text-only post.");
                    createTextOnlyPost(pageAccessToken, pageId, message);
                    return;
                }

                // Post to Facebook with the Imgur URL
                APIContext context = new APIContext(pageAccessToken);
                new Page(pageId, context).createFeed()
                        .setMessage(message)
                        .setLink(imgurUrl)
                        .execute();

                logger.info("Successfully posted to Facebook with Imgur image URL: {}", imgurUrl);
            } else {
                createTextOnlyPost(pageAccessToken, pageId, message);
            }
        } catch (APIException e) {
            logger.error("Error during Facebook post process: {}", e.getMessage());
            try {
                createTextOnlyPost(pageAccessToken, pageId, message);
            } catch (APIException ae) {
                logger.error("Failed to create fallback text-only post: {}", ae.getMessage());
            }
        }
    }

    /**
     * Posts to Instagram using image URL from Imgur.
     * Note: This method is prepared for future implementation when Instagram posting is needed.
     */
    public void postToInstagram(String instagramAccessToken, String instagramBusinessId, String imagePath, String caption) {
        try {
            if (imagePath != null && !imagePath.isEmpty()) {
                String imgurUrl = imgurUploader.uploadImage(imagePath);
                if (imgurUrl == null) {
                    logger.error("Failed to upload image to Imgur. Instagram post cancelled.");
                    return;
                }

                // TODO: Implement Instagram posting using the Imgur URL
                logger.info("Image uploaded to Imgur successfully. URL: {}", imgurUrl);
                logger.info("Instagram posting functionality to be implemented.");
            }
        } catch (Exception e) {
            logger.error("Error posting to Instagram: {}", e.getMessage());
        }
    }

    /**
     * Creates a text-only post on Facebook.
     */
    private void createTextOnlyPost(String pageAccessToken, String pageId, String message) throws APIException {
        APIContext context = new APIContext(pageAccessToken);
        new Page(pageId, context).createFeed()
                .setMessage(message)
                .execute();
        logger.info("Successfully posted text-only content to Facebook.");
    }

    /**
     * Enhances the original prompt for better image generation results.
     */
    private String createEnhancedPrompt(String originalPrompt) {
        return String.format(
                "Create a detailed scientific illustration in the style of a biology textbook showing: %s. " +
                        "The illustration should be clear, accurate, and educational, with a clean background. " +
                        "Use realistic colors and proper anatomical/biological detail. " +
                        "Include subtle labels or indicators where appropriate to highlight key features.",
                originalPrompt
        );
    }

    /**
     * Cleans up old generated images to manage disk space.
     */
    public void cleanupOldImages(int daysToKeep) {
        imageHandler.cleanupOldImages(daysToKeep);
    }
}