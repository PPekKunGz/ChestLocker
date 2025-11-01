package net.ppekkungz;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.ppekkungz.util.ChestAccessCommand;
import net.ppekkungz.util.ModPackets;

import java.nio.file.Path;

public class ChestLockerMod implements ModInitializer {
    public static final String MOD_ID = "chestlocker";

    @Override
    public void onInitialize() {
        Path configPath = FabricLoader.getInstance()
                .getConfigDir()
                .resolve("chestlocker.json");
        ChestLockManager.load(configPath);

        ModPackets.registerServerReceivers();
        ChestAccessCommand.register();

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClientSide) return true;
            if (ChestLockManager.isLocked(world, pos)) {
                return false; // ป้องกันการทุบ
            }
            return true;
        });
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
