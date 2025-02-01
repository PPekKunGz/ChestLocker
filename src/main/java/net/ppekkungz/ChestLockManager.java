package net.ppekkungz;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import java.util.HashMap;
import java.util.Map;

public class ChestLockManager {
	private static final Map<BlockPos, String> lockedChests = new HashMap<>();
	private static final Map<BlockPos, String> passwordMap = new HashMap<>();
	
	public static boolean lockChest(Level level, BlockPos pos, Player player, String password) {
		if (!level.isClientSide) {
			lockedChests.put(pos, player.getGameProfile().getName());
			passwordMap.put(pos, password);
			return true;
		}
		return false;
	}
	
	public static boolean isChestLocked(BlockPos pos) {
		return lockedChests.containsKey(pos);
	}
	
	public static boolean checkPassword(BlockPos pos, String password) {
		if (passwordMap.containsKey(pos)) {
			return passwordMap.get(pos).equals(password);
		}
		return false;
	}
	
	public static boolean unlockChest(Level level, BlockPos pos, Player player, String password) {
		if (!level.isClientSide) {
			if (checkPassword(pos, password)) {
				lockedChests.remove(pos);
				passwordMap.remove(pos);
				return true;
			}
		}
		return false;
	}
	
	public static String getOwner(BlockPos pos) {
		return lockedChests.get(pos);
	}
}