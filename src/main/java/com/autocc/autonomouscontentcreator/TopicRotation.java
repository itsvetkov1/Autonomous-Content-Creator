package com.autocc.autonomouscontentcreator;

import java.util.*;
import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopicRotation {
    private static final String OUTPUT_LANGUAGE = "Bulgarian";

    private static final Logger logger = LoggerFactory.getLogger(TopicRotation.class);
    private static final String TOPIC_STATE_FILE = "topic_state.json";
    private static final String PROMPT_STATE_FILE = "prompt_state.json";

    private List<String> topics;
    private List<PromptApproach> promptApproaches;
    private int currentTopicIndex;
    private int currentPromptIndex;

    // Inner class to represent different prompt approaches
    public static class PromptApproach {
        private final String systemMessage;
        private final String userMessageTemplate;

        public PromptApproach(String systemMessage, String userMessageTemplate) {
            this.systemMessage = systemMessage;
            this.userMessageTemplate = userMessageTemplate;
        }

        public String getSystemMessage() {
            return systemMessage;
        }

        public String getUserMessage(String topic) {
            return String.format(userMessageTemplate, topic);
        }
    }

    public TopicRotation() {

        initializeTopics();
        initializePromptApproaches();
        loadState();
    }

    // Initialize our list of biology topics
    private void initializeTopics() {
        topics = Arrays.asList(
                // Original Topics
                "cellular biology",
                "genetics",
                "evolution",
                "ecology",
                "marine biology",
                "plant biology",
                "animal behavior",
                "human anatomy",
                "microbiology",
                "immunology",
                "neuroscience",
                "biodiversity",
                "conservation",
                "molecular biology",
                "biochemistry",
                "developmental biology",
                "environmental biology",
                "zoology",
                "parasitology",
                "biotechnology",

                // New Specialized Fields
                "chronobiology",          // Study of biological rhythms
                "biomechanics",           // Study of mechanics in biological systems
                "systems biology",        // Holistic approach to biological systems
                "synthetic biology",      // Engineering biological systems
                "proteomics",            // Study of proteins on a large scale
                "metabolomics",          // Study of metabolites in organisms
                "bioinformatics",        // Computational analysis of biological data
                "epigenetics",           // Study of heritable changes without DNA changes
                "pharmacology",          // Study of drug interactions with organisms
                "virology",              // Study of viruses

                // Emerging Fields
                "neuroimmunology",       // Interaction between nervous and immune systems
                "biogeography",          // Study of species distribution
                "biomimicry",            // Nature-inspired innovation
                "astrobiology",          // Study of life in space
                "paleobiology",          // Study of ancient life
                "cryobiology",           // Study of life at low temperatures
                "sociobiology",          // Biological basis of social behavior
                "psychobiology",         // Biological basis of behavior
                "ethnobiology",          // How cultures use nature
                "radiobiology",          // Effects of radiation on life

                // Specialized Topics
                "endocrinology",         // Study of hormones
                "mycology",              // Study of fungi
                "herpetology",           // Study of reptiles and amphibians
                "ornithology",           // Study of birds
                "entomology",            // Study of insects
                "phycology",             // Study of algae
                "nematology",            // Study of roundworms
                "embryology",            // Study of embryos
                "histology",             // Study of tissues
                "cytogenetics",          // Study of chromosomes

                // Modern Applications
                "nanobiotechnology",     // Manipulation of biological systems at nano scale
                "bioengineering",        // Engineering principles in biology
                "computational biology",  // Computer modeling of biological systems
                "genomics",              // Study of complete genetic material
                "metagenomics",          // Study of genetic material from environmental samples
                "glycobiology",          // Study of carbohydrates in organisms
                "biogeochemistry",       // Study of chemical cycles in ecosystems
                "neurogenetics",         // Genetic basis of neural development
                "immunogenetics",        // Genetic basis of immune response
                "phylogenetics",         // Study of evolutionary relationships

                // Integrative Fields
                "behavioral ecology",     // How behavior affects survival
                "evolutionary medicine",  // Evolution's impact on health
                "conservation genetics",  // Genetic aspects of conservation
                "population genetics",    // Genetic variation in populations
                "molecular ecology",      // Molecular techniques in ecology
                "cognitive neuroscience", // Brain basis of cognition
                "environmental genomics", // Genetic responses to environment
                "molecular pathology",    // Molecular basis of disease
                "developmental genetics", // Genetic control of development
                "quantitative genetics"   // Statistical analysis of inheritance
        );
    }

    private void initializePromptApproaches() {
        promptApproaches = Arrays.asList(
                // Base Approach - Enhanced with Clear Structure
                new PromptApproach(
                        String.format("You are a helpful assistant with expertise in making complex scientific concepts accessible. You communicate in fluent %s to ensure precise and natural explanations.", OUTPUT_LANGUAGE),
                        String.format("Provide an interesting and unique biology fact about %%s in %s that reveals something unexpected or surprising. Focus on recent discoveries or lesser-known aspects. Keep it engaging and suitable for social media. Keep it under 280 characters.", OUTPUT_LANGUAGE)
                ),

                // Question-Based Format
                new PromptApproach(
                        String.format("You are a helpful assistant skilled in Socratic teaching methods, communicating effectively in %s.", OUTPUT_LANGUAGE),
                        String.format("Frame an interesting biology fact about %%s in %s as a 'Did you know?' question, followed by a clear, engaging answer. Make it suitable for social media and keep it under 280 characters.", OUTPUT_LANGUAGE)
                ),

                // Real-World Connection
                new PromptApproach(
                        String.format("You are a helpful assistant who excels at connecting scientific concepts to everyday experiences, speaking naturally in %s.", OUTPUT_LANGUAGE),
                        String.format("Share an interesting biology fact about %%s in %s that connects to everyday life or current events. Make it relatable and engaging for social media. Keep it under 280 characters.", OUTPUT_LANGUAGE)
                ),

                // Visual-Oriented
                new PromptApproach(
                        String.format("You are a helpful assistant with a talent for creating vivid mental images, expressing them clearly in %s.", OUTPUT_LANGUAGE),
                        String.format("Provide an interesting and unique biology fact about %%s in %s that can be easily visualized. Include a specific detail that would make a striking image. Keep it engaging for social media and under 280 characters.", OUTPUT_LANGUAGE)
                ),

                // Educational Impact
                new PromptApproach(
                        String.format("You are a helpful assistant focused on challenging misconceptions and deepening understanding, communicating effectively in %s.", OUTPUT_LANGUAGE),
                        String.format("Share a fascinating biology fact about %%s in %s that challenges common misconceptions or reveals something counterintuitive. Keep it engaging for social media and under 280 characters.", OUTPUT_LANGUAGE)
                )
        );
    }

    // Get the current prompt approach along with the next topic
    public PromptContent getNextContent() {
        String topic = getNextTopic();
        PromptApproach approach = getNextPromptApproach();

        return new PromptContent(
                approach.getSystemMessage(),
                approach.getUserMessage(topic)
        );
    }

    private PromptApproach getNextPromptApproach() {
        PromptApproach approach = promptApproaches.get(currentPromptIndex);
        currentPromptIndex = (currentPromptIndex + 1) % promptApproaches.size();
        savePromptState();
        logger.info("Selected prompt approach index: {}", currentPromptIndex);
        return approach;
    }

    // Class to hold the generated content
    public static class PromptContent {
        private final String systemMessage;
        private final String userMessage;

        public PromptContent(String systemMessage, String userMessage) {
            this.systemMessage = systemMessage;
            this.userMessage = userMessage;
        }

        public String getSystemMessage() {
            return systemMessage;
        }

        public String getUserMessage() {
            return userMessage;
        }
    }

    private void savePromptState() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(PROMPT_STATE_FILE))) {
            writer.println(currentPromptIndex);
        } catch (IOException e) {
            logger.error("Error saving prompt state", e);
        }
    }

    private void loadState() {
        loadTopicState();
        loadPromptState();
    }

    private void loadPromptState() {
        File stateFile = new File(PROMPT_STATE_FILE);
        if (stateFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(stateFile))) {
                currentPromptIndex = Integer.parseInt(reader.readLine().trim());
                if (currentPromptIndex >= promptApproaches.size()) {
                    currentPromptIndex = 0;
                }
            } catch (IOException e) {
                logger.error("Error loading prompt state", e);
                currentPromptIndex = 0;
            }
        } else {
            currentPromptIndex = 0;
        }
    }

    // Original methods remain the same but renamed for clarity
    private void loadTopicState() {
        File stateFile = new File(TOPIC_STATE_FILE);
        if (stateFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(stateFile))) {
                currentTopicIndex = Integer.parseInt(reader.readLine().trim());
                if (currentTopicIndex >= topics.size()) {
                    currentTopicIndex = 0;
                }
            } catch (IOException e) {
                logger.error("Error loading topic state", e);
                currentTopicIndex = 0;
            }
        } else {
            currentTopicIndex = 0;
        }
    }

    private String getNextTopic() {
        String topic = topics.get(currentTopicIndex);
        currentTopicIndex = (currentTopicIndex + 1) % topics.size();
        saveTopicState();
        logger.info("Selected topic: {}", topic);
        return topic;
    }

    private void saveTopicState() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TOPIC_STATE_FILE))) {
            writer.println(currentTopicIndex);
        } catch (IOException e) {
            logger.error("Error saving topic state", e);
        }
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