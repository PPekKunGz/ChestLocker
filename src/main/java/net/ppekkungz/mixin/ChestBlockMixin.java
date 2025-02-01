package net.ppekkungz.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.ppekkungz.ChestLockManager;
import net.ppekkungz.screen.PasswordScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBlock.class)
public class ChestBlockMixin {
	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void onUse(BlockState state, Level world, BlockPos pos, Player player,
	                   InteractionHand hand, BlockHitResult hit,
	                   CallbackInfoReturnable<InteractionResult> cir) {
		
		ItemStack heldItem = player.getItemInHand(hand);
		
		System.out.println("ChestBlockMixin: " + heldItem);
		
		if (player.isShiftKeyDown()) {
			if (heldItem.is(Items.IRON_INGOT)) {
				if (world.isClientSide) {
					Minecraft.getInstance().setScreen(new PasswordScreen(pos, true));
				}
				cir.setReturnValue(InteractionResult.SUCCESS);
				return;
			}
			
			if (heldItem.is(Items.TRIPWIRE_HOOK)) {
				if (ChestLockManager.isChestLocked(pos)) {
					if (world.isClientSide) {
						Minecraft.getInstance().setScreen(new PasswordScreen(pos, false));
					}
					cir.setReturnValue(InteractionResult.SUCCESS);
					return;
				}
			}
		}
		
		// If chest is locked, prevent opening
		if (ChestLockManager.isChestLocked(pos) &&
				!player.isShiftKeyDown() &&
				!heldItem.is(Items.TRIPWIRE_HOOK)) {
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}
}