package com.mojang.minecraft.net;

import com.mojang.minecraft.Game;
import com.mojang.minecraft.gui.ErrorScreen;
import com.mojang.net.NetworkHandler;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class NetworkManager
{
	public NetworkManager(Game game, String server, int port, String username, String key)
	{
		game.online = true;

		this.game = game;

		players = new HashMap<Byte, NetworkPlayer>();

		new ServerConnectThread(this, server, port, username, key, game).start();
	}

	public ByteArrayOutputStream levelData;

	public NetworkHandler netHandler;

	public Game game;

	public boolean successful = false;
	public boolean levelLoaded = false;

	public HashMap<Byte, NetworkPlayer> players;

	public void sendBlockChange(int x, int y, int z, int mode, int block)
	{
		netHandler.send(PacketType.PLAYER_SET_BLOCK, x, y, z, mode, block);
	}

	public void error(Exception e)
	{
		netHandler.close();

		ErrorScreen errorScreen = new ErrorScreen("Disconnected!", e.getMessage());

		game.setCurrentScreen(errorScreen);

		e.printStackTrace();
	}

	public boolean isConnected()
	{
		return netHandler != null && netHandler.connected;
	}

	public List<String> getPlayers()
	{
		ArrayList<String> list = new ArrayList<String>();

		list.add(game.session.username);

		for (NetworkPlayer networkPlayer : this.players.values()) {
			list.add(networkPlayer.name);
		}

		return list;
	}
}
