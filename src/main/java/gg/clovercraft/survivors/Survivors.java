package gg.clovercraft.survivors;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class Survivors implements ModInitializer {

    public static final String MOD_ID = "survivors";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final int MAX_LIVES = 5;

    @Override
    public void onInitialize() {

        LOGGER.info("setting up server commands");
        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> {
            Commands commands = new Commands(commandDispatcher, commandRegistryAccess, registrationEnvironment);
            commands.registerAll();
        }));

        LOGGER.info("setting up scoreboards");
        ServerLifecycleEvents.SERVER_STARTED.register(Scoreboards::register);

        LOGGER.info("Setting up player events");
        // on join event
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PlayerData playerState = StateSaverLoader.getPlayerState(handler.getPlayer());
            Scoreboards.updatePlayerTeam(handler.getPlayer(),playerState);
        });

        // on death event
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> {
            if (entity instanceof PlayerEntity) {
                DeathHandler.onPlayerDeath(entity, source);
            }
        });
    }


}
