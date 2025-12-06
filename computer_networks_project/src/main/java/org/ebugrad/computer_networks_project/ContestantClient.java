package org.ebugrad.computer_networks_project;

import java.io.*;
import java.net.Socket;

// Client class responsible for connecting to the game server and handling communication
public class ContestantClient {
    private Socket socket; // Socket for network communication
    private PrintWriter out; // Output stream to send messages to server
    private BufferedReader in; // Input stream to receive messages from server
    private Thread listenThread; // Thread to listen for server messages asynchronously
    private final ContestantController controller; // Reference to UI controller to update UI

    // Constructor receives controller to update UI based on server messages
    public ContestantClient(ContestantController controller) {
        this.controller = controller;
    }

    // Connect to server with given IP address and port
    public void connectToServer(String ip, int port) {
        try {
            socket = new Socket(ip, port); // Establish socket connection
            out = new PrintWriter(socket.getOutputStream(), true); // Initialize output stream with auto-flush
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Initialize input stream

            // Thread to continuously listen for messages from the server
            listenThread = new Thread(() -> {
                try {
                    String msg;
                    // Read messages line by line until connection closes or error occurs
                    while ((msg = in.readLine()) != null) {
                        System.out.println("From Server: " + msg);

                        // Handle different types of messages from the server
                        if (msg.startsWith("QUESTION:")) {
                            // Update question text in UI
                            controller.updateQuestion(msg.substring(9));
                        } else if (msg.startsWith("CHOICES:")) {
                            // Parse choices separated by "|" and update UI buttons
                            String[] parts = msg.substring(8).split("\\|");
                            controller.updateChoices(
                                    parts[0].substring(2),
                                    parts[1].substring(2),
                                    parts[2].substring(2),
                                    parts[3].substring(2)
                            );
                        } else if (msg.startsWith("JOKER_RESULT:")) {
                            // Show result of joker lifeline in UI
                            controller.showJokerResult(msg.substring(13));
                        } else if (msg.equals("CORRECT")) {
                            // Increase the score in UI on correct answer
                            controller.increaseScore();
                        } else if (msg.equals("WRONG")) {
                            // On wrong answer, read reward message and end game
                            String reward = in.readLine();
                            controller.endGame(reward != null && reward.startsWith("REWARD:") ? reward.substring(7) : "Game over.");
                            break; // Stop listening after game ends
                        } else if (msg.startsWith("REWARD:") && controller.getScore() == 5) {
                            // Show final reward message when all questions answered correctly
                            controller.showFinalMessage(msg.substring(7));
                            break; // Stop listening after game ends
                        }
                    }
                } catch (IOException e) {
                    System.out.println("⚠️ Error reading from server!");
                    e.printStackTrace();
                }
            });

            listenThread.setDaemon(true); // Set thread as daemon 'so' it doesn't block JVM exit
            listenThread.start(); // Start listening thread

        } catch (IOException e) {
            System.out.println("⚠️ Could not connect to server!");
            e.printStackTrace();
        }
    }

    // Send the selected answer to the server
    public void sendAnswer(String answer) {
        if (out != null) {
            out.println(answer);
        }
    }

    // Send joker lifeline code to the server
    public void sendJoker(String jokerCode) {
        if (out != null) {
            out.println(jokerCode);
        }
    }

    // Disconnect cleanly from the server and close all streams
    public void disconnect() {
        try {
            System.out.println("🧹 [Client] Disconnect started...");
            if (listenThread != null && listenThread.isAlive()) {
                listenThread.interrupt(); // Interrupt the listening thread
                System.out.println("🛑 [Client] Listening thread interrupted.");
            }
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Close socket connection
                System.out.println("🔌 [Client] Socket closed.");
            }
            if (in != null) {
                in.close(); // Close input stream
                System.out.println("🔒 [Client] Input stream closed.");
            }
            if (out != null) {
                out.close(); // Close output stream
                System.out.println("🔒 [Client] Output stream closed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}