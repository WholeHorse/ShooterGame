package org.example.shootergame.common;

import org.example.shootergame.model.TargetInfo;
import org.example.shootergame.model.PlayerInfo;

import java.util.ArrayList;
import java.util.List;

public class GameInfo {
    public final TargetInfo bigCircle;
    public final TargetInfo smallCircle;
    public final List<PlayerInfo> playerList = new ArrayList<>();

    public GameInfo(final double height) {
        bigCircle = new TargetInfo(493.0, 0.5 * height, 60.0, 2);
        smallCircle = new TargetInfo(599.0, 0.5 * height, 30.0, 4);
    }
}
