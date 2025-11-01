package net.ppekkungz.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.ChestBlock;
import net.ppekkungz.ChestLockManager;

public final class OwnerDisplayHud implements HudRenderCallback {

    private final Minecraft mc = Minecraft.getInstance();

    @Override
    public void onHudRender(GuiGraphics graphics, float tickDelta) {
        if (mc.player == null || mc.level == null || mc.hitResult == null) return;
        if (mc.hitResult.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) return;

        var hit = (net.minecraft.world.phys.BlockHitResult) mc.hitResult;
        BlockPos pos = hit.getBlockPos();
        var state = mc.level.getBlockState(pos);

        double distance = mc.player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
        if (distance > 25) return; // ถ้าไกลกว่า 5 บล็อก ไม่แสดง

        // แสดงเฉพาะถ้าเป็น Chest และล็อคอยู่
        if (!(state.getBlock() instanceof ChestBlock)) return;
        if (!ChestLockManager.isLocked(mc.level, pos)) return;

        // ดึงข้อมูล lock
        var data = ChestLockManager.getLock(mc.level, pos).orElse(null);
        if (data == null) return;

        String name = "Unknown";
        var owner = mc.level.getServer().getPlayerList().getPlayer(data.owner());
        if (owner != null) {
            name = owner.getName().getString();
        }

        // วาดข้อความกลางจอ
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        String text = "🏷 Locked by: " + name;
        graphics.drawCenteredString(mc.font, Component.literal(text), screenW / 2, screenH / 2 + 20, 0xFFAA00);
    }
}
