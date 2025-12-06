package org.ebugrad.computer_networks_project;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;

// Utility class to load a list of Question objects from a JSON resource file
public class QuestionLoader {

    /**
     * Loads a list of questions from a JSON file located in the resource path.
     *
     * @param resourcePath The path to the JSON resource (e.g., "/org/ebd/computer_networks_project/questions.json")
     * @return List of Question objects parsed from the JSON
     * @throws IOException if the resource file cannot be found or read
     */
    public static List<Question> loadFromJson(String resourcePath) throws IOException {
        // Open an input stream to the JSON resource file
        InputStream is = QuestionLoader.class.getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IOException("Resource not found: " + resourcePath);
        }

        // Read and parse the JSON into a list of Question objects using Gson
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            Gson gson = new Gson();
            Type questionListType = new TypeToken<List<Question>>() {}.getType();
            return gson.fromJson(reader, questionListType);
        }
    }
}