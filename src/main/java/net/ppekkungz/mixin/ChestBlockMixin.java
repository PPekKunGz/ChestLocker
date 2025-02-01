package net.ppekkungz.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.LockCode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.ppekkungz.ChestLockManager;
import net.ppekkungz.ChestLockerMod;
import net.ppekkungz.screen.PasswordScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(ChestBlock.class)
public abstract class ChestBlockMixin extends AbstractChestBlock<ChestBlockEntity> implements SimpleWaterloggedBlock {

    protected ChestBlockMixin(Properties properties, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier) {
        super(properties, supplier);
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> cir) {
        var stack = player.getItemInHand(interactionHand);
        if (stack.is(Items.TRIPWIRE_HOOK) && !level.isClientSide) {
            var password = ChestLockerMod.getPassword(stack);
            if (!password.isEmpty()) {
                var blockEntity = level.getBlockEntity(blockPos);
                if (blockEntity instanceof BaseContainerBlockEntity containerBlockEntity) {
                    if (containerBlockEntity.lockKey == LockCode.NO_LOCK) {
                        containerBlockEntity.lockKey = new LockCode(password);
                        stack.shrink(1);
                        cir.setReturnValue(InteractionResult.sidedSuccess(level.isClientSide()));
                    }
                }
            }
        }
    }


//	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
//	private void onUse(BlockState state, Level world, BlockPos pos, Player player,
//	                   InteractionHand hand, BlockHitResult hit,
//	                   CallbackInfoReturnable<InteractionResult> cir) {
//
//		ItemStack heldItem = player.getItemInHand(hand);
//
//		System.out.println("ChestBlockMixin: " + heldItem);
//
//		if (player.isShiftKeyDown()) {
//			if (heldItem.is(Items.IRON_INGOT)) {
//				if (world.isClientSide) {
//					Minecraft.getInstance().setScreen(new PasswordScreen(pos, true));
//				}
//				cir.setReturnValue(InteractionResult.SUCCESS);
//				return;
//			}
//
//			if (heldItem.is(Items.TRIPWIRE_HOOK)) {
//				if (ChestLockManager.isChestLocked(pos)) {
//					if (world.isClientSide) {
//						Minecraft.getInstance().setScreen(new PasswordScreen(pos, false));
//					}
//					cir.setReturnValue(InteractionResult.SUCCESS);
//					return;
//				}
//			}
//		}
//
//		// If chest is locked, prevent opening
//		if (ChestLockManager.isChestLocked(pos) &&
//				!player.isShiftKeyDown() &&
//				!heldItem.is(Items.TRIPWIRE_HOOK)) {
//			cir.setReturnValue(InteractionResult.FAIL);
//		}
//	}
}