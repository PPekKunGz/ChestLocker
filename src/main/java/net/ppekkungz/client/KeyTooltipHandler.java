package net.ppekkungz.client;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.UUID;

/**
 * Tooltip handler สำหรับกุญแจ (Tripwire Hook)
 * - แสดงชื่อเจ้าของจาก owner / owner_uuid
 * - แสดง password แบบ obfuscate
 * - กด SHIFT เพื่อดูรหัสจริง
 */
public class KeyTooltipHandler implements ItemTooltipCallback {

    @Override
    public void getTooltip(ItemStack stack, TooltipFlag context, List<Component> lines) {
        if (!stack.is(Items.TRIPWIRE_HOOK)) return;
        if (!stack.hasTag() || !stack.getTag().contains("password")) return;

        Minecraft mc = Minecraft.getInstance();
        var tag = stack.getTag();

        String password = tag.getString("password");
        String ownerName = getOwnerName(tag);

        // เปลี่ยนชื่อ item ให้สวยงาม
        stack.setHoverName(Component.literal("🔑 Key of " + ownerName)
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        boolean showReal = isShiftDown();
        String displayPassword = showReal ? password : obfuscate(password);

        lines.add(Component.literal("Password: ")
                .append(Component.literal(displayPassword)
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA))));
        lines.add(Component.literal(showReal ? "Hold SHIFT to hide" : "Hold SHIFT to reveal")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }

    private String getOwnerName(net.minecraft.nbt.CompoundTag tag) {
        // ลองใช้ owner_uuid ก่อน ถ้ามี
        if (tag.contains("owner_uuid")) {
            try {
                UUID uuid = UUID.fromString(tag.getString("owner_uuid"));
                // พยายามหาชื่อจาก player list (ถ้าเล่น singleplayer)
                var server = Minecraft.getInstance().getSingleplayerServer();
                if (server != null) {
                    var p = server.getPlayerList().getPlayer(uuid);
                    if (p != null) return p.getName().getString();
                }
            } catch (IllegalArgumentException ignored) {}
        }
        // ถ้าไม่มี uuid หรือหาไม่เจอ → ใช้ชื่อที่เก็บใน tag
        if (tag.contains("owner")) return tag.getString("owner");
        return "Unknown";
    }

    private boolean isShiftDown() {
        long handle = Minecraft.getInstance().getWindow().getWindow();
        return GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }

    private String obfuscate(String pwd) {
        if (pwd == null || pwd.isEmpty()) return "(none)";
        return "•".repeat(Math.min(pwd.length(), 16));
    }
}
