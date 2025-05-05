package org.example.shootergame.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Border;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import org.example.shootergame.network.State;
import org.example.shootergame.model.Arrow;
import org.example.shootergame.common.GameState;
import org.example.shootergame.common.LeaderboardInfo;
import org.example.shootergame.server.ServerHandler;
import org.example.shootergame.common.GameInfo;
import org.example.shootergame.model.PlayerInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class GameClient {
    @FXML
    VBox triangleBox;
    @FXML
    VBox labelBox;
    @FXML
    Circle bigCircle;
    @FXML
    Circle smallCircle;
    @FXML
    Pane gamePane;
    ServerHandler serverHandler;
    GameState state = GameState.OFF;

    public void connectServer(Socket socket, DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        serverHandler = new ServerHandler(this, socket, dataInputStream, dataOutputStream);
    }

    @FXML
    void onStartButtonClick() {
        if (state == GameState.OFF) {
            serverHandler.sendAction(State.Type.WantToStart);
        }
    }

    @FXML
    void onPauseButtonClick() {
        if (state != GameState.OFF) {
            serverHandler.sendAction(State.Type.WantToPause);
        }
    }

    @FXML
    void onShootButtonClick() {
        if (state == GameState.ON) {
            serverHandler.sendAction(State.Type.Shoot);
        }
    }

    @FXML
    void onLeaderboardButtonClick() {
        serverHandler.sendAction(State.Type.Leaderboard);
    }


    public void setGameInfo(final GameInfo gameInfo) {
        for (PlayerInfo p : gameInfo.playerList) {
            addPlayer(p);
        }
    }

    public void addPlayer(final PlayerInfo p) {
        Platform.runLater(() -> {
            Polygon triangle = new Polygon(0.0, 0.0, 20.0, -20.0, 0.0, -40.0);
            triangle.setId(p.nickname + "Triangle");
            triangle.setFill(Color.valueOf(p.color));
            triangleBox.getChildren().add(triangle);

            Label status = new Label("Статус: не готов");
            status.setTextFill(Color.valueOf("#4c4f69"));
            status.setId(p.nickname + "Status");
            if (p.wantToStart) {
                status.setText("Статус: готов");
            }

            Label wins = new Label(p.nickname + " победы:");
            wins.setTextFill(Color.valueOf("#4c4f69"));
            wins.setId(p.nickname + "Wins");

            Label winsCount = new Label(String.valueOf(p.wins));
            winsCount.setTextFill(Color.valueOf("#4c4f69"));
            winsCount.setId(p.nickname + "WinsCount");

            Label score = new Label(p.nickname + " очки:");
            score.setTextFill(Color.valueOf("#4c4f69"));
            score.setId(p.nickname + "Score");

            Label scoreCount = new Label(String.valueOf(p.score));
            scoreCount.setTextFill(Color.valueOf("#4c4f69"));
            scoreCount.setId(p.nickname + "ScoreCount");

            Label shots = new Label(p.nickname + " выстрелы:");
            shots.setTextFill(Color.valueOf("#4c4f69"));
            shots.setId(p.nickname + "Shots");

            Label shotsCount = new Label(String.valueOf(p.shots));
            shotsCount.setTextFill(Color.valueOf("#4c4f69"));
            shotsCount.setId(p.nickname + "ShotsCount");

            VBox vbox = new VBox(0.0d, status, wins, winsCount, score, scoreCount, shots, shotsCount);
            vbox.setBorder(Border.stroke(Color.valueOf("#4c4f69")));
            vbox.setAlignment(Pos.CENTER);
            vbox.setId(p.nickname + "VBox");
            labelBox.getChildren().add(vbox);
        });
    }

    private Polygon findTriangle(final String nickname) {
        return (Polygon) gamePane.getScene().lookup("#" + nickname + "Triangle");
    }

    private Label findStatusLabel(final String nickname) {
        return (Label) gamePane.getScene().lookup("#" + nickname + "Status");
    }

    public void setPlayerWantToStart(final String nickname) {
        Platform.runLater(() -> {
            Label statusLabel = findStatusLabel(nickname);
            statusLabel.setText("Статус: готов");
        });
    }

    public void updateGameInfo(final GameInfo gameInfo) {
        Platform.runLater(() -> {
            bigCircle.setLayoutY(gameInfo.bigCircle.y);
            smallCircle.setLayoutY(gameInfo.smallCircle.y);
            for (PlayerInfo p : gameInfo.playerList) {
                Arrow playerArrow = findArrow(p.nickname);
                if (p.shooting) {
                    if (playerArrow == null) {
                        playerArrow = createArrow(p);
                        setShots(p);
                    }
                    playerArrow.setLayoutX(p.arrow.x);
                } else if (playerArrow != null) {
                    removeArrow(playerArrow);
                    setScore(p);
                }
            }
        });
    }

    private Arrow createArrow(final PlayerInfo p) {
        Arrow arrow = new Arrow(0, 0.0, 45, 0.0);
        arrow.setLayoutX(5);
        arrow.setLayoutY(p.arrow.y);
        arrow.setId(p.nickname + "Arrow");
        gamePane.getChildren().add(arrow);
        return arrow;
    }

    private Arrow findArrow(final String nickname) {
        return (Arrow) gamePane.getScene().lookup("#" + nickname + "Arrow");
    }

    private void removeArrow(final Arrow arrow) {
        gamePane.getChildren().remove(arrow);
    }

    private Label findScoreCountLabel(final String nickname) {
        return (Label) gamePane.getScene().lookup("#" + nickname + "ScoreCount");
    }

    private void setScore(final PlayerInfo p) {
        final Label scoreLabel = findScoreCountLabel(p.nickname);
        scoreLabel.setText(String.valueOf(p.score));
    }

    private Label findShotsCountLabel(final String nickname) {
        return (Label) gamePane.getScene().lookup("#" + nickname + "ShotsCount");
    }

    private void setShots(final PlayerInfo playerInfo) {
        Label shotsLabel = findShotsCountLabel(playerInfo.nickname);
        shotsLabel.setText(String.valueOf(playerInfo.shots));
    }

    public void updatePlayerWantToPause(final String nickname) {
        Platform.runLater(() -> {
            Label statusLabel = findStatusLabel(nickname);
            if (statusLabel.getText().equals("Статус: пауза")) {
                statusLabel.setText("Статус: готов");
            } else {
                statusLabel.setText("Статус: пауза");
            }
        });
    }

    private Label findWinsCountLabel(final String nickname) {
        return (Label) gamePane.getScene().lookup("#" + nickname + "WinsCount");
    }

    private void setWins(PlayerInfo p) {
        Label winsCount = findWinsCountLabel(p.nickname);
        winsCount.setText(String.valueOf(p.wins));
    }

    public void setState(final GameState state) {
        this.state = state;
    }

    public void showWinner(PlayerInfo p) {
        Platform.runLater(() -> {
            setWins(p);
            String info = p.nickname + " победил!\n" + p.nickname + " выиграл со счетом " + p.score + " очков.";
            Alert alert = new Alert(Alert.AlertType.INFORMATION, info);
            alert.show();
        });
    }

    public void resetGameInfo(final GameInfo gameInfo) {
        Platform.runLater(() -> {
            bigCircle.setLayoutY(gameInfo.bigCircle.y);
            smallCircle.setLayoutY(gameInfo.smallCircle.y);
            for (PlayerInfo p : gameInfo.playerList) {
                setShots(p);
                setScore(p);
                gamePane.getChildren().remove(findArrow(p.nickname));

                // Сбрасываем статус для всех игроков
                Label statusLabel = findStatusLabel(p.nickname);
                statusLabel.setText("Статус: не готов");
            }
        });
    }

    public void removePlayer(final String nickname) {
        Platform.runLater(() -> {
            gamePane.getChildren().remove(findArrow(nickname));
            triangleBox.getChildren().remove(findTriangle(nickname));
            labelBox.getChildren().remove(findVBox(nickname));
        });
    }

    private VBox findVBox(final String nickname) {
        return (VBox) gamePane.getScene().lookup("#" + nickname + "VBox");
    }

    public void showStop() {
        Platform.runLater(() -> {
            String info = "Игра прекратилась, т.к. другой игрок вышел.";
            Alert alert = new Alert(Alert.AlertType.WARNING, info);
            alert.show();
        });
    }

    public void showLeaderboard(LeaderboardInfo info) {
        Platform.runLater(() -> {
            final TableView<PlayerInfo> tableView = new TableView<>(FXCollections.observableList(info.getAllPlayers()));
            final TableColumn<PlayerInfo, String> nameColumn = new TableColumn<>("Имя");
            final TableColumn<PlayerInfo, Integer> winsColumn = new TableColumn<>("Победы");
            nameColumn.setCellValueFactory(new PropertyValueFactory<PlayerInfo, String>("nickname"));
            winsColumn.setCellValueFactory(new PropertyValueFactory<PlayerInfo, Integer>("wins"));
            winsColumn.setSortType(TableColumn.SortType.DESCENDING);
            tableView.getColumns().addAll(nameColumn, winsColumn);
            tableView.getSortOrder().add(winsColumn);
            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

            Stage stage = new Stage();
            Scene scene = new Scene(tableView);
            stage.setScene(scene);
            stage.setTitle("Таблица лидеров");
            stage.show();
        });
    }
}