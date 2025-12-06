package org.ebugrad.computer_networks_project;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Main JavaFX application class for the contestant side of the quiz game
public class ContestantApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Start the GameServer and JokerServer in separate threads
        startGameAndJokerServers();

        // Load the FXML layout for the contestant UI
        FXMLLoader loader = new FXMLLoader(getClass().getResource("contestant.fxml"));
        Parent root = loader.load();

        // Create a scene with specified width and height
        Scene scene = new Scene(root, 700, 700);
        primaryStage.setTitle("Who Wants to Be a Millionaire?");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Prevent window resizing
        primaryStage.show(); // Display the GUI window

        // Get the controller associated with the FXML and set up the client
        ContestantController controller = loader.getController();
        ContestantClient client = new ContestantClient(controller);

        // Handle the window close event: disconnect client and exit application
        primaryStage.setOnCloseRequest(event -> {
            client.disconnect();
            Platform.exit();
            System.exit(0);
        });

        // Show the primary stage (again, though already shown above)
        primaryStage.show();

        // Pass the scene to the controller (possibly for dynamic UI updates)
        controller.setScene(scene);
    }

    // Method to start the GameServer and JokerServer concurrently
    private void startGameAndJokerServers() {
        new Thread(() -> {
            try {
                GameServer.main(null); // Start the Game Server
            } catch (Exception e) {
                e.printStackTrace(); // Print any exceptions
            }
        }).start();

        new Thread(() -> {
            try {
                JokerServer.main(null); // Start the Joker Server
            } catch (Exception e) {
                e.printStackTrace(); // Print any exceptions
            }
        }).start();
    }

    // Entry point of the JavaFX application
    public static void main(String[] args) {
        launch(args);
    }
}