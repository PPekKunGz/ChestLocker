package net.ppekkungz.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.ppekkungz.ChestLockerMod;

public class PasswordScreen extends Screen {
	private EditBox passwordField;
	private String message = "";
	
	public PasswordScreen() {
		super(Component.literal("Chest Password"));
	}
	
	@Override
	protected void init() {
		int width = this.width / 2;
		int height = this.height / 2;
		
		passwordField = new EditBox(this.font, width - 100, height - 10, 200, 20, Component.literal("Enter Password"));
		addRenderableWidget(passwordField);
		
		addRenderableWidget(Button.builder(Component.literal("Lock"), (button) -> {
			String password = passwordField.getValue();
			if (password.length() < 4) {
				message = "Password must be at least 4 characters!";
				return;
			}
			message = "Chest unlocked successfully!";
			
			var packet = PacketByteBufs.create();
			packet.writeUtf(password);
			ClientPlayNetworking.send(ChestLockerMod.PACKET, packet);
			
			Minecraft.getInstance().setScreen(null);
		}).pos(width - 50, height + 20).size(100, 20).build());
	}
	
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.drawCenteredString(this.font, "Enter Password to Lock Chest", this.width / 2, this.height / 2 - 30, 0xFFFFFF);
		if (!message.isEmpty()) {
			guiGraphics.drawCenteredString(this.font, message, this.width / 2, this.height / 2 + 50, 0xFF0000);
		}
	}
	
	@Override
	public boolean isPauseScreen() {
		return false;
	}
}