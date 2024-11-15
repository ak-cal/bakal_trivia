package com.example.groupbakal.groupbakal_finalproject;

import javafx.collections.FXCollections;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.scene.control.*;
import javafx.fxml.FXML;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;


public class LeaderboardController {
    public Button backButton;
    @FXML
    private ComboBox<String> gameModeComboBox;

    @FXML
    private ListView<String> leaderboardListView;

    private FirebaseHelper firebaseHelper;

    @FXML
    private void back() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    public LeaderboardController() {
        firebaseHelper = new FirebaseHelper();
    }

    @FXML
    public void initialize() {
        gameModeComboBox.getItems().addAll("Classic", "Blitz", "Endless");
        gameModeComboBox.getSelectionModel().select("Choose Game Mode");
        gameModeComboBox.setOnAction(event -> {
            System.out.println("ComboBox selection changed");
            onGameModeSelected();
        });

        leaderboardListView.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    if (item.equals("Player - Difficulty - Score")) {
                        GridPane gridPane = new GridPane();
                        gridPane.setHgap(20);
                        gridPane.setVgap(10);

                        gridPane.getColumnConstraints().addAll(
                                new ColumnConstraints() {{
                                    setHalignment(HPos.LEFT);
                                    setPercentWidth(30);
                                }},
                                new ColumnConstraints() {{
                                    setHalignment(HPos.CENTER);
                                    setPercentWidth(30);
                                }},
                                new ColumnConstraints() {{
                                    setHalignment(HPos.RIGHT);
                                    setPercentWidth(40);
                                }}
                        );

                        //header
                        Label playerLabel = new Label("Player");
                        Label difficultyLabel = new Label("Difficulty");
                        Label scoreLabel = new Label("Score");

                        playerLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: BLACK; -fx-font-size: 14px;");
                        difficultyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: BLACK; -fx-font-size: 14px;");
                        scoreLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: BLACK; -fx-font-size: 14px;");

                        gridPane.add(playerLabel, 0, 0);
                        gridPane.add(difficultyLabel, 1, 0);
                        gridPane.add(scoreLabel, 2, 0);

                        setGraphic(gridPane);
                        //
                    } else {
                        String[] parts = item.split(" - ");
                        if (parts.length == 3) {
                            String playerName = parts[0];
                            String difficulty = parts[1];
                            String score = parts[2];

                            GridPane gridPane = new GridPane();
                            gridPane.setHgap(20);
                            gridPane.setVgap(10);
                            gridPane.getColumnConstraints().addAll(
                                    new ColumnConstraints() {{
                                        setHalignment(HPos.LEFT);
                                        setPercentWidth(30);
                                    }},
                                    new ColumnConstraints() {{
                                        setHalignment(HPos.CENTER);
                                        setPercentWidth(30);
                                    }},
                                    new ColumnConstraints() {{
                                        setHalignment(HPos.RIGHT);
                                        setPercentWidth(40);
                                    }}
                            );

                            Label playerNameLabel = new Label(playerName);
                            Label difficultyLabel = new Label(difficulty);
                            Label scoreLabel = new Label(score);

                            gridPane.add(playerNameLabel, 0, 0);
                            gridPane.add(difficultyLabel, 1, 0);
                            gridPane.add(scoreLabel, 2, 0);

                            setGraphic(gridPane);
                        }
                    }
                }
            }
        });
    }


    @FXML
    private void onGameModeSelected() {
        String selectedGameMode = gameModeComboBox.getValue();
        System.out.println("Selected game mode: " + selectedGameMode);
        fetchLeaderboardData(selectedGameMode);
    }

    private void fetchLeaderboardData(String gameMode) {
        System.out.println("Fetching leaderboard for: " + gameMode);

        firebaseHelper.getLeaderboardReference(gameMode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ObservableList<String> leaderboardData = FXCollections.observableArrayList();

                System.out.println("Data Snapshot: " + dataSnapshot.toString());
                leaderboardData.add("Player - Difficulty - Score");
                if (!dataSnapshot.exists()) {
                    leaderboardData.add("No leaderboard data available for this mode.");
                } else {

                    List<PlayerData> playerList = new ArrayList<>();
                    for (DataSnapshot difficultySnapshot : dataSnapshot.getChildren()) {
                        String difficulty = difficultySnapshot.getKey();

                        for (DataSnapshot playerSnapshot : difficultySnapshot.getChildren()) {
                            String playerName = playerSnapshot.child("name").getValue(String.class);
                            Integer score = playerSnapshot.child("score").getValue(Integer.class);

                            if (playerName != null && score != null) {
                                playerList.add(new PlayerData(playerName, difficulty, score));
                            }
                        }
                    }
                    //sort
                    playerList.sort((player1, player2) -> player2.getScore().compareTo(player1.getScore()));

                    for (PlayerData player : playerList) {
                        leaderboardData.add(player.getName() + " - " + player.getDifficulty() + " - " + player.getScore() + " pts");
                    }
                }

                System.out.println("Leaderboard Data: " + leaderboardData);

                javafx.application.Platform.runLater(() -> {
                    leaderboardListView.setItems(leaderboardData);
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Error fetching data: " + databaseError.getMessage());
            }
        });
    }
}

