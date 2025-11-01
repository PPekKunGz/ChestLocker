package net.ppekkungz.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.ppekkungz.ChestLockerMod;

import java.util.UUID;

/**
 * หน้าจอตั้งรหัสผ่านสำหรับกุญแจ (Tripwire Hook)
 * จะบันทึก password + owner + owner_uuid ลงใน NBT
 */
public class PasswordScreen extends Screen {

    private EditBox passwordField;
    private final ItemStack keyStack;
    private String info = "";

    public PasswordScreen(ItemStack stack) {
        super(Component.literal("Set Password"));
        this.keyStack = stack;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        passwordField = new EditBox(this.font, centerX - 80, centerY - 10, 160, 20, Component.literal("Enter password"));
        passwordField.setMaxLength(32);
        this.addRenderableWidget(passwordField);

        this.addRenderableWidget(Button.builder(Component.literal("Save Password"), btn -> {
            String pwd = passwordField.getValue().trim();

            if (!pwd.isEmpty()) {
                // 🔹 บันทึกชื่อและ UUID ของเจ้าของใน client ก่อนส่ง
                if (Minecraft.getInstance().player != null) {
                    String ownerName = Minecraft.getInstance().player.getName().getString();
                    UUID ownerUuid = Minecraft.getInstance().player.getUUID();
                    keyStack.getOrCreateTag().putString("owner", ownerName);
                    keyStack.getOrCreateTag().putString("owner_uuid", ownerUuid.toString());
                }

                var buf = PacketByteBufs.create();
                buf.writeItem(keyStack);
                buf.writeUtf(pwd);

                ClientPlayNetworking.send(ChestLockerMod.id("set_key_password"), buf);

                Minecraft.getInstance().setScreen(null);
                info = "✅ Password set successfully!";
            } else {
                info = "⚠ Password cannot be empty!";
            }
        }).bounds(centerX - 80, centerY + 25, 160, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        this.renderBackground(g);
        super.render(g, mouseX, mouseY, delta);

        g.drawCenteredString(this.font, "🔐 Set Key Password", this.width / 2, this.height / 2 - 35, 0xFFFFFF);
        if (!info.isEmpty()) {
            g.drawCenteredString(this.font, info, this.width / 2, this.height / 2 + 55, 0x55FF55);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
