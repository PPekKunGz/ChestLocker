package net.ppekkungz;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Items;
import net.ppekkungz.screen.PasswordScreen;

@Environment(EnvType.CLIENT)
public class ChestLoggerClient implements ClientModInitializer {
	public static final String MOD_ID = "chestlocker";
	
	@Override
	public void onInitializeClient() {
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
}
