package org.example.shootergame;

import java.util.ArrayList;
import java.util.List;

public class GameInfo {
    public final CircleInfo bigCircle;
    public final CircleInfo smallCircle;
    public final List<PlayerInfo> playerList = new ArrayList<>();

    public GameInfo(final double height) {
        bigCircle = new CircleInfo(493.0, 0.5 * height, 60.0, 2);
        smallCircle = new CircleInfo(599.0, 0.5 * height, 30.0, 4);
    }
}
