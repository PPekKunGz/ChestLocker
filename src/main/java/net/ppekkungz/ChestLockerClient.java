package net.ppekkungz;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Items;
import net.ppekkungz.client.KeyTooltipHandler;
import net.ppekkungz.client.OwnerDisplayHud;
import net.ppekkungz.screen.PasswordScreen;

@Environment(EnvType.CLIENT)
public class ChestLockerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // แสดงชื่อเจ้าของ / password tooltip
        ItemTooltipCallback.EVENT.register(new KeyTooltipHandler());
        HudRenderCallback.EVENT.register(new OwnerDisplayHud());
        // Event: เมื่อผู้เล่นใช้ item (คลิกขวา)
        UseItemCallback.EVENT.register((player, level, hand) -> {
            // ตรวจเฉพาะมือหลัก
            if (hand != InteractionHand.MAIN_HAND)
                return InteractionResultHolder.pass(player.getItemInHand(hand));

            var stack = player.getItemInHand(hand);

            // เงื่อนไข: ใช้ Tripwire Hook และกำลังก้มอยู่ → เปิดหน้ารหัส
            if (stack.is(Items.TRIPWIRE_HOOK) && player.isCrouching()) {
                // ถ้ายังไม่มี password tag → เปิดหน้าจอตั้งรหัส
                if (!stack.getOrCreateTag().contains("password")) {
                    Minecraft.getInstance().setScreen(new PasswordScreen(stack));
                }
                return InteractionResultHolder.success(stack);
            }

            return InteractionResultHolder.pass(stack);
        });
    }
}