package gg.clovercraft.survivors;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.*;
import net.minecraft.scoreboard.ScoreboardCriterion.RenderType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

public class SurvivorsAdvancements {

    public static final String GETTING_RISKY = "survivors_adv_getting_risky";
    public static final String DEATHS_KISS = "survivors_adv_deaths_kiss";
    public static final String LIFESTEAL = "survivors_adv_lifesteal";
    public static final String HEALING_GIFT = "survivors_adv_healing_gift";
    public static final String MIRACLE_WORKER = "survivors_adv_miracle_worker";
    public static final String FIRST_BLOOD = "survivors_adv_first_blood";
    public static final String KILLSTREAK = "survivors_adv_killstreak";
    public static final String FINALIST = "survivors_adv_finalist";
    public static final String VICTOR = "survivors_adv_victor";

    public MinecraftServer server;
    public Scoreboard scoreboard;

    public static void register(MinecraftServer minecraftServer) {
        SurvivorsAdvancements advancements = new SurvivorsAdvancements(minecraftServer);
        advancements.loadScores();
    }

    public static void grantAdvancement(PlayerEntity player, String advancement) {
        MinecraftServer server = player.getServer();
        assert server != null;
        Collection<ScoreboardObjective> objectives = server.getScoreboard().getObjectives();
        objectives.forEach(objective -> {
            if(objective.getName().equals(advancement)) {
                ScoreAccess score = player.getScoreboard().getOrCreateScore(ScoreHolder.fromName(player.getNameForScoreboard()), objective);
                score.setScore(1);
            }
        });
    }

    public static void checkGlobals(MinecraftServer server) {
        StateSaverLoader state = StateSaverLoader.getServerState(server);

        int total = state.totalPlayers;
        int killed = state.playersEliminated;
        final int finalistCount = 3;
        final int victorCount = 1;

        if (killed > 1) {
            // at least one player has been eliminated
            int remaining = total - killed;

            if (remaining <= finalistCount) {
                // three or fewer players remain. grant finalist advancement
                state.players.forEach((uuid, data) -> {
                    if (data.lives > 0 ) {
                        PlayerEntity player = server.getPlayerManager().getPlayer(uuid);
                        assert player != null;
                        SurvivorsAdvancements.grantAdvancement(player, FINALIST);
                    }
                });
            }

            if (remaining == victorCount) {
                // last player alive gets the victor advancement
                state.players.forEach((uuid, data) -> {
                    if (data.lives > 0 ) {
                        PlayerEntity player = server.getPlayerManager().getPlayer(uuid);
                        assert player != null;
                        SurvivorsAdvancements.grantAdvancement(player, VICTOR);
                    }
                });
            }
        }
    }

    public SurvivorsAdvancements(MinecraftServer server) {
        this.server = server;
        this.scoreboard = server.getScoreboard();
    }

    public void loadScores()
    {
        // create achievement scoreboards
        Survivors.LOGGER.info("Creating advancement scoreboards");
        addScore(GETTING_RISKY, "Getting Risky");
        addScore(DEATHS_KISS, "Deaths Kiss");
        addScore(LIFESTEAL, "Lifesteal");
        addScore(HEALING_GIFT, "Healing Gift");
        addScore(MIRACLE_WORKER, "Miracle Worker");
        addScore(FIRST_BLOOD, "First Blood");
        addScore(KILLSTREAK, "Kill Streak");
        addScore(FINALIST, "Finalist");
        addScore(VICTOR, "Victor");
    }

    public void addScore(String score, String display) {
        AtomicBoolean scoreExists = new AtomicBoolean(false);
        Collection<ScoreboardObjective> objectives = scoreboard.getObjectives();
        objectives.forEach(objective -> {

            if (objective.getName().equals(score)) {
                // skip this, already exists
                scoreExists.set(true);
            }
        });
        if (!scoreExists.get()) {
            scoreboard.addObjective(score, ScoreboardCriterion.TRIGGER, Text.literal(display), RenderType.INTEGER, false, null);
        }
    }
}
