package gg.clovercraft.survivors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.util.Objects;

public class DeathHandler {

    private final DamageSource source;
    private final PlayerEntity player;
    private final PlayerData state;
    private final StateSaverLoader serverState;

    public static void onPlayerDeath(LivingEntity entity, DamageSource source) {
        PlayerEntity player = (PlayerEntity) entity;
        DeathHandler handler = new DeathHandler(player, source);
        handler.onDeath();
    }

    public DeathHandler(PlayerEntity player, DamageSource source) {
        this.player = player;
        this.source = source;
        this.state = StateSaverLoader.getPlayerState(player);
        this.serverState = StateSaverLoader.getServerState(Objects.requireNonNull(player.getServer()));
    }

    public static void afterDeath(PlayerEntity player) {
        PlayerData state = StateSaverLoader.getPlayerState(player);
        switch(state.lives) {
            case 2:
                SurvivorsAdvancements.grantAdvancement(player, SurvivorsAdvancements.GETTING_RISKY);
            case 1:
                SurvivorsAdvancements.grantAdvancement(player, SurvivorsAdvancements.DEATHS_KISS);
        }
    }

    public void onDeath() {
        // check if they were killed by another player
        Entity attacker = source.getAttacker();
        if( attacker != null && attacker.isPlayer() ) {
            handleKiller( attacker );
        }

        // handle player death
        this.state.lives -= 1;
        Scoreboards.updatePlayerTeam(this.player, this.state);

        if ( this.state.lives == 0 ) {
            endRun();
        } else {
            sendDeathMessage();
        }
        SurvivorsAdvancements.checkGlobals(this.player.getServer());
    }

    private void sendDeathMessage() {
        ServerPlayerEntity player = (ServerPlayerEntity)this.player;
        String message = "You have died! You have %s lives remaining.".formatted(this.state.lives);
        if (state.lives == 2) {
            message = "You have died! You are now a Yellow life. You may kill a dark green player to regain a life. Die again, and become Red.";
        }
        if (state.lives == 1 ) {
            message = "You have died! You are now a Red life. You must kill to regain a life. Everyone is fair game.";
        }
        player.sendMessage(Text.literal(message));
    }

    private void endRun() {
        ServerPlayerEntity player = (ServerPlayerEntity)this.player;
        player.changeGameMode(GameMode.SPECTATOR);
        player.sendMessage(Text.literal("You have died for the last time. Your run has ended."));
        serverState.playersEliminated += 1;
    }

    private void handleKiller( Entity attacker ) {
        PlayerEntity killer = (PlayerEntity) attacker;
        PlayerData killerState = StateSaverLoader.getPlayerState(killer);

        this.serverState.totalPlayersKilled += 1;
        if ( this.serverState.totalPlayersKilled == 1 ) {
            SurvivorsAdvancements.grantAdvancement(killer, SurvivorsAdvancements.FIRST_BLOOD);
        }

        if(killerState.lives == 2 && this.state.lives >= 4 ) {
            SurvivorsAdvancements.grantAdvancement(killer, SurvivorsAdvancements.LIFESTEAL);
            killerState.addLife(true);
            Scoreboards.updatePlayerTeam(killer, killerState);
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) killer;
            serverPlayer.sendMessage(Text.literal("Congratulations. You have restored one life. You are green once more."));
            return;
        }

        if(killerState.lives == 1) {
            SurvivorsAdvancements.grantAdvancement(killer, SurvivorsAdvancements.LIFESTEAL);
            killerState.addLife(true);
            Scoreboards.updatePlayerTeam(killer, killerState);
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) killer;
            serverPlayer.sendMessage(Text.literal("Congratulations. You have restored one life and are once more Yellow. You may now only target dark green lives."));
        }

        if (killerState.killCount == 5 ) {
            SurvivorsAdvancements.grantAdvancement(killer, SurvivorsAdvancements.KILLSTREAK);
        }
    }
}
