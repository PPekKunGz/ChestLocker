package net.ppekkungz.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.core.BlockPos;
import net.ppekkungz.ChestLockManager;

public class PasswordScreen extends Screen {
	private EditBox passwordField;
	private final BlockPos chestPos;
	private final boolean isLocking;
	
	public PasswordScreen(BlockPos pos, boolean isLocking) {
		super(new TextComponent("Chest Password"));
		this.chestPos = pos;
		this.isLocking = isLocking;
	}
	
	@Override
	protected void init() {
		int width = this.width / 2;
		int height = this.height / 2;
		
		passwordField = new EditBox(this.font, width - 100, height - 10, 200, 20, new TextComponent("Enter Password"));
		
		addRenderableWidget(passwordField);
		
		addRenderableWidget(new Button(width - 50, height + 20, 100, 20,
				new TextComponent(isLocking ? "Lock" : "Unlock"), (button) -> {
			String password = passwordField.getValue();
			if (isLocking) {
				ChestLockManager.lockChest(Minecraft.getInstance().level, chestPos,
						Minecraft.getInstance().player, password);
			} else {
				if (ChestLockManager.unlockChest(Minecraft.getInstance().level, chestPos,
						Minecraft.getInstance().player, password)) {
					// Unlock successful
				}
			}
			this.onClose();
		}));
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		drawCenteredString(matrixStack, this.font,
				isLocking ? "Enter Password to Lock Chest" : "Enter Password to Unlock Chest",
				this.width / 2, this.height / 2 - 30, 0xFFFFFF);
	}
}