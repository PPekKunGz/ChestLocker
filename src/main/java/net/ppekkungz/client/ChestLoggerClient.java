package net.ppekkungz.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class ChestLoggerClient implements ClientModInitializer {
	
	@Override
	public void onInitializeClient() {
//		ClientTickEvents.END_CLIENT_TICK.register(client -> {
//			System.out.println("Client tick!");
//		});
	}
}
