package net.ppekkungz;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ChestBlock;
import net.ppekkungz.screen.PasswordScreen;

public class ChestLockerMod implements ModInitializer {

	public static final String MOD_ID = "chestlocker";
	public static final ResourceLocation PACKET = new ResourceLocation(MOD_ID, "key_item");


	@Override
	public void onInitialize() {
		// Register event handlers
//		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
//			if (world.getBlockState(hitResult.getBlockPos()).getBlock() instanceof ChestBlock) {
//				BlockPos pos = hitResult.getBlockPos();
//				if (ChestLockManager.isChestLocked(pos)) {
//					if (!player.getItemInHand(hand).is(Items.TRIPWIRE_HOOK) && !player.isShiftKeyDown()) {
//						return InteractionResult.FAIL;
//					}
//				}
//			}
//			return InteractionResult.PASS;
//		});

		ServerPlayNetworking.registerGlobalReceiver(PACKET, (minecraftServer, serverPlayer, serverGamePacketListener, friendlyByteBuf, packetSender) -> {
			var password = friendlyByteBuf.readUtf();
			var itemStack = serverPlayer.getItemInHand(InteractionHand.MAIN_HAND);
			if (itemStack.is(Items.TRIPWIRE_HOOK)) {
				itemStack.shrink(1);
				ItemStack stack = new ItemStack(itemStack.getItem(), 1);
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

		UseItemCallback.EVENT.register((player, level, interactionHand) -> {
			var itemStack = player.getItemInHand(interactionHand);
			if (itemStack.is(Items.TRIPWIRE_HOOK) && interactionHand.equals(InteractionHand.MAIN_HAND) && player.isCrouching()) {
				var checkPassword = itemStack.getOrCreateTag();
				if (checkPassword.contains("password")) {
				} else {
					if (level.isClientSide) {
						Minecraft.getInstance().setScreen(new PasswordScreen());
					}
				}
			}
			return InteractionResultHolder.pass(itemStack);
		});
	}

	public static String getPassword(ItemStack stack) {
		if (stack.hasTag() && stack.getTag() != null) {
			return stack.getTag().getString("password");
		}
		return "";
	}

}