package org.example.shootergame.server;

import com.google.gson.Gson;
import org.example.shootergame.db.SessionFactoryBuilder;
import org.example.shootergame.network.State;
import org.example.shootergame.common.GameState;
import org.example.shootergame.common.LeaderboardInfo;
import org.example.shootergame.model.ArrowInfo;
import org.example.shootergame.model.TargetInfo;
import org.example.shootergame.common.GameInfo;
import org.example.shootergame.model.PlayerInfo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.sqrt;

public class GameServer {
    public static final Gson gson = new Gson();
    private static final Random rand = new Random();
    private static final String[] colors = {
            "#1E90FF", // Ярко-синий
            "#FF4500", // Оранжево-красный
            "#32CD32", // Зеленый
            "#8B008B", // Темно-пурпурный
    };
    private static final double height = 540;
    private static final double width = 650;
    private GameState state = GameState.OFF;
    private final GameInfo gameInfo = new GameInfo(height);
    private final List<PlayerHandler> handlerList = new ArrayList<>();
    private Thread nextThread;


    public static void main(String[] args) {
        // Initialize the SessionFactory
        SessionFactoryBuilder.getSessionFactory();

        GameServer server = new GameServer();
        server.start(7777);
    }

    // Rest of the code remains unchanged
    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new PlayerHandler(this, clientSocket);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ... Rest of the existing methods ...
    public void removePlayer(PlayerHandler handler) {
        handlerList.remove(handler);
        if (handler.getPlayerInfo() != null) {
            if (nextThread != null && nextThread.isAlive()) {
                nextThread.interrupt();
            }
            gameInfo.playerList.remove(handler.getPlayerInfo());
            sendRemove(handler.getPlayerInfo());
            startGame();
        }
    }

    private void sendRemove(PlayerInfo p) {
        State state = new State(State.Type.Remove, p.nickname);
        String json = gson.toJson(state);
        for (PlayerHandler h : handlerList) {
            h.sendMessage(json);
        }
    }

    public boolean containsNickname(String nickname) {
        for (PlayerInfo p : gameInfo.playerList) {
            if (p.nickname.equals(nickname)) return true;
        }
        return false;
    }

    public void addPlayer(String nickname, PlayerHandler handler) {
        // Если это клиент для просмотра таблицы лидеров
        if (nickname.equals("__leaderboard_viewer__")) {
            handlerList.add(handler);
            // Сразу отправляем таблицу лидеров
            sendLeaderboard(handler);
            return;
        }

        // Обычная логика для игроков
        String color = colors[rand.nextInt(colors.length)];
        while (containsColor(color)) color = colors[rand.nextInt(colors.length)];

        PlayerInfo newPlayer = PlayerInfo.loadOrCreateByName(nickname);
        newPlayer.color = color;
        gameInfo.playerList.add(newPlayer);
        handler.setPlayerInfo(newPlayer);

        sendNewPlayer(newPlayer);
        handlerList.add(handler);

        String jsonInfo = gson.toJson(gameInfo);
        handler.sendMessage(jsonInfo);
    }

    private boolean containsColor(String color) {
        for (PlayerInfo p : gameInfo.playerList) {
            if (p.color.equals(color)) return true;
        }
        return false;
    }

    private void sendNewPlayer(PlayerInfo p) {
        String jsonPlayer = gson.toJson(p);
        State state = new State(State.Type.New, jsonPlayer);
        String json = gson.toJson(state);
        for (PlayerHandler h : handlerList) {
            h.sendMessage(json);
        }
    }

    public void sendWantToStart(PlayerHandler handler) {
        State wantToStart = new State(State.Type.WantToStart, handler.getPlayerInfo().nickname);
        String json = gson.toJson(wantToStart);
        for (PlayerHandler h : handlerList) {
            h.sendMessage(json);
        }
    }

    private boolean allWantToStart() {
        for (PlayerInfo p : gameInfo.playerList)
            if (!p.wantToStart) return false;
        return true;
    }

    private void sendState() {
        String jsonState = gson.toJson(this.state);
        State state = new State(State.Type.State, jsonState);
        String json = gson.toJson(state);
        for (PlayerHandler h : handlerList) {
            h.sendMessage(json);
        }
    }

    public void startGame() {
        if (allWantToStart() && !gameInfo.playerList.isEmpty()) {
            setArrowStartY();
            state = GameState.ON;
            sendState();
            nextThread = new Thread(() -> {
                try {
                    while (!isGameOver()) {
                        if (state == GameState.PAUSE) pause();
                        next();
                        sendGameInfo(State.Type.Update);
                        Thread.sleep(16);
                    }
                    sendWinner();
                } catch (InterruptedException e) {
                    sendStop();
                } finally {
                    resetInfo();
                    sendGameInfo(State.Type.Reset);
                    state = GameState.OFF;
                    sendState();
                }
            });
            nextThread.setDaemon(true);
            nextThread.start();
        }
    }

    private void sendStop() {
        State state = new State(State.Type.Stop, null);
        String json = gson.toJson(state);
        for (PlayerHandler h : handlerList) {
            h.sendMessage(json);
        }
    }

