package gg.clovercraft.survivors;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.UUID;

public class StateSaverLoader extends PersistentState {

    public HashMap<UUID, PlayerData> players = new HashMap<>();
    public int totalPlayersKilled = 0;

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        // write basic data
        nbt.putInt("totalPlayersKilled", totalPlayersKilled);

        // write player data
        NbtCompound playersNbt = new NbtCompound();
        players.forEach((uuid, playerData) -> playersNbt.put(uuid.toString(), playerData.toNbt()));
        nbt.put("players", playersNbt);
        return nbt;
    }

    public static StateSaverLoader createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup ) {
        StateSaverLoader state = new StateSaverLoader();
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
}
