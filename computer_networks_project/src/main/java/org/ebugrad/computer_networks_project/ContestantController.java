package org.ebugrad.computer_networks_project;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.ServerSocket;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.prefs.Preferences;

public class ContestantController {

    @FXML private BarChart<String, Number> audienceChart;
    @FXML private Label questionLabel;
    @FXML private Button btnA, btnB, btnC, btnD;
    @FXML private Button btnAudience, btnFiftyFifty;
    @FXML private Label timerLabel, scoreLabel, jokerLabel;
    @FXML private Button btnRestart, btnToggleTheme;
    @FXML private Label notificationLabel;

    private ContestantClient client;
    private Timeline timer;
    private int timeLeft = 30;
    private int score = 0;

    private boolean audienceUsed = false;
    private boolean fiftyFiftyUsed = false;

    private Scene scene;

    @FXML
    public void initialize() {
        loadTheme(); // Load user preferred theme
        startGameAndJokerServers(); // Start the game and joker servers if not running
        startClient(); // Initialize client connection
        audienceChart.setVisible(false); // Hide audience chart initially
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        loadTheme(); // Apply theme when scene is set
    }

    private void startClient() {
        client = new ContestantClient(this);
        client.connectToServer("127.0.0.1", 4337); // Connect to game server
    }

    @FXML
    private void handleAnswer(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        String answer = clicked.getText().substring(0, 1); // Get first letter as answer
        client.sendAnswer(answer); // Send answer to server
        stopTimer(); // Stop countdown
        disableAnswerButtons(); // Prevent multiple answers
    }

    @FXML
    private void handleAudienceJoker() {
        if (!audienceUsed) {
            client.sendJoker("S"); // Request audience joker
            audienceUsed = true;
            btnAudience.setDisable(true); // Disable joker button
        }
    }

    @FXML
    private void handleFiftyFiftyJoker() {
        if (!fiftyFiftyUsed) {
            client.sendJoker("Y"); // Request 50:50 joker
            fiftyFiftyUsed = true;
            btnFiftyFifty.setDisable(true); // Disable joker button
        }
    }

    @FXML
    private void handleRestart() {
        System.out.println("🔁 Restarting game...");

        stopTimer(); // Stop timer
        disableAnswerButtons(); // Disable answer buttons

        score = 0;
        scoreLabel.getStyleClass().remove("high-score");
        scoreLabel.setText("Score: 0"); // Reset score display
        jokerLabel.setText("");
        questionLabel.setText("Ready..."); // Reset question label
        btnA.setText("A"); // Reset answer buttons text
        btnB.setText("B");
        btnC.setText("C");
        btnD.setText("D");
        enableAnswerButtons(); // Enable buttons for new game

        btnAudience.setDisable(false);
        btnFiftyFifty.setDisable(false);
        audienceUsed = false;
        fiftyFiftyUsed = false;

        audienceChart.setVisible(false); // Hide audience chart

        if (client != null) {
            client.disconnect(); // Disconnect existing client
            client = null;
        }

        startGameAndJokerServers(); // Restart servers if needed

        // Reconnect client after 1-second delay on UI thread
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> {
                    client = new ContestantClient(this);
                    client.connectToServer("127.0.0.1", 4337);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleExit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("The quiz will be terminated.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (client != null) client.disconnect(); // Disconnect client on exit
            Platform.exit();
            System.exit(0); // Terminate application
        }
    }

    @FXML
    private void handleToggleTheme() {
        Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
        boolean isDark = prefs.getBoolean("isDarkTheme", true);
        prefs.putBoolean("isDarkTheme", !isDark); // Toggle theme preference
        applyTheme(!isDark); // Apply new theme
    }

    private void loadTheme() {
        Platform.runLater(() -> {
            Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
            boolean isDark = prefs.getBoolean("isDarkTheme", true);
            applyTheme(isDark); // Load and apply saved theme
        });
    }

    private void applyTheme(boolean isDark) {
        if (scene == null) return;

        ObservableList<String> stylesheets = scene.getStylesheets();
        String darkCss = Objects.requireNonNull(getClass().getResource("/org/ebugrad/computer_networks_project/css/dark.css")).toExternalForm();
        String lightCss = Objects.requireNonNull(getClass().getResource("/org/ebugrad/computer_networks_project/css/light.css")).toExternalForm();

        stylesheets.clear();
        if (isDark) {
            stylesheets.add(darkCss);
            btnToggleTheme.setText("🌙"); // Moon icon for dark theme
        } else {
            stylesheets.add(lightCss);
            btnToggleTheme.setText("☀️"); // Sun icon for light theme
        }
    }

    public void showNotification(String message) {
        Platform.runLater(() -> {
            notificationLabel.setText(message);
            notificationLabel.setVisible(true);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), notificationLabel);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), notificationLabel);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setDelay(Duration.seconds(3));

