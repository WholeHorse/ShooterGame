package org.example.shootergame.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class WelcomeController {
    @FXML
    private TextField nicknameField;
    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;

    private void connectServer() {
        try {
            clientSocket = new Socket("localhost", 7777);
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Сервер не доступен.");
            alert.showAndWait();
        }
    }

    private void loginGame() {
        try {
            out.writeUTF(nicknameField.getText());
            String response = in.readUTF();
            if (response.equals("OK")) {
                FXMLLoader fxmlLoader = new FXMLLoader(ShooterGame.class.getResource("/org/example/shootergame/shooter-game-view.fxml"));
                Scene scene = new Scene(fxmlLoader.load());
                Stage stage = (Stage) nicknameField.getScene().getWindow();
                stage.setScene(scene);

                GameClient client = fxmlLoader.getController();
                client.connectServer(clientSocket, in, out);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, response);
                alert.showAndWait();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void onConnectButtonClick() {
        if (clientSocket == null) {
            connectServer();
        }
        if (clientSocket != null) {
            loginGame();
        }
    }
}
