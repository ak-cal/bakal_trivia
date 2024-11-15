package com.example.groupbakal.groupbakal_finalproject;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.animation.Timeline;

import javafx.scene.media.AudioClip;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import org.apache.commons.lang3.StringEscapeUtils;
import javafx.concurrent.Task;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class TriviaController {
    private Stage stage;
    private VBox quizRoot;
    private Path triviaFilePath;
    private String apiUrl;
    private int questionIndex;
    private List<Question> questions;
    private String difficulty;
    private String mode;

    //profile
    @FXML
    private TextField playerProfile;
    private String playerName;

    private int score = 0;

    @FXML
    public void initialize() {
        Font.loadFont(getClass().getResourceAsStream("LuckiestGuy-Regular.ttf"), 54);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    public void setTriviaFilePath(Path triviaFilePath) {
        this.triviaFilePath = triviaFilePath;
    }
    public void setMode(String mode) {
        this.mode = mode;
    }
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
    //profile
    private void savePlayerName(String name) {
        this.playerName = name;
    }
    //

    //leaderboard
    public void showLeaderboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/groupbakal/groupbakal_finalproject/Leaderboard.fxml"));
            Parent root = loader.load();
            // Create a new Stage
            Stage popupStage = new Stage();
            popupStage.initStyle(StageStyle.UNDECORATED);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setScene(new Scene(root));

            popupStage.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    //

    @FXML
    public void switchToMenu(ActionEvent event) throws IOException {
        //profile
        String playerName = playerProfile.getText().trim();
        if (playerName.isEmpty()) {
            playerName = "Guest";
        }
        PlayerProfile.setPlayerName(playerName);
        System.out.println("Player name saved: " + playerName);
        //

        Parent root = FXMLLoader.load(getClass().getResource("Menu.fxml"));
        //profile
        savePlayerName(playerName);
        //
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
    public void switchToMenu() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Menu.fxml"));
        stage = (Stage) stage.getScene().getWindow(); // Use the stored stage directly
        stage.setScene(new Scene(root));
        stage.show();
    }
    public void switchToStart() throws IOException {
        quizBGMusic.stop();

        Parent root = FXMLLoader.load(getClass().getResource("Start.fxml"));
        stage = (Stage) stage.getScene().getWindow(); // Use the stored stage directly
        stage.setScene(new Scene(root));
        stage.show();
    }
    VBox scoreBar = new VBox(10);
    Label scoreLabel = new Label();
    private long startQuizTime;
    private long endQuizTime;
    private int totalQuizTime;
    private StackPane stackPane;
    private VBox pauseOverlay;

    Media backgroundMusic = new Media(getClass().getResource("/sounds/music-background.mp3").toExternalForm());
    MediaPlayer quizBGMusic = new MediaPlayer(backgroundMusic);

    public void switchToQuiz() throws IOException {
        quizBGMusic.setVolume(0.1);
        quizBGMusic.setCycleCount(MediaPlayer.INDEFINITE);
        quizBGMusic.play();

        loadQuestionItems();
        if (stage == null || triviaFilePath == null || questions == null || questions.isEmpty()) {
            System.out.println("Quiz setup is incomplete.");
            return;
        }
        startQuizTime = System.currentTimeMillis();

        quizRoot = new VBox(10);
        stackPane = new StackPane();

        questionIndex = 0;
        displayQuestion(questions.get(questionIndex));

        quizRoot.setStyle("-fx-font-family: 'Luckiest Guy';");
        stackPane.getChildren().add(quizRoot);

        stage.setScene(new Scene(stackPane, 700, 450));
        stage.setTitle("Trivia App");
        stage.show();
    }
    private void displayQuestion(Question question) {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), quizRoot);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(event -> {
            quizRoot.getChildren().clear();
            quizRoot.setAlignment(Pos.CENTER);
            quizRoot.setStyle("-fx-background-color: linear-gradient(to top, #356169, #a0d1c5); -fx-background-radius: 10px; -fx-padding: 20px;");

            // Question text area
            String decodedQuestion = StringEscapeUtils.unescapeHtml4(question.getQuestion());
            TextArea questionTextArea = new TextArea(decodedQuestion);
            int fontSize = decodedQuestion.length() > 100 ? 16 : 20;
            questionTextArea.setStyle(
                    "-fx-font-size: " + fontSize + "px; " +
                            "-fx-font-family: 'Luckiest Guy'; " +
                            "-fx-text-fill: black; " +
                            "-fx-control-inner-background: white; " +
                            "-fx-padding: 5px; " +
                            "-fx-background-color: transparent; " +
                            "-fx-border-color: black; " +
                            "-fx-border-width: 5px; " +
                            "-fx-border-radius: 20px; "
            );

            questionTextArea.setWrapText(true);
            questionTextArea.setEditable(false);
            questionTextArea.setPrefWidth(quizRoot.getWidth() - 100);
            questionTextArea.setMaxWidth(quizRoot.getWidth() - 100);
            questionTextArea.setPrefHeight(100);
            questionTextArea.setMaxHeight(200);

            VBox centeredVBox = new VBox(questionTextArea);
            centeredVBox.setAlignment(Pos.CENTER);
            centeredVBox.setPadding(new Insets(1));

            VBox questionVBox = new VBox(1, centeredVBox);
            questionVBox.setAlignment(Pos.CENTER);
            questionVBox.setPadding(new Insets(1));

            VBox answerVBox = new VBox(10);
            answerVBox.setAlignment(Pos.CENTER);
            answerVBox.setPadding(new Insets(10, 5, 10, 5));

            // Choices setup
            List<String> choices = new ArrayList<>(question.getIncorrectAnswers());
            choices.add(question.getCorrectAnswer());
            Collections.shuffle(choices);

            for (int i = 0; i < choices.size(); i++) {
                String decodedChoice = StringEscapeUtils.unescapeHtml4(choices.get(i));
                Button choiceButton = createChoiceButton(decodedChoice, question.getCorrectAnswer(), i);
                answerVBox.getChildren().add(choiceButton);
            }

            // Mode label
            Label modeLabel = new Label();
            scoreLabel.setStyle("-fx-font-size: " + fontSize + "px; -fx-font-family: 'Luckiest Guy'; -fx-text-fill: white;" +
                    "    -fx-effect: dropshadow(gaussian, black, 3, 0.8, 2, 2), dropshadow(gaussian, black, 3, 0.8, -2, -2);");
            timeLabel.setStyle("-fx-font-size: " + fontSize + "px; -fx-font-family: 'Luckiest Guy'; -fx-text-fill: white;" +
                    "    -fx-effect: dropshadow(gaussian, black, 3, 0.8, 2, 2), dropshadow(gaussian, black, 3, 0.8, -2, -2);");
            liveslabel.setStyle("-fx-font-size: " + fontSize + "px; -fx-font-family: 'Luckiest Guy'; -fx-text-fill: white;" +
                    "    -fx-effect: dropshadow(gaussian, black, 3, 0.8, 2, 2), dropshadow(gaussian, black, 3, 0.8, -2, -2);");
            timerLabel.setStyle("-fx-font-size: " + fontSize + "px; -fx-font-family: 'Luckiest Guy'; -fx-text-fill: white;" +
                    "    -fx-effect: dropshadow(gaussian, black, 3, 0.8, 2, 2), dropshadow(gaussian, black, 3, 0.8, -2, -2);");

            // Pause button
            Button pauseButton = new Button("| |");
            pauseButton.setStyle("-fx-font-size: 20px; -fx-font-family: 'Luckiest Guy'; -fx-background-color: black; -fx-text-fill: white;");
            pauseButton.setOnAction(e -> showOption());

            // Display the score, timer, and lives based on mode
            scoreLabel.setText("Score: " + score);
            if ("Blitz".equals(mode)) {
                modeLabel.setText("BLITZ");
                modeLabel.setStyle("-fx-font-size: 50px; -fx-font-family: 'Luckiest Guy'; -fx-text-fill: white;" +
                        "-fx-effect: dropshadow(gaussian, black, 5, 1.0, 3, 3), dropshadow(gaussian, black, 5, 1.0, -3, -3)");
                timerLabel.setText("Time:10");
                scoreBar.getChildren().setAll(scoreLabel, timerLabel);
                resetTimer();
            } else if ("Classic".equals(mode)) {
                modeLabel.setText("CLASSIC");
                modeLabel.setStyle("-fx-font-size: 50px; -fx-font-family: 'Luckiest Guy'; -fx-text-fill: white;" +
                        "-fx-effect: dropshadow(gaussian, black, 5, 1.0, 3, 3), dropshadow(gaussian, black, 5, 1.0, -3, -3)");
                scoreBar.getChildren().setAll(scoreLabel, timeLabel);
                startStopwatch(timeLabel);
            } else if ("Endless".equals(mode)) {
                modeLabel.setText("ENDLESS");
                modeLabel.setStyle("-fx-font-size: 50px; -fx-font-family: 'Luckiest Guy'; -fx-text-fill: white; " +
                        "-fx-effect: dropshadow(gaussian, black, 5, 1.0, 3, 3), dropshadow(gaussian, black, 5, 1.0, -3, -3)");
                liveslabel.setText("Lives: " + lives);
                scoreBar.getChildren().setAll(scoreLabel, liveslabel);
            }

            // Spacer regions for alignment
            Region spacer1 = new Region();
            HBox.setHgrow(spacer1, Priority.ALWAYS);
            Region spacer2 = new Region();
            HBox.setHgrow(spacer2, Priority.ALWAYS);

            // Score and pause bar layout
            HBox scorePauseBar = new HBox(pauseButton, spacer1, modeLabel, spacer2, scoreBar);
            scorePauseBar.setAlignment(Pos.TOP_CENTER);
            scorePauseBar.setPadding(new Insets(10, 0, 0, 10));

            quizRoot.getChildren().addAll(scorePauseBar, questionVBox, answerVBox);

            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), quizRoot);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }
    private void showOption() {
        // Pause the quiz timers
        if ("Blitz".equals(mode) && timer != null) {
            timer.cancel();
        } else if ("Classic".equals(mode) && isRunning) {
            timeline.pause();
            isRunning = false;
        }

        if (pauseOverlay == null) {
            pauseOverlay = new VBox(20);
            pauseOverlay.setAlignment(Pos.CENTER);
            pauseOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-padding: 20; -fx-background-radius: 10;");
            pauseOverlay.setPrefSize(stackPane.getWidth(), stackPane.getHeight());

            Button resumeButton = new Button("Resume");
            resumeButton.setStyle("-fx-font-family: 'Luckiest Guy'; -fx-font-size: 16; -fx-background-color: #2ecc71; -fx-text-fill: white;");
            resumeButton.setOnAction(e -> {
                resumeQuiz();
                pauseOverlay.setVisible(false);
            });

            Button restartButton = new Button("Restart");
            restartButton.setStyle("-fx-font-family: 'Luckiest Guy'; -fx-font-size: 16; -fx-background-color: #e74c3c; -fx-text-fill: white;");
            restartButton.setOnAction(e -> {
                pauseOverlay.setVisible(false);
                resetQuiz();
            });

            Button mainMenuButton = new Button("Main Menu");
            mainMenuButton.setStyle("-fx-font-family: 'Luckiest Guy'; -fx-font-size: 16; -fx-background-color: #3498db; -fx-text-fill: white;");
            mainMenuButton.setOnAction(e -> {
                pauseOverlay.setVisible(false);
                try {
                    switchToMenu();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            pauseOverlay.getChildren().addAll(resumeButton, restartButton, mainMenuButton);
            stackPane.getChildren().add(pauseOverlay);
        }

        pauseOverlay.setVisible(true);
    }

    private void resumeQuiz() {
        if ("Blitz".equals(mode)) {
            startTimer();
        } else if ("Classic".equals(mode)) {
            isRunning = true;
            startStopwatch(timeLabel);
        }
    }

    private void resetQuiz() {
        // Reset all score, lives, and counters
        score = 0;
        streakCount = 0;
        lives = 3;
        seconds = 0;
        scoreLabel.setText("Score: " + score);

        // Reset stopwatch or timer depending on the mode
        if ("Blitz".equals(mode)) {
            resetTimer();
        } else if ("Classic".equals(mode)) {
            stopStopwatch();
            timeLabel.setText("00:00");
        } else if ("Endless".equals(mode)) {
            liveslabel.setText("Lives: " + lives);
        }
        // Restart the quiz at the first question
        questionIndex = 0;
        displayQuestion(questions.get(questionIndex));
    }

    private Button createChoiceButton(String choice, String correctAnswer, int choiceIndex) {
        Button choiceButton = new Button(choice);
        choiceButton.setStyle(getButtonStyle(choiceIndex));
        choiceButton.setOnAction(e -> handleAnswer(choice, correctAnswer));
        return choiceButton;
    }

    private String getButtonStyle(int choiceIndex) {
        String color;
        switch (choiceIndex) {
            case 0 -> color = "#e21b3c";  // Red
            case 1 -> color = "#d89e00";  // Yellow
            case 2 -> color = "#1368ce";  // Blue
            default -> color = "#26890c"; // Green
        }
        return String.format("-fx-background-color: %s; -fx-background-radius: 10; -fx-padding: 13; " +
                "-fx-border-radius: 10; -fx-border-color: black; " +
                "-fx-font-size: 21px; -fx-font-family: 'Luckiest Guy'; " +
                "-fx-text-fill: linear-gradient(to bottom, #A1B7B2, white);" +
                "    -fx-fill: #ffffff;" +
                "    -fx-effect: dropshadow(gaussian, black, 5, 0.5, 2, 2);", color);
    }

    private int totalTime = 20;
    private int streakCount = 0;
    private long endTime;
    private int timeTaken;


    private void handleAnswer(String chosenAnswer, String correctAnswer) {
        int remainingTime = questionTime + timeBonus - timePenalty;
        correctAnswer = StringEscapeUtils.unescapeHtml4(correctAnswer);

        boolean isCorrect = chosenAnswer.equals(correctAnswer);
        AudioClip correctFX = new AudioClip(getClass().getResource("/sounds/correct-156911.mp3").toExternalForm());
        AudioClip wrongFX = new AudioClip(getClass().getResource("/sounds/error-8-206492.mp3").toExternalForm());

        if (isCorrect) {
            System.out.println("Correct!");
            correctFX.play();
            streakCount++;
        } else {
            System.out.println("Incorrect! The correct answer is " + correctAnswer);
            streakCount = 0;
            wrongFX.play();
            if (mode.equals("Endless")) {
                lives--;
                liveslabel.setText("Lives: " + lives);
            }
        }
        switch (mode) {
            case "Blitz":
                if (isCorrect) {
                    endTime = System.currentTimeMillis();
                    timeTaken = (int) ((endTime - startTime) / 1000);
                    blitzScoringSystem(true, timeTaken, remainingTime, totalTime);
                }
                break;
            case "Classic":
                if (isCorrect) {
                    classicScoringSystem();
                }
                break;
            case "Endless":
                if (isCorrect) {
                    endlessScoringSystem();
                }
                break;
        }

        questionIndex++;

        // Handle Endless mode
        if ("Endless".equals(mode) && (questionIndex >= questions.size() && lives > 0)) {
            // Fetch new questions asynchronously
            fetchNewQuestions();
        } else if (shouldShowScore()) {
            endQuizTime = System.currentTimeMillis();
            totalQuizTime = (int) ((endQuizTime - startQuizTime) / 1000);
            showScore(score, totalQuizTime);
        } else if (questionIndex < questions.size()) {
            displayQuestion(questions.get(questionIndex));
        }
    }


    private boolean shouldShowScore() {
        return mode.equals("Endless") && lives == 0
                || questionIndex >= questions.size();
    }

    private void showScore(int totalScore, int totalTime) {
        String playerName = PlayerProfile.getPlayerName();
        quizBGMusic.stop();
        quizRoot.getChildren().clear();
        quizRoot.setAlignment(Pos.CENTER);
        quizRoot.setStyle("-fx-background-color: #34495e;");

        VBox scoreBox = new VBox();
        scoreBox.setStyle("-fx-background-radius: 8; -fx-background-color: #ecf0f1; -fx-border-radius: 5; -fx-border-color: #2c3e50; -fx-border-width: 2;");
        scoreBox.setPrefSize(550, 330);
        scoreBox.setAlignment(Pos.CENTER);

        // Title label
        Label endLabel = createLabel("QUIZ COMPLETED!", "-fx-font-size: 50px; -fx-font-family: 'Luckiest Guy'; -fx-text-fill: white;" +
                "-fx-effect: dropshadow(gaussian, black, 5, 1.0, 3, 3), dropshadow(gaussian, black, 5, 1.0, -3, -3)");
        if("Endless".equals(mode) && lives == 0){
            endLabel.setText("GAME OVER!");
        }
        Label playerLabel = createLabel("Good Job! " + playerName , "-fx-font-family: 'Luckiest Guy'; -fx-font-size: 30; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Score label and score value
        Label scoreLabel = createLabel("SCORE", "-fx-font-family: 'Luckiest Guy'; -fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label totalPoints = createLabel(String.valueOf(totalScore), "-fx-font-family: 'Luckiest Guy'; -fx-font-size: 36; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox scoreTotalBox = new VBox();
        scoreTotalBox.getChildren().addAll(scoreLabel, totalPoints);
        scoreTotalBox.setAlignment(Pos.CENTER);

        Label timeLabel = createLabel("TIME", "-fx-font-family: 'Luckiest Guy'; -fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label totalTimeLabel = createLabel(Integer.toString(totalQuizTime) + " s", " -fx-font-family: 'Luckiest Guy'; -fx-font-size: 36; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");


        VBox timeBox = new VBox();
        timeBox.getChildren().addAll(timeLabel, totalTimeLabel);
        timeBox.setAlignment(Pos.CENTER);
        VBox.setMargin(timeBox, new Insets(10, 15, 10 ,15));
        VBox.setMargin(scoreTotalBox, new Insets(10, 15, 10 ,15));

        VBox spacer = new VBox(20, new Label("   "));
        spacer.setPadding(new Insets(10, 20, 10 ,20));

        HBox scoreTimeLabelBox = new HBox();
        scoreTimeLabelBox.getChildren().addAll(scoreTotalBox, spacer, timeBox);
        scoreTimeLabelBox.setAlignment(Pos.CENTER);
        scoreTimeLabelBox.setPadding(new Insets(10, 10, 10, 10));

        Button replayButton = new Button("PLAY AGAIN");
        replayButton.setStyle("-fx-font-family: 'Luckiest Guy'; -fx-font-size: 16; -fx-background-color: #2c3e50; -fx-text-fill: white; -fx-border-radius: 5; -fx-padding: 5 15;");
        replayButton.setOnAction(e -> {
            try {
                switchToMenu();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        scoreLabel.setPadding(new Insets(5, 5, 5, 5)); // Padding around the score label
        timeLabel.setPadding(new Insets(5, 5, 5, 5));  // Padding around the time label
        //leaderboard
        Button leaderboardButton = new Button("VIEW LEADERBOARD");
        leaderboardButton.setStyle("-fx-font-family: 'Luckiest Guy'; -fx-font-size: 16; -fx-background-color: #2c3e50; -fx-text-fill: white; -fx-border-radius: 5; -fx-padding: 5 15;");
        leaderboardButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/groupbakal/groupbakal_finalproject/Leaderboard.fxml"));
                Parent root = loader.load();
                Stage popupStage = new Stage();
                popupStage.initStyle(StageStyle.UNDECORATED);
                popupStage.initModality(Modality.APPLICATION_MODAL);
                popupStage.setScene(new Scene(root));

                popupStage.showAndWait();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Leaderboard clicked");
        });

        Button exitButton = new Button("BACK TO MENU");
        exitButton.setStyle("-fx-font-family: 'Luckiest Guy' ; -fx-font-size: 16; -fx-background-color: #2c3e50; -fx-text-fill: white; -fx-border-radius: 5; -fx-padding: 5 15;");
        exitButton.setOnAction(e -> {
            try {
                switchToStart();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        VBox spacer2 = new VBox(20, new Label(" "));
        spacer.setPadding(new Insets(10, 10, 10 ,10));
        VBox spacer3 = new VBox(20, new Label(" "));
        spacer.setPadding(new Insets(10, 10, 10 ,10));
        HBox buttonReplayBox = new HBox();
        buttonReplayBox.getChildren().addAll(replayButton, spacer2, leaderboardButton, spacer3, exitButton);
        buttonReplayBox.setAlignment(Pos.CENTER);
        buttonReplayBox.setPadding(new Insets(20));

        scoreBox.getChildren().addAll(endLabel, playerLabel, scoreTimeLabelBox, buttonReplayBox);
        //profile
        if("Classic".equals(mode) && "Easy".equals(difficulty)){
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            String formattedTime = String.format("%02d:%02d", minutes, remainingSeconds);
            totalTimeLabel.setText(formattedTime);
            Firebase.savePlayerScore(playerName, totalScore, "Classic", "Easy");
        } else if ("Classic".equals(mode) && "Medium".equals(difficulty)) {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            String formattedTime = String.format("%02d:%02d", minutes, remainingSeconds);
            totalTimeLabel.setText(formattedTime);
            Firebase.savePlayerScore(playerName, totalScore, "Classic", "Medium");
        } else if ("Classic".equals(mode) && "Hard".equals(difficulty)) {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            String formattedTime = String.format("%02d:%02d", minutes, remainingSeconds);
            totalTimeLabel.setText(formattedTime);
            Firebase.savePlayerScore(playerName, totalScore, "Classic", "Hard");
        } else if ("Blitz".equals(mode) && "Easy".equals(difficulty)) {
            Firebase.savePlayerScore(playerName, totalScore, "Blitz", "Easy");
        }  else if ("Blitz".equals(mode) && "Medium".equals(difficulty)) {
            Firebase.savePlayerScore(playerName, totalScore, "Blitz", "Medium");
        } else if ("Blitz".equals(mode) && "Hard".equals(difficulty)) {
            Firebase.savePlayerScore(playerName, totalScore, "Blitz", "Hard");
        } else if ("Endless".equals(mode) && "Easy".equals(difficulty)) {
            Firebase.savePlayerScore(playerName, totalScore, "Endless", "Easy");
        } else if ("Endless".equals(mode) && "Medium".equals(difficulty)) {
            Firebase.savePlayerScore(playerName, totalScore, "Endless", "Medium");
        } else if ("Endless".equals(mode) && "Hard".equals(difficulty)) {
            Firebase.savePlayerScore(playerName, totalScore, "Endless", "Hard");
        }
        // Add the score pane to the main root
        quizRoot.getChildren().add(scoreBox);
    }

    private Label createLabel(String text, String style) { //create label
        Label label = new Label(text);
        label.setStyle(style);
        return label;
    }

    private void loadQuestionItems() { // maps question from json file
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            if (triviaFilePath == null) throw new IOException("Trivia questions file path is null");
            QuestionResponse questionResponse = objectMapper.readValue(Files.newInputStream(triviaFilePath), QuestionResponse.class);
            this.questions = questionResponse.getResults();
            System.out.println("Loaded questions: " + questions.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private int switchBasePoint(){ //switches basepoint depending on difficulty
        int BASE_POINT = 100;
        switch(difficulty){
            case "Easy": {
                BASE_POINT = 100;
                break;
            }
            case "Medium": {
                BASE_POINT = 500;
                break;
            }
            case "Hard": {
                BASE_POINT = 1000;
                break;
            }
        }
        return BASE_POINT;
    }
    private int calculateStreakBonus(){
        return streakCount > 0 ? streakCount * this.streakBonusPoint : 0;
    }

    /////////////////////////////////////////////////////////////BLITZ MODE//////////////////////////////////////////////////
    private Label timerLabel = new Label("Time: 20");
    private long startTime;

    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }
        startTime = System.currentTimeMillis();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            int remainingTime = questionTime + timeBonus - timePenalty;

            @Override
            public void run() {
                if (remainingTime <= 0) {
                    cancel();
                    Platform.runLater(() -> {
                        switch (difficulty){
                            // Deduct points for incorrect answer
                            case "Easy": {
                                score -= 100;
                                break;
                            }
                            case "Medium": {
                                score -= 500;
                                break;
                            }
                            case "Hard": {
                                score -= 1000;
                                break;
                            }
                        }// Apply penalty for timeout
                        scoreLabel.setText("Score: " + score);

                        if (questionIndex < questions.size()) {
                            displayQuestion(questions.get(questionIndex));
                        } else {
                            showScore(score, totalQuizTime);
                        }
                    });
                } else {
                    remainingTime--;
                    Platform.runLater(() -> timerLabel.setText("Time: " + remainingTime));
                }
            }
        }, 0, 1000);
    }

    private void resetTimer() {
        if (timer != null) {
            timer.cancel();
        }
        startTimer(); // Restart timer
        timeBonus = 0;
        timePenalty = 0;
    }

    private int timeBonus = 0;
    private int timePenalty = 0;
    private int questionTime = 15; // Base question time
    private int streakBonusPoint = 100;
    private Timer timer;

    private void blitzScoringSystem(boolean correct, int timeTaken, int remainingTime, int totalTime) {
        int BASE_POINT = switchBasePoint();
        double speedFactor = (double) remainingTime/ totalTime;
        int speedPoints = (int) (BASE_POINT * speedFactor);
        int streakBonus = calculateStreakBonus();
        score += speedPoints + streakBonus;
        if (correct) {
            if (timeTaken <= 5) {
                timeBonus += 5;
            } else if (timeTaken <= 10) {
                timeBonus += 3;
            } else if (timeTaken <= 15) {
                timeBonus += 1;
            } else if (timeTaken <= 20) {
                timePenalty += 3;
            } else {
                timePenalty += 5;
            }
        } else {
            switch (difficulty){
                case "Easy": {
                    score -= 100;
                    break;
                }
                case "Medium": {
                    score -= 500;
                    break;
                }
                case "Hard": {
                    score -= 1000;
                    break;
                }
            }
        }
        // Update score display
        scoreLabel.setText("Score: " + score);
    }

////////////////////////////////////////////////////////////////// CLASSIC MODE /////////////////////////////////////////////
    private Label timeLabel = new Label("00:00");

    private int seconds = 0;
    private Timeline timeline;
    private boolean isRunning = false;
    private void classicScoringSystem(){
        int basePoint = switchBasePoint();
        int streakBonus = calculateStreakBonus();
        System.out.println(streakCount);
        score += basePoint + streakBonus;
        scoreLabel.setText("Score: " + score);
    }
    private void startStopwatch(Label timeLabel) {
        if (!isRunning) {
            timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTime(timeLabel)));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
            isRunning = true;
        }else {
            timeline.play();
        }
    }
    private void stopStopwatch() {
        if (isRunning) {
            timeline.stop();
            isRunning = false;
        }
    }

    private void updateTime(Label timeLabel) {
        seconds++;
        updateLabel(timeLabel);
    }

    private void updateLabel(Label timeLabel) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        timeLabel.setText(String.format("%02d:%02d", minutes, remainingSeconds));
    }

    ////////////////////////////////////////////////////ENDLESS//////////////////////////////
    int lives = 3;
    Label liveslabel = new Label();
    private void endlessScoringSystem(){
        int basePoint = switchBasePoint();
        int streakBonus = calculateStreakBonus();
        score += basePoint + streakBonus;
        scoreLabel.setText("Score: " + score);
    }
    private void fetchNewQuestions() {
        // Ensure API URL is set
        if (apiUrl == null || apiUrl.isEmpty()) {
            System.out.println("API URL is not set.");
            return;
        }

        // Perform API request asynchronously
        Task<Void> apiTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .GET()
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // Save response to a temporary file
                triviaFilePath = Files.createTempFile("trivia_questions", ".json");
                Files.writeString(triviaFilePath, response.body(), StandardCharsets.UTF_8);

                Platform.runLater(() -> {
                    loadQuestionItems(); // Load questions from the file
                    questionIndex = 0;  // Reset question index after fetching new questions
                    if (questions.size() > 0) {
                        displayQuestion(questions.get(questionIndex)); // Display the first question from the new set
                    }
                });
                return null;
            }
        };

        Thread thread = new Thread(apiTask);
        thread.setDaemon(true);
        thread.start();
    }

}
