package com.thepowermisha.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FilterService {
    private static final Logger logger = Logger.getLogger(FilterService.class.getName());

    private final Set<String> forbiddenWords = new HashSet<>();

    public FilterService(String filePath) {
        loadWords(filePath);
    }

    private void loadWords(String resourceName) {

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Resource not found: " + resourceName);
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    String word = line.trim().toLowerCase();
                    if (!word.isEmpty()) {
                        forbiddenWords.add(word);
                    }
                }
            }

            logger.info("Loaded " + forbiddenWords.size() + " forbidden words");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load forbidden words from resource: " + resourceName, e);
            throw new RuntimeException("Cannot load forbidden words", e);
        }

    }

    public boolean containsForbiddenWords(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String lowerText = text.toLowerCase();

        for (String forbiddenWord : forbiddenWords) {
            if (lowerText.contains(forbiddenWord)) {
                return true;
            }
        }

        return false;
    }
}
