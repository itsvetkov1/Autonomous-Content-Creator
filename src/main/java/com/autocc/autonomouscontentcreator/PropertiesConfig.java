package com.autocc.autonomouscontentcreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesConfig {
    private static final Logger logger = LoggerFactory.getLogger(PropertiesConfig.class);
    private static Properties properties;

    static {
        properties = new Properties();
        loadProperties();
    }

    private static void loadProperties() {
        try {
            // Try loading from classpath
            InputStream input = PropertiesConfig.class.getClassLoader().getResourceAsStream("application.properties");

            // If not found in classpath, try loading from current directory
            if (input == null) {
                input = PropertiesConfig.class.getClassLoader().getResourceAsStream("./application.properties");
            }

            // If still not found, try loading from src/main/resources
            if (input == null) {
                input = PropertiesConfig.class.getClassLoader().getResourceAsStream("src/main/resources/application.properties");
            }

            if (input == null) {
                logger.error("Unable to find application.properties. Please ensure it exists in src/main/resources/");
                throw new RuntimeException("Unable to find application.properties. Please ensure it exists in src/main/resources/");
            }

            properties.load(input);
            logger.info("Successfully loaded application.properties");
        } catch (IOException e) {
            logger.error("Error loading properties file", e);
            throw new RuntimeException("Error loading properties file", e);
        }
    }

    public static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            logger.warn("Property {} not found in configuration", key);
        }
        return value;
    }
}