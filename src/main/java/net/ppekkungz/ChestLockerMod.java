package net.ppekkungz;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ChestBlock;

public class ChestLockerMod implements ModInitializer {
	public static final String MOD_ID = "chestlocker";
	
	@Override
	public void onInitialize() {
		// Register event handlers
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			// If chest is locked and player doesn't have permission
			if (world.getBlockState(hitResult.getBlockPos()).getBlock() instanceof ChestBlock) {
				BlockPos pos = hitResult.getBlockPos();
				if (ChestLockManager.isChestLocked(pos)) {
					// If chest is locked and player is not using a tripwire hook
					if (!player.getItemInHand(hand).is(Items.TRIPWIRE_HOOK) &&
							!player.isShiftKeyDown()) {
						return InteractionResult.FAIL;
					}
				}
			}
			return InteractionResult.PASS;
		});
	}
}