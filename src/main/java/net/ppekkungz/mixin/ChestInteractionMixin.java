package net.ppekkungz.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.client.Minecraft;
import net.ppekkungz.ChestLockManager;
import net.ppekkungz.screen.PasswordScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBlockEntity.class)
public class ChestInteractionMixin {
	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void onChestInteract(Level world, Player player, InteractionHand hand, BlockPos pos, CallbackInfoReturnable<InteractionResult> cir) {
		ItemStack heldItem = player.getItemInHand(hand);
		
		// Lock chest with iron ingot (shift + right-click)
		if (player.isShiftKeyDown() && heldItem.is(Items.IRON_INGOT)) {
			if (!world.isClientSide) {
				Minecraft.getInstance().setScreen(new PasswordScreen(pos, true));
			}
			cir.setReturnValue(InteractionResult.SUCCESS);
			return;
		}
		
		// Unlock with tripwire hook (shift + right-click)
		if (player.isShiftKeyDown() && heldItem.is(Items.TRIPWIRE_HOOK)) {
			if (ChestLockManager.isChestLocked(pos)) {
				if (!world.isClientSide) {
					Minecraft.getInstance().setScreen(new PasswordScreen(pos, false));
				}
				cir.setReturnValue(InteractionResult.SUCCESS);
				return;
			}
		}
	}
}