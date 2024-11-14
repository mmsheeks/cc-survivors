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

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        NbtCompound playersNbt = new NbtCompound();
        players.forEach((uuid, playerData) -> {
            NbtCompound playerNbt = new NbtCompound();
            playerNbt.putInt("lives", playerData.lives);
            playerNbt.putString("lifeGiveTimestamp", playerData.lifeGiveTimestamp);
            playersNbt.put(uuid.toString(), playerNbt);
        });
        nbt.put("players", playersNbt);
        return nbt;
    }

    public static StateSaverLoader createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup ) {
        StateSaverLoader state = new StateSaverLoader();
        NbtCompound playersNbt = tag.getCompound("players");
        playersNbt.getKeys().forEach(key -> {
            PlayerData playerData = new PlayerData();
            playerData.lives = playersNbt.getCompound(key).getInt("lives");
            playerData.lifeGiveTimestamp = playersNbt.getCompound(key).getString("lifeGiveTimestamp");
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
        PlayerData playerState = serverState.players.computeIfAbsent(player.getUuid(), uuid -> new PlayerData());
        return playerState;
    }
}
