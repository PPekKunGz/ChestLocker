package net.ppekkungz.util;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.ppekkungz.ChestLockerMod;

/**
 * จัดการช่องทาง packet ทั้งหมดของ ChestLocker
 * - รับข้อมูลจาก client (เช่น ตั้งรหัสผ่านของกุญแจ)
 * - รัน logic ในฝั่ง server เท่านั้น
 */
public final class ModPackets {

    private ModPackets() {}

    public static void registerServerReceivers() {

        // ✅ รับ packet จาก client ตอนตั้ง password ให้ key
        ServerPlayNetworking.registerGlobalReceiver(
                ChestLockerMod.id("set_key_password"),
                (server, player, handler, buf, responseSender) -> {
                    ItemStack stack = buf.readItem();
                    String password = buf.readUtf();

                    // ทำงานฝั่ง server เท่านั้น
                    server.execute(() -> handleSetKeyPassword(player, stack, password));
                }
        );
    }

    /**
     * เมื่อ client ส่ง password มาให้เซิร์ฟเวอร์
     */
    private static void handleSetKeyPassword(ServerPlayer player, ItemStack stack, String password) {
        ItemStack real = player.getMainHandItem();

        // ป้องกัน spoof — ตรวจให้แน่ใจว่า item ที่ client ส่งคือของในมือจริงๆ
        if (!ItemStack.isSameItemSameTags(real, stack)) return;

        // เซ็ต password ลง NBT ของ item จริงในมือ
        real.getOrCreateTag().putString("password", password);
    }
}
