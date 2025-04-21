package org.example.shootergame.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ShooterGame extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(ShooterGame.class.getResource("/org/example/shootergame/welcome-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage.sizeToScene();
        primaryStage.setResizable(false);
        primaryStage.setTitle("ShooterGame");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}