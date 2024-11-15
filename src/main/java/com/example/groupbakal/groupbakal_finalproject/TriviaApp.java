package com.example.groupbakal.groupbakal_finalproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;


public class TriviaApp extends Application {
    private TriviaController controller;
    //===================================================================================================
    // Sir, if it doesn't load, please try reloading the dependencies in the pom.xml and refresh Maven.
    //===================================================================================================
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Start.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.setStage(stage);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Trivia Quiz");
        stage.show();
        Font.loadFont(getClass().getResourceAsStream("/LuckiestGuy-Regular.ttf"), 12);
    }



    public static void main(String[] args) {
        launch();
    }
}
