package org.ebugrad.computer_networks_project;

// Represents a single quiz question with multiple choices and the correct answer
public class Question {
    // The question text
    public String question;

    // The possible answer choices (e.g., A, B, C, D)
    public String[] choices;

    // The correct answer option (e.g., "A", "B", "C", or "D")
    public String correct;

    // Default no-argument constructor
    public Question() {}

    // Constructor to initialize all fields
    public Question(String question, String[] choices, String correct) {
        this.question = question;
        this.choices = choices;
        this.correct = correct;
    }
}