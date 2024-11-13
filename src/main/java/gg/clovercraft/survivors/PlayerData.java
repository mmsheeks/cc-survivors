package gg.clovercraft.survivors;

import java.util.HashMap;
import java.util.UUID;

public class PlayerData {
    public int lives = 5;

    public HashMap<UUID, PlayerData> players = new HashMap<>();

    public void addLife() {
        lives += 1;
    }

    public void subLife() {
        lives -= 1;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }
}
