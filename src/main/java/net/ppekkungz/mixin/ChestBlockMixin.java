package net.ppekkungz.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.ppekkungz.ChestLockManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/**
 * จัดการคลิกขวา chest:
 * - ถ้าถูกล็อค → ตรวจสิทธิ์ / ปลดล็อคด้วย key ถ้ากดย่อ
 * - ถ้ายังไม่ล็อค → ใช้ key ของเจ้าของล็อคได้
 */
@Mixin(ChestBlock.class)
public class ChestBlockMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void chestlocker$onUse(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (level.isClientSide) return;
        var stack = player.getItemInHand(hand);

        // 1️⃣ ถ้า chest ถูกล็อคอยู่
        if (ChestLockManager.isLocked(level, pos)) {
            // 👉 Sneak + ถือ key → พยายามปลดล็อค
            if (player.isCrouching() && stack.is(Items.TRIPWIRE_HOOK) && stack.hasTag()) {
                String keyOwnerUuidStr = stack.getTag().getString("owner_uuid");
                String keyPassword = stack.getTag().getString("password");

                if (!keyOwnerUuidStr.isEmpty() && !keyPassword.isEmpty()) {
                    try {
                        UUID keyOwnerUuid = UUID.fromString(keyOwnerUuidStr);
                        boolean unlocked = ChestLockManager.unlockWithKey(
                                (net.minecraft.server.level.ServerLevel) level,
                                pos,
                                keyOwnerUuid,
                                keyPassword
                        );

                        if (unlocked) {
                            player.displayClientMessage(
                                    Component.literal("🔓 Chest unlocked with key.")
                                            .withStyle(ChatFormatting.GREEN),
                                    true
                            );
                            cir.setReturnValue(InteractionResult.SUCCESS);
                            return;
                        } else {
                            player.displayClientMessage(
                                    Component.literal("❌ Key does not match or password incorrect.")
                                            .withStyle(ChatFormatting.RED),
                                    true
                            );
                            cir.setReturnValue(InteractionResult.FAIL);
                            return;
                        }
                    } catch (IllegalArgumentException ignored) {}
                }
            }

            // 👉 ไม่ได้ปลดล็อค → ตรวจสิทธิ์ปกติ (owner / whitelist)
            boolean canOpen = ChestLockManager.canAccess(
                    (net.minecraft.server.level.ServerLevel) level,
                    pos,
                    player.getUUID(),
                    ""
            );
            if (!canOpen) {
                player.displayClientMessage(
                        Component.literal("🔒 This chest is locked!")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                cir.setReturnValue(InteractionResult.FAIL);
            }
            return;
        }

        // 2️⃣ ถ้ายังไม่ล็อค → ถือ key ล็อค chest
        if (stack.is(Items.TRIPWIRE_HOOK) && stack.hasTag()) {
            String password = stack.getTag().getString("password");
            String ownerUuidStr = stack.getTag().getString("owner_uuid");
            String ownerName = stack.getTag().getString("owner");

            if (password.isEmpty()) {
                player.displayClientMessage(
                        Component.literal("❗ Set a password on the key first!")
                                .withStyle(ChatFormatting.YELLOW),
                        true
                );
                cir.setReturnValue(InteractionResult.FAIL);
                return;
            }

            // ตรวจว่าคนถือ key ตรงกับเจ้าของไหม
            if (!ownerUuidStr.isEmpty()) {
                try {
                    UUID keyOwnerUuid = UUID.fromString(ownerUuidStr);
                    if (!keyOwnerUuid.equals(player.getUUID())) {
                        player.displayClientMessage(
                                Component.literal("❌ This key does not belong to you!")
                                        .withStyle(ChatFormatting.RED),
                                true
                        );
                        cir.setReturnValue(InteractionResult.FAIL);
                        return;
                    }
                } catch (IllegalArgumentException ignored) {}
            } else if (!ownerName.isEmpty() && !ownerName.equals(player.getName().getString())) {
                player.displayClientMessage(
                        Component.literal("❌ This key does not belong to you!")
                                .withStyle(ChatFormatting.RED),
                        true
                );
                cir.setReturnValue(InteractionResult.FAIL);
                return;
            }

            // ✅ ล็อค chest
            boolean success = ChestLockManager.lock(
                    (net.minecraft.server.level.ServerLevel) level,
                    pos,
                    player.getUUID(),
                    password
            );

            if (success) {
                player.displayClientMessage(
                        Component.literal("✅ Chest locked successfully.")
                                .withStyle(ChatFormatting.GREEN),
                        true
                );
                cir.setReturnValue(InteractionResult.SUCCESS);
            } else {
                player.displayClientMessage(
                        Component.literal("⚠ This chest is already locked.")
                                .withStyle(ChatFormatting.YELLOW),
                        true
                );
            }
        }
    }
}