    private PlayerInfo findWinner() {
        PlayerInfo winner = gameInfo.playerList.getFirst();
        for (PlayerInfo p : gameInfo.playerList) {
            if (p.score > winner.score) winner = p;
        }
        return winner;
    }

    private void sendWinner() {
        PlayerInfo winner = findWinner();
        winner.increaseWins();
        String jsonWinner = gson.toJson(winner);
        State state = new State(State.Type.Winner, jsonWinner);
        String json = gson.toJson(state);
        for (PlayerHandler p : handlerList) {
            p.sendMessage(json);
        }
    }

    private void resetInfo() {
        for (PlayerInfo p : gameInfo.playerList) {
            p.score = 0;
            p.shots = 0;
            p.shooting = false;
            p.wantToPause = false;
            p.wantToStart = false;
            p.arrow.x = 5.0;
        }
        gameInfo.bigCircle.y = 0.5 * height;
        gameInfo.bigCircle.direction = 1;
        gameInfo.smallCircle.y = 0.5 * height;
        gameInfo.smallCircle.direction = 1;
    }

    private boolean isGameOver() {
        for (PlayerInfo p : gameInfo.playerList) {
            if (p.score > 5) return true;
        }
        return false;
    }

    private void sendGameInfo(State.Type type) {
        String jsonInfo = gson.toJson(gameInfo);
        State state = new State(type, jsonInfo);
        String json = gson.toJson(state);
        for (PlayerHandler h : handlerList) {
            h.sendMessage(json);
        }
    }

    private void setArrowStartY() {
        final int div = gameInfo.playerList.size() / 2;
        final int mod = gameInfo.playerList.size() % 2;
        for (int i = 0; i < gameInfo.playerList.size(); ++i) {
            gameInfo.playerList.get(i).arrow.y = 0.5 * height + 50.0 * (i - div) + (1 - mod) * 25.0;
        }
    }

    private void next() {
        nextCirclePos(gameInfo.bigCircle);
        nextCirclePos(gameInfo.smallCircle);
        for (PlayerInfo p : gameInfo.playerList) {
            if (p.shooting) {
                p.arrow.x += p.arrow.moveSpeed;
                if (hit(p.arrow, gameInfo.bigCircle)) {
                    ++p.score;
                    p.shooting = false;
                    p.arrow.x = 5.0;
                } else if (hit(p.arrow, gameInfo.smallCircle)) {
                    p.score += 2;
                    p.shooting = false;
                    p.arrow.x = 5.0;
                } else if (p.arrow.x + 45.0 > width) {
                    p.shooting = false;
                    p.arrow.x = 5.0;
                }
            }
        }
    }

    private void nextCirclePos(TargetInfo c) {
        if (c.y + c.radius + c.moveSpeed > height || c.y - c.radius - c.moveSpeed < 0.0) c.direction *= -1;
        c.y += c.direction * c.moveSpeed;
    }

    boolean hit(ArrowInfo a, TargetInfo c) {
        return sqrt((a.x + 45.0 - c.x) * (a.x + 45.0 - c.x) + (a.y - c.y) * (a.y - c.y)) < c.radius;
    }

    synchronized void resume() {
        this.notifyAll();
    }

    synchronized void pause() throws InterruptedException {
        this.wait();
    }

    private boolean allWantToPause() {
        for (PlayerInfo p : gameInfo.playerList) {
            if (!p.wantToPause) return false;
        }
        return true;
    }

    private boolean allWantToResume() {
        for (PlayerInfo p : gameInfo.playerList) {
            if (p.wantToPause) return false;
        }
        return true;
    }

    public void pauseGame() {
        switch (state) {
            case PAUSE:
                if (allWantToResume()) {
                    state = GameState.ON;
                    sendState();
                    resume();
                }
                break;
            case ON:
                if (allWantToPause()) {
                    state = GameState.PAUSE;
                    sendState();
                }
                break;
        }
    }

    public void sendWantToPause(PlayerHandler handler) {
        State state = new State(State.Type.WantToPause, handler.getPlayerInfo().nickname);
        String json = gson.toJson(state);
        for (PlayerHandler h : handlerList) {
            h.sendMessage(json);
        }
    }

    public boolean isGameStarted() {
        return state == GameState.ON || state == GameState.PAUSE;
    }

    public String canPlayerConnect(String nickname) {
        String response = "OK";

        // Специальная проверка для приложения на Android - клиент только для просмотра лидеров
        if (nickname.equals("__leaderboard_viewer__")) {
            return "OK";
        }

        // Стандартные проверки для игроков
        if (containsNickname(nickname))
            response = "Имя " + nickname + " уже используется, введите другое.\n";
        if (gameInfo.playerList.size() == 4)
            response = "Максимум игроков уже достигнут.\n";
        if (isGameStarted()) {
            response = "Игра в процессе. Подождите, пока она завершится.\n";
        }
        return response;
    }

    public void sendLeaderboard(PlayerHandler handler) {
        LeaderboardInfo leaderboard = new LeaderboardInfo(PlayerInfo.getAllPlayers());
        String jsonInfo = gson.toJson(leaderboard);
        State state = new State(State.Type.Leaderboard, jsonInfo);
        String json = gson.toJson(state);
        handler.sendMessage(json);
    }
}