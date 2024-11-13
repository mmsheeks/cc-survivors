package gg.clovercraft.survivors;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.UUID;

public class Survivors implements ModInitializer {

    public static final String MOD_ID = "survivors";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final int MAX_LIVES = 5;

    @Override
    public void onInitialize() {

        LOGGER.info("setting up server commands");
        CommandRegistrationCallback.EVENT.register(Commands::registerAll);

        LOGGER.info("setting up scoreboards");
        ServerLifecycleEvents.SERVER_STARTED.register(Scoreboards::register);

        LOGGER.info("Setting up player events");
        // on join event
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PlayerData playerState = StateSaverLoader.getPlayerState(handler.getPlayer());
            Scoreboards.updatePlayerTeam(handler.getPlayer(),playerState);
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof PlayerEntity) {
                PlayerData playerState = StateSaverLoader.getPlayerState(entity);
                playerState.lives -= 1;
                Scoreboards.updatePlayerTeam((PlayerEntity) entity, playerState);

                if (playerState.lives == 0) {
                    setPlayerDead(entity);
                } else {
                    ServerPlayerEntity player = (ServerPlayerEntity)entity;
                    String message = "You have died! You have %s lives remaining.".formatted(playerState.lives);
                    if (playerState.lives == 1 ) {
                        message = "You have died! You have 1 life remaining.";
                    }
                    player.sendMessage(Text.literal(message));
                }
            }
        });
    }

    public void setPlayerDead(LivingEntity entity) {
        MinecraftServer server = entity.getServer();
        UUID uuid = entity.getUuid();
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
        player.changeGameMode(GameMode.SPECTATOR);
        player.sendMessage(Text.literal("You have died for the last time. Your run has ended."));
    }


}
