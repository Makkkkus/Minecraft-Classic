package com.mojang.minecraft.net;

import com.mojang.minecraft.Game;
import com.mojang.minecraft.gui.ErrorScreen;
import com.mojang.net.NetworkHandler;

public class ServerConnectThread extends Thread
{
	public ServerConnectThread(NetworkManager networkManager, String server, int port, String username, String key, Game game) {
		super();

		netManager = networkManager;

		this.server = server;
		this.port = port;

		this.username = username;
		this.key = key;

		this.game = game;
	}

	@Override
	public void run()
	{
		try {
			netManager.netHandler = new NetworkHandler(server, port);
			netManager.netHandler.netManager = netManager;

			netManager.netHandler.send(PacketType.IDENTIFICATION, new Object[]{Byte.valueOf((byte)7), this.username, this.key, Integer.valueOf(0)});

			netManager.successful = true;
		} catch (Exception var3) {
			game.online = false;

			game.networkManager = null;

			game.setCurrentScreen(new ErrorScreen("Failed to connect", "You failed to connect to the server. It\'s probably down!"));

			netManager.successful = false;
		}
	}

	private String server;
	private int port;

	private String username;
	private String key;

	private Game game;

	private NetworkManager netManager;
}
