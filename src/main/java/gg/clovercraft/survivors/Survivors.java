package gg.clovercraft.survivors;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class Survivors implements ModInitializer {

    public static final String MOD_ID = "survivors";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final int MAX_LIVES = 5;
    public static final int WORLD_SIZE = 500;

    @Override
    public void onInitialize() {

        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> {
            LOGGER.info("setting up server commands");
            Commands commands = new Commands(commandDispatcher, commandRegistryAccess, registrationEnvironment);
            commands.registerAll();
        }));

        LOGGER.info("setting up scoreboards");
        ServerLifecycleEvents.SERVER_STARTED.register(Scoreboards::register);
        ServerLifecycleEvents.SERVER_STARTED.register(SurvivorsAdvancements::register);

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

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!alive) {
                DeathHandler.afterDeath(newPlayer);
            }
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LOGGER.info("Setting server game rules");
            server.setDifficulty(Difficulty.HARD,true);
            server.getGameRules().get(GameRules.KEEP_INVENTORY).set(true,server);
            server.getGameRules().get(GameRules.SPAWN_RADIUS).set(0,server);
            LOGGER.info("setting world size to %s".formatted(WORLD_SIZE));
            server.getWorld(World.OVERWORLD).getWorldBorder().setSize(WORLD_SIZE);
        });
    }


}
