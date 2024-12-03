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