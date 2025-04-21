package org.example.shootergame.common;

import org.example.shootergame.model.PlayerInfo;

import java.util.List;

public class LeaderboardInfo {
    private final List<PlayerInfo> allPlayers;

    public LeaderboardInfo(List<PlayerInfo> allPlayers) {
        this.allPlayers = allPlayers;
    }

    public List<PlayerInfo> getAllPlayers() {
        return allPlayers;
    }
}
