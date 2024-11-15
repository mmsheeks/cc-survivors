package gg.clovercraft.survivors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

public class DeathHandler {

    private final DamageSource source;
    private final PlayerEntity player;
    private final PlayerData state;

    public static void onPlayerDeath(LivingEntity entity, DamageSource source) {
        PlayerEntity player = (PlayerEntity) entity;
        DeathHandler handler = new DeathHandler(player, source);
        handler.onDeath();
    }

    public DeathHandler(PlayerEntity player, DamageSource source) {
        this.player = player;
        this.source = source;
        this.state = StateSaverLoader.getPlayerState(player);
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

        if (this.state.lives == 0) {
            endRun();
        } else {
            sendDeathMessage();
        }
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
    }

    private void handleKiller( Entity attacker ) {
        PlayerEntity killer = (PlayerEntity) attacker;
        PlayerData killerState = StateSaverLoader.getPlayerState(killer);

        if(killerState.lives == 2 && this.state.lives >= 4 ) {
            killerState.addLife(true);
            Scoreboards.updatePlayerTeam(killer, killerState);
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) killer;
            serverPlayer.sendMessage(Text.literal("Congratulations. You have restored one life. You are green once more."));
            return;
        }

        if(killerState.lives == 1) {
            killerState.addLife(true);
            Scoreboards.updatePlayerTeam(killer, killerState);
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) killer;
            serverPlayer.sendMessage(Text.literal("Congratulations. You have restored one life and are once more Yellow. You may now only target dark green lives."));
        }
    }
}
