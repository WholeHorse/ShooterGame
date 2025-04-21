package org.example.shootergame.model;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class PlayerInfo {
    private static final Map<String, PlayerInfo> allPlayers = new HashMap<>();

    public String nickname;
    public int wins = 0;
    public String color;
    public ArrowInfo arrow = new ArrowInfo();
    public int shots = 0;
    public int score = 0;
    public boolean wantToPause = false;
    public boolean wantToStart = false;
    public boolean shooting = false;

    public PlayerInfo() {
    }

    public PlayerInfo(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public int getWins() {
        return wins;
    }

    public static PlayerInfo loadOrCreateByName(String nickname) {
        PlayerInfo player = allPlayers.get(nickname);
        if (player == null) {
            player = new PlayerInfo(nickname);
            allPlayers.put(nickname, player);
        }
        return player;
    }

    public void increaseWins() {
        ++wins;
    }

    public static List<PlayerInfo> getAllPlayers() {
        return new ArrayList<>(allPlayers.values());
    }
}