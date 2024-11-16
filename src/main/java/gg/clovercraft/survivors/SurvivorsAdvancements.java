package gg.clovercraft.survivors;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;

import java.util.Objects;

public class SurvivorsAdvancements {

    public static final String GETTING_RISKY = "survivors_adv_getting_risky";
    public static final String DEATHS_KISS = "survivors_adv_deaths_kiss";
    public static final String LIFESTEAL = "survivors_adv_lifesteal";

    public MinecraftServer server;
    public Scoreboard scoreboard;

    public static void register(MinecraftServer minecraftServer) {
        SurvivorsAdvancements advancements = new SurvivorsAdvancements(minecraftServer);
        advancements.loadScoreboards();
    }

    public SurvivorsAdvancements(MinecraftServer server) {
        this.server = server;
        this.scoreboard = server.getScoreboard();
    }

    public void loadScoreboards()
    {
        // create achievement scoreboards
        Survivors.LOGGER.info("Creating advancement scoreboards");
        scoreboard.addTeam(GETTING_RISKY);
        scoreboard.addTeam(DEATHS_KISS);
        scoreboard.addTeam(LIFESTEAL);
    }

    public static void grantAdvancement(PlayerEntity player, String advancement) {
        MinecraftServer server = player.getServer();
        assert server != null;
        SurvivorsAdvancements advancements = new SurvivorsAdvancements(server);
        Objects.requireNonNull(advancements.scoreboard.getTeam(advancement)).getPlayerList().add(player.getNameForScoreboard());
    }
}
