package org.ebugrad.computer_networks_project;

import java.io.*;
import java.net.*;
import java.util.*;

public class JokerServer {
    private static final int PORT = 4338;  // Port number for Joker Server

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("JokerServer is running. Port: " + PORT);

            // Continuously listen for incoming connections from GameServer
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Game Server connected: " + socket);

                // Handle each request in a new thread
                new Thread(() -> handleRequest(socket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Processes the joker request received from the GameServer
    private static void handleRequest(Socket socket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // Expect input format: JOKER:Y|S:CORRECT=A (e.g. JOKER:S:CORRECT=B)
            String input = in.readLine();
            System.out.println("Received joker request: " + input);

            if (input == null || !input.startsWith("JOKER:")) return;

            String[] parts = input.split(":");
            if (parts.length < 3) return;

            String jokerType = parts[1]; // Either "Y" (50:50) or "S" (Ask Audience)
            String correctOption = parts[2].split("=")[1]; // Correct answer option, e.g., A, B, C, D

            if (jokerType.equals("S")) {
                // Handle "Ask the Audience" joker
                Map<String, Integer> percentages = generateAudiencePercentages(correctOption);

                // Format the percentages for each choice
                String result = String.format("A) %d%% | B) %d%% | C) %d%% | D) %d%%",
                        percentages.get("A"), percentages.get("B"), percentages.get("C"), percentages.get("D"));

                // Send the result back to GameServer
                out.println("JOKER_RESULT:" + result);

            } else if (jokerType.equals("Y")) {
                // Handle "50:50" joker - keep correct option and one incorrect option
                List<String> options = Arrays.asList("A", "B", "C", "D");
                List<String> remaining = new ArrayList<>();
                remaining.add(correctOption);

                // Randomly select one wrong option to keep
                Collections.shuffle(options);
                for (String s : options) {
                    if (!s.equals(correctOption)) {
                        remaining.add(s);
                        break;
                    }
                }

                // Sort options alphabetically before sending
                Collections.sort(remaining); // e.g., A, C
                out.println("JOKER_RESULT:" + String.join(",", remaining));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Generates random percentages for the "Ask Audience" joker with bias towards correct answer
    private static Map<String, Integer> generateAudiencePercentages(String correctOption) {
        Map<String, Integer> map = new HashMap<>();
        List<String> options = Arrays.asList("A", "B", "C", "D");

        Random rand = new Random();
        int correctPercent = 60 + rand.nextInt(21); // Correct answer gets 60–80%

        map.put(correctOption, correctPercent);

        // Distribute the remaining percentage randomly among wrong options
        int remaining = 100 - correctPercent;
        List<String> wrongOptions = new ArrayList<>(options);
        wrongOptions.remove(correctOption);

        int r1 = rand.nextInt(remaining + 1);
        int r2 = rand.nextInt(remaining - r1 + 1);
        int r3 = remaining - r1 - r2;

        map.put(wrongOptions.get(0), r1);
        map.put(wrongOptions.get(1), r2);
        map.put(wrongOptions.get(2), r3);

        return map;
    }
}