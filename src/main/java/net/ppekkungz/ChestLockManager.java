package net.ppekkungz;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class ChestLockManager {
	private static final Map<BlockPos, String> lockedChests = new HashMap<>();
	
	public static boolean lockChest(Level world, BlockPos pos, Player player, String password) {
		if (!world.isClientSide) {
			lockedChests.put(pos, password);
			return true;
		}
		return false;
	}
	
	public static boolean unlockChest(Level world, BlockPos pos, Player player, String attemptedPassword) {
		if (!world.isClientSide) {
			String storedPassword = lockedChests.get(pos);
			return storedPassword != null && storedPassword.equals(attemptedPassword);
		}
		return false;
	}
	
	public static boolean isChestLocked(BlockPos pos) {
		return lockedChests.containsKey(pos);
	}
}