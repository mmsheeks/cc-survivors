package gg.clovercraft.survivors;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class Commands {

    private final CommandDispatcher<ServerCommandSource> dispatcher;
    private final CommandRegistryAccess access;
    private final CommandManager.RegistrationEnvironment environment;

    public Commands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment ) {
        this.dispatcher = dispatcher;
        this.access = registryAccess;
        this.environment = environment;
    }

    public void registerAll() {
        lives();
        set_lives();
        give_life();
    }

    public void lives() {
        dispatcher.register(CommandManager.literal("lives")
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    if(source.isExecutedByPlayer()) {
                        ServerPlayerEntity player = source.getPlayer();
                        if (player == null) {
                            context.getSource().sendFeedback(() -> Text.literal("Failed to determine player"), false);
                            return 0;
                        }
                        PlayerData playerState = StateSaverLoader.getPlayerState(player);
                        int lives = playerState.lives;

                        context.getSource().sendFeedback(() -> Text.literal("You have %s lives remaining.".formatted(lives)), false);
                    } else {
                        context.getSource().sendFeedback(() -> Text.literal("you are not a player."), false);
                    }
                    return 1;
                }));
    }

    public void set_lives() {
        dispatcher.register(CommandManager.literal("setlives")
                .requires(source -> source.hasPermissionLevel(1))
                .then(CommandManager.argument("player", StringArgumentType.string())
                        .then(CommandManager.argument("lives", IntegerArgumentType.integer())
                        .executes(context -> {
                            // parse args
                            String playerName = StringArgumentType.getString(context, "player");
                            int lives = IntegerArgumentType.getInteger(context, "lives");

                            // get selected player state
                            PlayerEntity player = getPlayer(context);
                            if (player == null) {
                                context.getSource().sendFeedback(() -> Text.literal("Failed to determine player"), false);
                                return 0;
                            }
                            PlayerData playerState = StateSaverLoader.getPlayerState(player);

                            // set lives
                            if(lives > Survivors.MAX_LIVES) {
                                lives = Survivors.MAX_LIVES;
                            }
                            playerState.setLives(lives);
                            Scoreboards.updatePlayerTeam(player, playerState);

                            context.getSource().sendFeedback(() -> Text.literal("Set %s lives to %s".formatted(player.getDisplayName().getString(), playerState.lives)), true);
                            return 1;
                        }))));
    }

    public void give_life() {
        dispatcher.register(CommandManager.literal("givelife")
                .then(CommandManager.argument("player", StringArgumentType.string())
                        .executes(context -> {
                            // get targeted player
                            PlayerEntity target = getPlayer(context);
                            if (target == null) {
                                context.getSource().sendFeedback(() -> Text.literal("Failed to determine player"), false);
                                return 0;
                            }
                            PlayerEntity player = context.getSource().getPlayer();
                            if (player == null) {
                                context.getSource().sendFeedback(() -> Text.literal("Failed to determine player"), false);
                                return 0;
                            }

                            // get player states
                            PlayerData playerState = StateSaverLoader.getPlayerState(player);
                            PlayerData targetState = StateSaverLoader.getPlayerState(target);

                            // check ability to cast
                            if (!playerState.canGiveLife()) {
                                context.getSource().sendFeedback(() -> Text.literal("You have already sent a life today. You must wait for next session."), false);
                                return 1;
                            }

                            if (playerState.lives <= 1) {
                                context.getSource().sendFeedback(() -> Text.literal("You cannot give away your last life."), false);
                                return 1;
                            }

                            if (targetState.lives == Survivors.MAX_LIVES) {
                                context.getSource().sendFeedback(() -> Text.literal("%s already has the maximum number of lives.".formatted(target.getDisplayName().toString())), false);
                                return 1;
                            }

                            // all checks pass. adjust lives and inform the players
                            playerState.subLife();
                            playerState.flagGiveLife();
                            Scoreboards.updatePlayerTeam(player, playerState);
                            targetState.addLife();
                            Scoreboards.updatePlayerTeam(target, targetState);

                            context.getSource().sendFeedback(() -> Text.literal("Gave one life to %s".formatted(target.getDisplayName().toString())), false);
                            return 1;
                        })));
    }

    private PlayerEntity getPlayer(CommandContext<ServerCommandSource> context) {
        // get selected player state
        String playerName = StringArgumentType.getString(context, "player");
        MinecraftServer server = context.getSource().getServer();
        PlayerEntity player = server.getPlayerManager().getPlayer(playerName);
        if(player == null) {
            context.getSource().sendFeedback(() -> Text.literal("Could not find player: %s".formatted(playerName)), false);
            return null;
        }
        return player;
    }
}
