package gg.clovercraft.survivors;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Formatting;

import java.util.Objects;

public class Scoreboards {

    MinecraftServer server;

    public static void register(MinecraftServer minecraftServer) {
        Scoreboards teams = new Scoreboards(minecraftServer);
        teams.setup();
    }

    public static void updatePlayerTeam(PlayerEntity player, PlayerData state) {
        ServerScoreboard scoreboard;

        try {
            scoreboard = Objects.requireNonNull(player.getServer()).getScoreboard();
        } catch (NullPointerException e) {
            Survivors.LOGGER.error("Failed to retrieve server scoreboard! This should never happen.");
            return;
        }
        switch(state.lives) {
            case 5:
            case 4:
                scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), scoreboard.getTeam("green"));
                break;
            case 3:
                scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), scoreboard.getTeam("lime"));
                break;
            case 2:
                scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), scoreboard.getTeam("yellow"));
                break;
            case 1:
                scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), scoreboard.getTeam("red"));
                break;
            case 0:
                scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), scoreboard.getTeam("gray"));
                break;
        }
    }

    public Scoreboards(MinecraftServer minecraftServer) {
        this.server = minecraftServer;
    }

    public void setup() {
        ServerScoreboard scoreboard = server.getScoreboard();

        // create teams
        scoreboard.addTeam("green").setColor(Formatting.DARK_GREEN);
        scoreboard.addTeam("lime").setColor(Formatting.GREEN);
        scoreboard.addTeam("yellow").setColor(Formatting.YELLOW);
        scoreboard.addTeam("red").setColor(Formatting.RED);
        scoreboard.addTeam("gray").setColor(Formatting.GRAY);
    }


}