            fadeIn.play();
            fadeIn.setOnFinished(e -> fadeOut.play());
            fadeOut.setOnFinished(e -> notificationLabel.setVisible(false));
        });
    }

    public void updateQuestion(String question) {
        Platform.runLater(() -> {
            if (score == 0) {
                showNotification("🎉 New game started! Good luck!"); // Notify new game start
            }

            questionLabel.setText(question);
            jokerLabel.setText("");
            audienceChart.setVisible(false); // Hide audience chart on new question
            startTimer(); // Start countdown timer
        });
    }

    public void updateChoices(String a, String b, String c, String d) {
        Platform.runLater(() -> {
            btnA.setText("A) " + a);
            btnB.setText("B) " + b);
            btnC.setText("C) " + c);
            btnD.setText("D) " + d);
            enableAnswerButtons(); // Enable buttons for new choices
        });
    }

    public int getScore() {
        return score;
    }

    public void increaseScore() {
        score++;
        updateScoreWithAnimation(score); // Update score visually

        if (score == 5) {
            scoreLabel.getStyleClass().add("high-score"); // Highlight high score
        } else {
            scoreLabel.getStyleClass().remove("high-score");
        }
    }

    public void updateScoreWithAnimation(int newScore) {
        Platform.runLater(() -> {
            KeyValue keyValue = new KeyValue(scoreLabel.textFillProperty(), Color.GREEN);
            KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), keyValue);
            Timeline timeline = new Timeline(keyFrame);
            timeline.play();
            scoreLabel.setText("Score: " + newScore);
        });
    }

    /**
     * Process joker result message
     * @param msg Message from joker server
     */
    public void showJokerResult(String msg) {
        Platform.runLater(() -> {
            if (msg.matches("[A-D](,[A-D])*")) {
                jokerLabel.setText("🎁 50:50 used. Remaining options: " + msg);
                audienceChart.setVisible(false);

                Map<String, Button> buttons = Map.of(
                        "A", btnA, "B", btnB, "C", btnC, "D", btnD
                );

                buttons.values().forEach(btn -> btn.setDisable(true)); // Disable all buttons

                for (String opt : msg.split(",")) {
                    Button b = buttons.get(opt.trim());
                    if (b != null) b.setDisable(false); // Enable remaining options
                }
            }
            else {
                jokerLabel.setText("🎁 Ask the Audience result:");
                audienceChart.setVisible(true);
                audienceChart.getData().clear();

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                for (String part : msg.split("\\|")) {
                    String[] split = part.trim().split("\\)");
                    if (split.length == 2) {
                        String option = split[0];
                        String percentStr = split[1].replaceAll("\\D", "");
                        try {
                            int percent = Integer.parseInt(percentStr);
                            series.getData().add(new XYChart.Data<>(option, percent)); // Add audience votes
                        } catch (NumberFormatException ignored) {}
                    }
                }
                audienceChart.getData().add(series);
            }
        });
    }

    public void endGame(String message) {
        Platform.runLater(() -> {
            stopTimer();
            disableAnswerButtons();
            btnAudience.setDisable(true);
            btnFiftyFifty.setDisable(true);
            timerLabel.setText("⏳");
            questionLabel.setText("❌ Game Over!\n" + message);
            btnRestart.setDisable(false);
            audienceChart.setVisible(false);
        });
    }

    public void showFinalMessage(String rewardMessage) {
        Platform.runLater(() -> {
            stopTimer();
            questionLabel.setText("🎉 Congratulations! You answered all questions correctly!\n" + rewardMessage);
            disableAnswerButtons();
            btnAudience.setDisable(true);
            btnFiftyFifty.setDisable(true);
            btnRestart.setDisable(false);
            audienceChart.setVisible(false);
        });
    }

    private void startTimer() {
        stopTimer();
        timeLeft = 30;
        updateTimerLabel();

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft--;
            updateTimerLabel();

            if (timeLeft <= 0) {
                stopTimer();
                disableAnswerButtons();
                timerLabel.getStyleClass().remove("warning");
                timerLabel.setText("⏳ Time's up!");
                questionLabel.setText("⏳ Time's up! No answer received.");
                client.sendAnswer(""); // Send empty answer when time runs out
            }
        }));

        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void updateTimerLabel() {
        timerLabel.setText("⏳ Time Left: " + timeLeft + " s");
        if (timeLeft <= 5 && !timerLabel.getStyleClass().contains("warning")) {
            timerLabel.getStyleClass().add("warning"); // Add warning style when time is low
        } else if (timeLeft > 5) {
            timerLabel.getStyleClass().remove("warning"); // Remove warning style otherwise
        }
    }

    private void stopTimer() {
        if (timer != null) timer.stop();
    }

    private void disableAnswerButtons() {
        btnA.setDisable(true);
        btnB.setDisable(true);
        btnC.setDisable(true);
        btnD.setDisable(true);
    }

    private void enableAnswerButtons() {
        btnA.setDisable(false);
        btnB.setDisable(false);
        btnC.setDisable(false);
        btnD.setDisable(false);
    }

    private boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true; // Port is available
        } catch (Exception e) {
            return false; // Port is in use
        }
    }

    private void startGameAndJokerServers() {
        if (isPortAvailable(4337)) {
            new Thread(() -> {
                try {
                    GameServer.main(null); // Start game server
                } catch (Exception e) {
                    System.out.println("⚠️ GameServer could not start.");
                    e.printStackTrace();
                }
            }).start();
        } else {
            System.out.println("🔁 GameServer already running.");
        }

        if (isPortAvailable(4338)) {
            new Thread(() -> {
                try {
                    JokerServer.main(null); // Start joker server
                } catch (Exception e) {
                    System.out.println("⚠️ JokerServer could not start.");
                    e.printStackTrace();
                }
            }).start();
        } else {
            System.out.println("🔁 JokerServer already running.");
        }
    }
}