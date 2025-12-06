package org.ebugrad.computer_networks_project;

import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {

    private static final int PORT = 4337;           // Port for contestant connections
    private static final String JOKER_IP = "127.0.0.1";  // Joker server IP address
    private static final int JOKER_PORT = 4338;          // Joker server port

    // List to hold all the quiz questions
    private static List<Question> questions = new ArrayList<>();

    // Reward messages based on how far the contestant progresses
    private static final String[] rewardMessages = {
            "Mob loading...", "Participation matters", "Two is greater than one",
            "It wasn't easy to get here", "You really know this game", "You're amazing!"
    };

    public static void main(String[] args) throws IOException {
        // Load questions from a JSON file at startup
        questions = QuestionLoader.loadFromJson("/org/ebugrad/computer_networks_project/questions.json");
        if (questions == null || questions.isEmpty()) {
            System.err.println("❌ Failed to load questions!");
            return;
        }
        System.out.println("Questions loaded successfully.");

        // Create server socket to listen for incoming contestant connections
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Game Server started. Port: " + PORT);

            // Continuously accept new clients and handle each in a new thread
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Contestant connected: " + clientSocket.getInetAddress());
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Handles the game interaction with a single contestant
    private static void handleClient(Socket clientSocket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            int score = 0;  // Track the number of correctly answered questions

            // Loop through all the questions
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);

                // Send question and choices to the contestant
                out.println("QUESTION:" + q.question);
                out.println("CHOICES:A)" + q.choices[0] + "|B)" + q.choices[1] + "|C)" + q.choices[2] + "|D)" + q.choices[3]);
                out.println("JOKERS:S (Ask Audience), Y (50:50)");

                String response;

                // Wait for contestant's answer or joker request
                while (true) {
                    response = in.readLine();
                    if (response == null) {
                        System.out.println("❌ Contestant connection closed.");
                        return;
                    }
                    // Handle joker requests
                    if (response.equalsIgnoreCase("S") || response.equalsIgnoreCase("Y")) {
                        String jokerReply = askJoker(response, q);
                        out.println(jokerReply);
                        continue;
                    }
                    break;
                }

                System.out.println("Received answer: " + response);

                // Check answer correctness
                if (response.equalsIgnoreCase(q.correct)) {
                    score++;
                    out.println("CORRECT");
                } else {
                    out.println("WRONG");
                    // Send reward message based on score reached
                    out.println("REWARD:" + rewardMessages[score]);
                    return;  // End game on wrong answer
                }
            }

            // If all questions answered correctly
            out.println("Congratulations! You answered all questions correctly!");
            out.println("REWARD:" + rewardMessages[5]);

        } catch (IOException e) {
            System.err.println("⚠️ Error in handleClient: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("❌ Contestant connection closed.");
            } catch (IOException ignored) {
            }
        }
    }

    // Communicates with the Joker Server to get a hint or help
    private static String askJoker(String jokerCode, Question q) {
        try (Socket jokerSocket = new Socket(JOKER_IP, JOKER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(jokerSocket.getInputStream()));
             PrintWriter out = new PrintWriter(jokerSocket.getOutputStream(), true)) {

            // Send the joker code and the correct answer to Joker Server
            out.println("JOKER:" + jokerCode + ":CORRECT=" + q.correct);
            return in.readLine();  // Return Joker Server's reply

        } catch (IOException e) {
            e.printStackTrace();
            return "Joker connection failed.";
        }
    }
}