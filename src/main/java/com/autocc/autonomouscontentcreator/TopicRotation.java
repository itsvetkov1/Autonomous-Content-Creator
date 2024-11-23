package com.autocc.autonomouscontentcreator;

import java.util.*;
import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopicRotation {
    // Logger for debugging and monitoring
    private static final Logger logger = LoggerFactory.getLogger(TopicRotation.class);

    // File where we'll save our current position in the topic list
    private static final String TOPIC_STATE_FILE = "topic_state.json";

    // List to store all our biology topics
    private List<String> topics;

    // Keeps track of which topic we're currently on
    private int currentIndex;

    // Constructor - called when we create a new TopicRotation object
    public TopicRotation() {
        // Set up our list of topics
        initializeTopics();
        // Load the last used position
        loadState();
    }

    // Initialize our list of biology topics
    private void initializeTopics() {
        topics = Arrays.asList(
                // General Biology Fields
                "cellular biology",    // Study of cells
                "genetics",           // Study of genes and heredity
                "evolution",          // How species change over time
                "ecology",            // How organisms interact with environment

                // Specialized Fields
                "marine biology",     // Study of ocean life
                "plant biology",      // Study of plants
                "animal behavior",    // How animals behave
                "human anatomy",      // Structure of human body

                // Microscopic Biology
                "microbiology",       // Study of microscopic organisms
                "immunology",         // Study of immune system
                "neuroscience",       // Study of nervous system

                // Environmental Biology
                "biodiversity",       // Variety of life forms
                "conservation",       // Protecting species and habitats
                "molecular biology",  // Study of biological molecules

                // Additional Fields
                "biochemistry",       // Chemical processes in living organisms
                "developmental biology", // How organisms grow and develop
                "environmental biology", // Relationship between life and environment
                "zoology",            // Study of animals
                "parasitology",       // Study of parasites
                "biotechnology"       // Using biology in technology
        );
    }

    // Load the last used topic position from our save file
    private void loadState() {
        // Create a File object pointing to our save file
        File stateFile = new File(TOPIC_STATE_FILE);

        // Check if the file exists
        if (stateFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(stateFile))) {
                // Read the number from the file and convert it to an integer
                currentIndex = Integer.parseInt(reader.readLine().trim());

                // If the number is too big, start over from 0
                if (currentIndex >= topics.size()) {
                    currentIndex = 0;
                }
            } catch (IOException e) {
                // If there's any error, log it and start from 0
                logger.error("Error loading topic state", e);
                currentIndex = 0;
            }
        } else {
            // If file doesn't exist, start from 0
            currentIndex = 0;
        }
    }

    // Save our current position to the file
    private void saveState() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TOPIC_STATE_FILE))) {
            // Write the current index to the file
            writer.println(currentIndex);
        } catch (IOException e) {
            // Log any errors that occur
            logger.error("Error saving topic state", e);
        }
    }

    // Get the next topic in the rotation
    public String getNextTopic() {
        // Get the current topic
        String topic = topics.get(currentIndex);

        // Move to next topic, loop back to 0 if we reach the end
        currentIndex = (currentIndex + 1) % topics.size();

        // Save our new position
        saveState();

        // Log which topic we're using
        logger.info("Selected topic: {}", topic);

        return topic;
    }

    // Randomly shuffle the order of topics
    public void shuffleTopics() {
        // Create a new list from our topics
        List<String> shuffledTopics = new ArrayList<>(topics);
        // Randomly shuffle it
        Collections.shuffle(shuffledTopics);
        // Replace our original list with the shuffled one
        topics = shuffledTopics;
        logger.info("Topics shuffled");
    }

    // Get all topics (useful for debugging)
    public List<String> getAllTopics() {
        // Return a copy of our topics list
        return new ArrayList<>(topics);
    }
}