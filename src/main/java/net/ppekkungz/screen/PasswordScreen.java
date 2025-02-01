package net.ppekkungz.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.core.BlockPos;
import net.ppekkungz.ChestLockManager;

public class PasswordScreen extends Screen {
	private EditBox passwordField;
	private final BlockPos chestPos;
	private final boolean isLocking;
	private String message = "";
	
	public PasswordScreen(BlockPos pos, boolean isLocking) {
		super(new TextComponent("Chest Password"));
		this.chestPos = pos;
		this.isLocking = isLocking;
	}
	
	@Override
	protected void init() {
		int width = this.width / 2;
		int height = this.height / 2;
		
		// Create password input field
		passwordField = new EditBox(this.font, width - 100, height - 10, 200, 20,
				new TextComponent("Enter Password"));
		
		addRenderableWidget(passwordField);
		
		// Create submit button
		addRenderableWidget(new Button(width - 50, height + 20, 100, 20,
				new TextComponent(isLocking ? "Lock" : "Unlock"), (button) -> {
			String password = passwordField.getValue();
			if (password.length() < 4) {
				message = "Password must be at least 4 characters!";
				return;
			}
			
			if (isLocking) {
				if (ChestLockManager.lockChest(Minecraft.getInstance().level, chestPos,
						Minecraft.getInstance().player, password)) {
					message = "Chest locked successfully!";
				}
			} else {
				if (ChestLockManager.unlockChest(Minecraft.getInstance().level, chestPos,
						Minecraft.getInstance().player, password)) {
					message = "Chest unlocked successfully!";
				} else {
					message = "Wrong password!";
					return;
				}
			}
			
			// Close screen after short delay to show message
			Minecraft.getInstance().tell(() -> this.onClose());
		}));
	}
	
	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(poseStack);
		super.render(poseStack, mouseX, mouseY, partialTick);
		
		// Draw title
		drawCenteredString(poseStack, this.font,
				isLocking ? "Enter Password to Lock Chest" : "Enter Password to Unlock Chest",
				this.width / 2, this.height / 2 - 30, 0xFFFFFF);
		
		// Draw message (if any)
		if (!message.isEmpty()) {
			drawCenteredString(poseStack, this.font, message,
					this.width / 2, this.height / 2 + 50, 0xFF0000);
		}
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
}