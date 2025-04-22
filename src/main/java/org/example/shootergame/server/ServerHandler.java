package org.example.shootergame.server;

import com.google.gson.Gson;
import org.example.shootergame.client.GameClient;
import org.example.shootergame.common.GameState;
import org.example.shootergame.common.LeaderboardInfo;
import org.example.shootergame.common.GameInfo;
import org.example.shootergame.model.PlayerInfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerHandler extends Thread {
    private final Gson gson = new Gson();
    private final GameClient gameClient;
    private final Socket clientSocket;
    private final DataInputStream in;
    private final DataOutputStream out;

    public ServerHandler(GameClient gameClient, Socket socket,
                         DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        this.gameClient = gameClient;
        clientSocket = socket;
        in = dataInputStream;
        out = dataOutputStream;
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        try {
            requestGameInfo();
            handlingMessage();
        } catch (IOException e) {
            throw new RuntimeException();
        } finally {
            downHandler();
        }
    }

    private void requestGameInfo() throws IOException {
        String jsonInfo = in.readUTF();
        GameInfo gameInfo = gson.fromJson(jsonInfo, GameInfo.class);
        gameClient.setGameInfo(gameInfo);
    }

    private void handlingMessage() throws IOException {
        while (true) {
            String msg = in.readUTF();
            org.example.shootergame.network.State state = gson.fromJson(msg, org.example.shootergame.network.State.class);
            switch (state.type()) {
                case New -> gameClient.addPlayer(gson.fromJson(state.info(), PlayerInfo.class));
                case WantToStart -> gameClient.setPlayerWantToStart(state.info());
                case State -> gameClient.setState(gson.fromJson(state.info(), GameState.class));
                case Update -> gameClient.updateGameInfo(gson.fromJson(state.info(), GameInfo.class));
                case WantToPause -> gameClient.updatePlayerWantToPause(state.info());
                case Winner -> gameClient.showWinner(gson.fromJson(state.info(), PlayerInfo.class));
                case Reset -> gameClient.resetGameInfo(gson.fromJson(state.info(), GameInfo.class));
                case Remove -> gameClient.removePlayer(state.info());
                case Stop -> gameClient.showStop();
                case Leaderboard -> gameClient.showLeaderboard(gson.fromJson(state.info(), LeaderboardInfo.class));
            }
        }
    }

    private void downHandler() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendAction(org.example.shootergame.network.State.Type actionType) {
        try {
            String json = gson.toJson(actionType);
            out.writeUTF(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
