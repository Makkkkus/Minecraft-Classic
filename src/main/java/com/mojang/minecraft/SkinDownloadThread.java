package com.mojang.minecraft;

import com.mojang.minecraft.player.Player;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;

public class SkinDownloadThread extends Thread
{
	public SkinDownloadThread(Game game)
	{
		super();

		this.game = game;
	}

	@Override
	public void run()
	{
		if(game.session != null)
		{
			HttpURLConnection connection = null;

			try {
				connection = (HttpURLConnection)new URL("http://www.minecraft.net/skin/" + game.session.username + ".png").openConnection();

				connection.setDoInput(true);
				connection.setDoOutput(false);

				connection.connect();

				if(connection.getResponseCode() != 404)
				{
					Player.newTexture = ImageIO.read(connection.getInputStream());

					return;
				}
			} catch (Exception var4) {
				var4.printStackTrace();
			} finally {
				if(connection != null)
				{
					connection.disconnect();
				}
			}

		}
	}

	private Game game;
}
