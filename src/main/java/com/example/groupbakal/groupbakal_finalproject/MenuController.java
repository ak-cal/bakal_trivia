package com.example.groupbakal.groupbakal_finalproject;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javafx.scene.control.ComboBox;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MenuController {

    private TriviaController quiz = new TriviaController();

    @FXML
    private ComboBox<String> modeBox;
    @FXML
    private ComboBox<String> difficultyBox;
    @FXML
    private ComboBox<String> categoryBox;

    private Map<String, String> urlMap;
    @FXML
    public void initialize() {
        modeBox.getItems().addAll("Classic", "Blitz", "Endless");
        difficultyBox.getItems().addAll("Easy", "Medium", "Hard");
        categoryBox.getItems().addAll("General", "Sports");

        initializeUrlMap();
    }
    private void initializeUrlMap() {
        urlMap = new HashMap<>();
        // Classic mode URLs
        urlMap.put("Classic-Easy-General", "https://opentdb.com/api.php?amount=10&category=9&difficulty=easy&type=multiple");
        urlMap.put("Classic-Easy-Sports", "https://opentdb.com/api.php?amount=10&category=21&difficulty=easy&type=multiple");
        urlMap.put("Classic-Medium-General", "https://opentdb.com/api.php?amount=10&category=9&difficulty=medium&type=multiple");
        urlMap.put("Classic-Medium-Sports", "https://opentdb.com/api.php?amount=10&category=21&difficulty=medium&type=multiple");
        urlMap.put("Classic-Hard-General", "https://opentdb.com/api.php?amount=10&category=9&difficulty=hard&type=multiple");
        urlMap.put("Classic-Hard-Sports", "https://opentdb.com/api.php?amount=10&category=21&difficulty=hard&type=multiple");
        // Blitz mode URLs
        urlMap.put("Blitz-Easy-General", "https://opentdb.com/api.php?amount=10&category=9&difficulty=easy&type=multiple");
        urlMap.put("Blitz-Easy-Sports", "https://opentdb.com/api.php?amount=10&category=21&difficulty=easy&type=multiple");
        urlMap.put("Blitz-Medium-General", "https://opentdb.com/api.php?amount=10&category=9&difficulty=medium&type=multiple");
        urlMap.put("Blitz-Medium-Sports", "https://opentdb.com/api.php?amount=10&category=21&difficulty=medium&type=multiple");
        urlMap.put("Blitz-Hard-General", "https://opentdb.com/api.php?amount=10&category=9&difficulty=hard&type=multiple");
        urlMap.put("Blitz-Hard-Sports", "https://opentdb.com/api.php?amount=10&category=21&difficulty=hard&type=multiple");
        // Endless
        urlMap.put("Endless-Easy-General", "https://opentdb.com/api.php?amount=30&category=9&difficulty=easy&type=multiple");
        urlMap.put("Endless-Easy-Sports", "https://opentdb.com/api.php?amount=20&category=21&difficulty=easy&type=multiple");
        urlMap.put("Endless-Medium-General", "https://opentdb.com/api.php?amount=30&category=9&difficulty=medium&type=multiple");
        urlMap.put("Endless-Medium-Sports", "https://opentdb.com/api.php?amount=20&category=21&difficulty=medium&type=multiple");
        urlMap.put("Endless-Hard-General", "https://opentdb.com/api.php?amount=30&category=9&difficulty=hard&type=multiple");
        urlMap.put("Endless-Hard-Sports", "https://opentdb.com/api.php?amount=20&category=21&difficulty=hard&type=multiple");
    }
    public String modeValue;
    public String difficultyValue;
    public String categoryValue;
    @FXML
    private void handleComboBox() {
        modeValue = modeBox.getValue();
        difficultyValue = difficultyBox.getValue();
        categoryValue = categoryBox.getValue();
    }
    private String key;
    private String URLlink;
    private Stage stage;
    @FXML
    private void switchToLoading(ActionEvent event) throws IOException {
        key = modeValue + "-" + difficultyValue + "-" + categoryValue;
        URLlink = urlMap.get(key);

        if (URLlink != null) {
            System.out.println(URLlink);
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            quiz.setStage(stage);

            callApi(URLlink, () -> {
                try {
                    quiz.setMode(modeValue);
                    quiz.setDifficulty(difficultyValue);
                    quiz.setApiUrl(URLlink);
                    quiz.setTriviaFilePath(triviaFilePath);

                    quiz.switchToQuiz();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            System.out.println("No URL found for the selected combination.");
        }
    }
    Path triviaFilePath;
    void callApi(String URLlink, Runnable onComplete) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(URLlink))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                // Save response to a temporary file
                triviaFilePath = Files.createTempFile("trivia_questions", ".json");
                Files.writeString(triviaFilePath, response.body(), StandardCharsets.UTF_8);

                Platform.runLater(() -> {
                    quiz.setTriviaFilePath(triviaFilePath);
                    quiz.setApiUrl(URLlink);
                    System.out.println("Response saved to: " + triviaFilePath.toAbsolutePath());

                    onComplete.run();
                });

                return null;
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

}
