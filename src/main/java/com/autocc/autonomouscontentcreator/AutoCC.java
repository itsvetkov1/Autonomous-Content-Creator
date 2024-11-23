package com.autocc.autonomouscontentcreator;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
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

import javax.imageio.ImageIO;

public class AutoCC {
    private static final Logger logger = LoggerFactory.getLogger(AutoCC.class);
    private OpenAiService openAiService;

    public AutoCC(String openAiApiKey) {
        openAiService = new OpenAiService(openAiApiKey);
    }

    // Step 1: Generate Biology Fact
    public String generateBiologyFact() {
        try {
            ChatMessage systemMessage = new ChatMessage("system", "You are a helpful assistant.");
            ChatMessage userMessage = new ChatMessage("user", "Provide an interesting and unique biology fact.");

            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                    .model("gpt-4o-mini")
                    .messages(Arrays.asList(systemMessage, userMessage))
                    .maxTokens(60)
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

    // Step 2: Post to Facebook
    public void postToFacebook(String pageAccessToken, String pageId, String message) {
        APIContext context = new APIContext(pageAccessToken);
        try {
            new Page(pageId, context).createFeed()
                    .setMessage(message)
                    .execute();
            logger.info("Successfully posted to Facebook.");
        } catch (APIException e) {
            logger.error("Error posting to Facebook.", e);
        }
    }

    // Step 3: Create Image with Text
    public String createImageWithText(String text) {
        int width = 800;
        int height = 800;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Fill background with white color
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Set text properties
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));

        // Split text into lines if too long
        String[] lines = splitTextIntoLines(text, 40);

        // Calculate starting position
        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight();
        int totalTextHeight = lines.length * lineHeight;
        int y = (height - totalTextHeight) / 2 + fm.getAscent();

        for (String line : lines) {
            int textWidth = fm.stringWidth(line);
            int x = (width - textWidth) / 2;
            g2d.drawString(line, x, y);
            y += lineHeight;
        }

        g2d.dispose();

        String filePath = "biology_fact.jpg";
        try {
            ImageIO.write(image, "jpg", new File(filePath));
            logger.info("Image created at: {}", filePath);
            return filePath;
        } catch (IOException e) {
            logger.error("Error creating image with text.", e);
            return null;
        }
    }

    private String[] splitTextIntoLines(String text, int maxLineLength) {
        String[] words = text.split(" ");
        StringBuilder sb = new StringBuilder();
        java.util.List<String> lines = new java.util.ArrayList<>();
        for (String word : words) {
            if (sb.length() + word.length() + 1 > maxLineLength) {
                lines.add(sb.toString());
                sb = new StringBuilder();
            }
            sb.append(word).append(" ");
        }
        lines.add(sb.toString().trim());
        return lines.toArray(new String[0]);
    }

    // Step 4: Post to Instagram
    public void postToInstagram(String instagramAccessToken, String instagramBusinessId, String imageUrl, String caption) {
        try {
            // Step 1: Create Media Object
            String mediaCreationEndpoint = String.format(
                    "https://graph.facebook.com/v17.0/%s/media",
                    instagramBusinessId
            );

            Map<String, String> params = new HashMap<>();
            params.put("image_url", imageUrl);
            params.put("caption", caption);
            params.put("access_token", instagramAccessToken);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(mediaCreationEndpoint))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(ofFormData(params))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String mediaId = parseMediaId(response.body());

            if (mediaId == null) {
                logger.error("Failed to create media object on Instagram.");
                return;
            }

            // Step 2: Publish Media
            String publishEndpoint = String.format(
                    "https://graph.facebook.com/v17.0/%s/media_publish",
                    instagramBusinessId
            );

            Map<String, String> publishParams = new HashMap<>();
            publishParams.put("creation_id", mediaId);
            publishParams.put("access_token", instagramAccessToken);

            HttpRequest publishRequest = HttpRequest.newBuilder()
                    .uri(URI.create(publishEndpoint))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(ofFormData(publishParams))
                    .build();

            HttpResponse<String> publishResponse = client.send(publishRequest, HttpResponse.BodyHandlers.ofString());

            logger.info("Successfully posted to Instagram.");
        } catch (Exception e) {
            logger.error("Error posting to Instagram.", e);
        }
    }

    private static HttpRequest.BodyPublisher ofFormData(Map<String, String> data) {
        var builder = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    private String parseMediaId(String responseBody) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(responseBody);
            String mediaId = root.path("id").asText();
            logger.info("Media ID received: {}", mediaId);
            return mediaId.isEmpty() ? null : mediaId;
        } catch (IOException e) {
            logger.error("Error parsing media ID from response.", e);
            return null;
        }
    }
}
