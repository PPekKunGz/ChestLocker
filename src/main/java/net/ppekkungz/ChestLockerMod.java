package net.ppekkungz;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.ppekkungz.screen.PasswordScreen;

public class ChestLockerMod implements ModInitializer {
	public static final ResourceLocation PACKET = new ResourceLocation(ChestLoggerClient.MOD_ID, "key_item");


	@Override
	public void onInitialize() {
		ServerPlayNetworking.registerGlobalReceiver(PACKET, (minecraftServer, serverPlayer, serverGamePacketListener, friendlyByteBuf, packetSender) -> {
			var password = friendlyByteBuf.readUtf();
			var itemStack = serverPlayer.getItemInHand(InteractionHand.MAIN_HAND);
			if (itemStack.is(Items.TRIPWIRE_HOOK)) {
				ItemStack stack = new ItemStack(itemStack.getItem(), 1);
				itemStack.shrink(1);
				CompoundTag nbt = new CompoundTag();
				CompoundTag display = new CompoundTag();
//				display.putString("Name", "{\"text\": \"รหัสถูกตั้งไว้แล้ว\", \"italic\": \"false\"}");
				display.putString("Name", String.format("{\"text\": \"%s\", \"italic\": \"false\", \"obfuscated\": \"true\"}", password));

				ListTag loreList = new ListTag();
				loreList.add(StringTag.valueOf(String.format("{\"text\": \"รหัสผ่านคือ : %s\", \"italic\": \"false\"}", password)));

				display.put("Lore", loreList);
				nbt.put("display", display);
				nbt.putString("password", password);
				stack.setTag(nbt);
				serverPlayer.addItem(stack);
			}
		});
	}

	public static String getPassword(ItemStack stack) {
		if (stack.hasTag() && stack.getTag() != null) {
			return stack.getTag().getString("password");
		}
		return "";
	}

}