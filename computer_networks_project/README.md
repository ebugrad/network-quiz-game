# 🎓 Networked Quiz Game: Who Wants to Be a Millionaire?

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-UI-blue?style=for-the-badge)
![Socket](https://img.shields.io/badge/Socket-TCP%2FIP-critical?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

> A real-time, socket-based multiplayer quiz game implementation mimicking the famous TV show format. Built with JavaFX and pure Java Sockets.

---

## 📖 Overview

This project simulates a **distributed game environment** where a client application connects to a central game server. It features a robust **multithreaded backend** capable of handling game states, scoring, and "Lifeline" (Joker) requests asynchronously.

### 🌟 Key Highlights
* **Real-time Communication:** Instant feedback between Client and Server using `PrintWriter` and `BufferedReader`.
* **Decoupled Joker Logic:** A dedicated `JokerServer` runs on a separate port (4338) to process lifeline algorithms independently.
* **Dynamic UI (JavaFX):**
  * Dark/Light mode toggle 🌙/☀️
  * Animated score updates and notifications.
  * Live bar charts for the "Ask the Audience" lifeline.
* **Smart Algorithms:**
  * *50:50* intelligently removes 2 wrong answers.
  * *Audience Vote* simulates a weighted probability distribution favoring the correct answer.

---

## 🏗️ System Architecture

The system follows a modular design with three distinct components communicating over TCP/IP.

```mermaid
graph TD;
    Client[Contestant App (GUI)] -->|Port 4337| GameServer[Game Server Logic];
    Client -->|Port 4338| JokerServer[Joker Server Logic];
    GameServer -.->|Validates Answer| JSON[Questions.json];
    JokerServer -.->|Calculates Probability| Logic[Algorithms];
```

* **Game Server (Port 4337):** Manages the game flow, questions, and scoring.
* **Joker Server (Port 4338):** Handles "50:50" and "Ask the Audience" requests.
* **Contestant App:** The JavaFX front-end for the player.

---

## 📸 Features Preview

| **Game Interface** | **Audience Joker** |
|:---:|:---:|
| 30-second timer per question.<br>Score tracking.<br>Immediate feedback. | Generates a dynamic BarChart.<br>Visualizes voting percentages. |

---

## 🚀 Getting Started

### Prerequisites

* **JDK 17** or later.
* **Maven** (Optional, but recommended).
* **IntelliJ IDEA** (Preferred IDE).

### 📥 Installation & Run

1. **Clone the Repository**

   ```bash
   git clone [https://github.com/ebugrad/network-quiz-game.git]
   cd network-quiz-game
   ```

2. **Dependencies**
   Ensure `gson-2.x.x.jar` is added to your project libraries or `pom.xml`.

3. **Run the Game**
   You only need to run the main client file. It automatically spins up the necessary servers in background threads.

   *Run the file:* `src/main/java/org/ebugrad/computer_networks_project/ContestantApp.java`

---

## 🎮 How to Play

1. **Start the App:** The game connects automatically.
2. **Answer Questions:** Click A, B, C, or D within **30 seconds**.
3. **Use Lifelines:**
  * **50:50:** Disables two wrong buttons.
  * **Audience:** Displays a chart of what the "AI audience" thinks.
4. **Win:** Reach 5 points to see the special victory message!

---

## 📂 File Structure

```text
src/main/java/org/ebugrad/computer_networks_project/
├── 🟢 ContestantApp.java       # Main Entry Point (JavaFX)
├── 🔵 ContestantController.java# UI Logic & Event Handling
├── 🟠 ContestantClient.java    # Socket Client Implementation
├── 🔴 GameServer.java          # Main Game Logic Server
├── 🟣 JokerServer.java         # Lifeline Logic Server
└── 📄 QuestionLoader.java      # JSON Parser
```

---

## 🤝 Contributing

Contributions are welcome! Please fork the repo and submit a pull request.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AdditionalFeature`)
3. Commit your Changes (`git commit -m 'Add some AdditionalFeature'`)
4. Push to the Branch (`git push origin feature/AdditionalFeature`)
5. Open a Pull Request

---

## 📜 License

Distributed under the MIT License. See `LICENSE` for more information.

**Built with ☕ and JavaFX.**