package gg.clovercraft.survivors;

import net.minecraft.entity.player.PlayerEntity;

import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerData {
    public int lives = 5;
    public String lifeGiveTimestamp = "";

    public HashMap<UUID, PlayerData> players = new HashMap<>();

    public void addLife() {
        lives += 1;
    }

    public void subLife() {
        lives -= 1;
    }

    public boolean canGiveLife() {
        Date now = new Date();
        Date lastUse;
        boolean canUse = true;

        if (!Objects.equals(lifeGiveTimestamp, "")) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

            try {
                lastUse = formatter.parse(lifeGiveTimestamp);
                long daysSinceUse = dayDiff(now, lastUse);
                if (daysSinceUse < 1 ) {
                    canUse = false;
                }
            } catch (ParseException e) {
                Survivors.LOGGER.error("Error parsing last life give use time");
            }
        }

        return canUse;
    }

    public void flagGiveLife() {
        Date now = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String timestamp = formatter.format(now);
        lifeGiveTimestamp = timestamp;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public static long dayDiff(Date date1, Date date2) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MICROSECONDS);
    }
}
