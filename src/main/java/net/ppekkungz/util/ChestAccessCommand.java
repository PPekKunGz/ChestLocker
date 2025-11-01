package net.ppekkungz.util;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.ppekkungz.ChestLockManager;

import java.util.Optional;
import java.util.UUID;

/**
 * คำสั่ง /chestaccess
 * ใช้สำหรับเพิ่ม / ลบ / ดูรายชื่อ whitelist ของ chest ที่ผู้เล่นมองอยู่
 */
public final class ChestAccessCommand {

    private ChestAccessCommand() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, env) -> {
            dispatcher.register(Commands.literal("chestaccess")
                    .requires(src -> src.hasPermission(0))
                    .then(Commands.literal("add")
                            .then(Commands.argument("player", StringArgumentType.string())
                                    .executes(ctx -> {
                                        ServerPlayer executor = ctx.getSource().getPlayerOrException();
                                        String targetName = StringArgumentType.getString(ctx, "player");
                                        return handleAdd(ctx.getSource().getServer(), executor, targetName);
                                    })
                            )
                    )
                    .then(Commands.literal("remove")
                            .then(Commands.argument("player", StringArgumentType.string())
                                    .executes(ctx -> {
                                        ServerPlayer executor = ctx.getSource().getPlayerOrException();
                                        String targetName = StringArgumentType.getString(ctx, "player");
                                        return handleRemove(ctx.getSource().getServer(), executor, targetName);
                                    })
                            )
                    )
                    .then(Commands.literal("list")
                            .executes(ctx -> {
                                ServerPlayer executor = ctx.getSource().getPlayerOrException();
                                return handleList(ctx.getSource().getServer(), executor);
                            })
                    )
            );
        });
    }

    // ------------------- Logic -------------------

    private static int handleAdd(MinecraftServer server, ServerPlayer player, String targetName) {
        ServerPlayer target = server.getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            player.displayClientMessage(Component.literal("❌ Player not found."), false);
            return 0;
        }

        Optional<BlockTarget> block = getTargetedChest(player);
        if (block.isEmpty()) {
            player.displayClientMessage(Component.literal("⚠️ Look at a chest to add access."), false);
            return 0;
        }

        boolean ok = ChestLockManager.addToWhitelist(
                block.get().level,
                block.get().pos,
                player.getUUID(),
                target.getUUID()
        );

        if (ok) {
            player.displayClientMessage(Component.literal("✅ Added " + targetName + " to chest whitelist."), false);
        } else {
            player.displayClientMessage(Component.literal("⚠️ You must own this chest."), false);
        }
        return 1;
    }

    private static int handleRemove(MinecraftServer server, ServerPlayer player, String targetName) {
        ServerPlayer target = server.getPlayerList().getPlayerByName(targetName);
        if (target == null) {
            player.displayClientMessage(Component.literal("❌ Player not found."), false);
            return 0;
        }

        Optional<BlockTarget> block = getTargetedChest(player);
        if (block.isEmpty()) {
            player.displayClientMessage(Component.literal("⚠️ Look at a chest to remove access."), false);
            return 0;
        }

        boolean ok = ChestLockManager.removeFromWhitelist(
                block.get().level,
                block.get().pos,
                player.getUUID(),
                target.getUUID()
        );

        if (ok) {
            player.displayClientMessage(Component.literal("🗑 Removed " + targetName + " from whitelist."), false);
        } else {
            player.displayClientMessage(Component.literal("⚠️ You must own this chest."), false);
        }
        return 1;
    }

    private static int handleList(MinecraftServer server, ServerPlayer player) {
        Optional<BlockTarget> block = getTargetedChest(player);
        if (block.isEmpty()) {
            player.displayClientMessage(Component.literal("⚠️ Look at a chest to view whitelist."), false);
            return 0;
        }

        var dataOpt = ChestLockManager.getLock(block.get().level, block.get().pos);
        if (dataOpt.isEmpty()) {
            player.displayClientMessage(Component.literal("❌ This chest is not locked."), false);
            return 0;
        }

        var data = dataOpt.get();
        if (!data.owner().equals(player.getUUID())) {
            player.displayClientMessage(Component.literal("⚠️ You must own this chest."), false);
            return 0;
        }

        if (data.whitelist().isEmpty()) {
            player.displayClientMessage(Component.literal("📭 No players in whitelist."), false);
        } else {
            player.displayClientMessage(Component.literal("👥 Whitelisted players:"), false);
            data.whitelist().forEach(uuid -> {
                ServerPlayer p = server.getPlayerList().getPlayer(uuid);
                String name = (p != null) ? p.getName().getString() : uuid.toString();
                player.displayClientMessage(Component.literal(" - " + name), false);
            });
        }

        return 1;
    }

    // ------------------- Helper -------------------

    /**
     * ตรวจสอบว่าผู้เล่นกำลังมองที่ chest ตัวไหน
     */
    private static Optional<BlockTarget> getTargetedChest(ServerPlayer player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookDir = player.getLookAngle();
        Vec3 target = eyePos.add(lookDir.scale(5.0)); // ระยะมอง 5 บล็อก

        var hit = player.level().clip(new net.minecraft.world.level.ClipContext(
                eyePos,
                target,
                net.minecraft.world.level.ClipContext.Block.OUTLINE,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                player
        ));

        if (hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
            var state = player.level().getBlockState(hit.getBlockPos());
            if (state.getBlock() instanceof net.minecraft.world.level.block.ChestBlock) {
                return Optional.of(new BlockTarget((ServerLevel) player.level(), hit.getBlockPos()));
            }
        }
        return Optional.empty();
    }

    // class เล็ก ๆ สำหรับเก็บผลลัพธ์จาก getTargetedChest()
    private record BlockTarget(ServerLevel level, net.minecraft.core.BlockPos pos) {}
}
