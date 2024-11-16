package gg.clovercraft.survivors;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.UUID;

public class StateSaverLoader extends PersistentState {

    public HashMap<UUID, PlayerData> players = new HashMap<>();
    public static final String TOTAL_PLAYERS_KILLED = "totalPlayersKilled";
    public int totalPlayersKilled = 0;
    public static final String TOTAL_PLAYERS = "totalPlayers";
    public int totalPlayers = 0;
    public static final String PLAYERS_ELIMINATED = "playersEliminated";
    public int playersEliminated = 0;

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        // write basic data
        nbt.putInt(TOTAL_PLAYERS_KILLED, totalPlayersKilled);
        nbt.putInt(TOTAL_PLAYERS, totalPlayers);
        nbt.putInt(PLAYERS_ELIMINATED, playersEliminated);

        // write player data
        NbtCompound playersNbt = new NbtCompound();
        players.forEach((uuid, playerData) -> playersNbt.put(uuid.toString(), playerData.toNbt()));
        nbt.put("players", playersNbt);
        return nbt;
    }

    public static StateSaverLoader createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup ) {
        StateSaverLoader state = new StateSaverLoader();

        state.totalPlayersKilled = tag.getInt(TOTAL_PLAYERS_KILLED);
        state.totalPlayers = tag.getInt(TOTAL_PLAYERS);
        state.playersEliminated = tag.getInt(PLAYERS_ELIMINATED);

        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerData playerData = PlayerData.fromNbt(playersNbt.getCompound(key));
            UUID uuid = UUID.fromString(key);
            state.players.put(uuid, playerData);
        });
        return state;
    }

    private static final Type<StateSaverLoader> type = new Type<>(
            StateSaverLoader::new,
            StateSaverLoader::createFromNbt,
            null
    );

    public static StateSaverLoader getServerState(MinecraftServer server) {
        PersistentStateManager stateManager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        StateSaverLoader state = stateManager.getOrCreate(type, Survivors.MOD_ID);
        state.markDirty();
        return state;
    }

    public static PlayerData getPlayerState(LivingEntity player) {
        StateSaverLoader serverState = getServerState(player.getWorld().getServer());
        return serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerData());
    }

    public static void onPlayerJoin(ServerPlayNetworkHandler handler) {
        // set player team for name color
        PlayerData playerState = StateSaverLoader.getPlayerState(handler.getPlayer());
        Scoreboards.updatePlayerTeam(handler.getPlayer(),playerState);

        // update total players on server
        MinecraftServer server = handler.getPlayer().getServer();
        assert server != null;
        StateSaverLoader serverState = StateSaverLoader.getServerState(server);
        int playerCount = server.getCurrentPlayerCount();
        if ( playerCount > serverState.totalPlayers ) {
            serverState.totalPlayers = playerCount;
        }
    }
}
