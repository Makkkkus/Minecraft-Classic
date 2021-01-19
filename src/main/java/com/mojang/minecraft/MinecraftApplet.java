package com.mojang.minecraft;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;

public class MinecraftApplet extends Applet
{
	private static final long serialVersionUID = 1L;

	private Canvas canvas;
	private Game game;

	private Thread thread = null;

	public void init()
	{
		canvas = new MinecraftCanvas(this);

		boolean fullscreen = false;

		if(getParameter("fullscreen") != null)
		{
			fullscreen = getParameter("fullscreen").equalsIgnoreCase("true");
		}

		game = new Game(canvas, this, getWidth(), getHeight(), fullscreen);

		game.host = getDocumentBase().getHost();

		if(getDocumentBase().getPort() > 0)
		{
			game.host = game.host + ":" + getDocumentBase().getPort();
		}

		if(getParameter("username") != null && getParameter("sessionid") != null)
		{
			game.session = new SessionData(getParameter("username"), getParameter("sessionid"));

			if(getParameter("mppass") != null)
			{
				game.session.mppass = getParameter("mppass");
			}

			// TODO: Not tested.
			game.session.haspaid = getParameter("haspaid").equalsIgnoreCase("true");
		}

		if(getParameter("loadmap_user") != null && getParameter("loadmap_id") != null)
		{
			game.levelName = getParameter("loadmap_user");
			game.levelId = Integer.parseInt(getParameter("loadmap_id"));
		} else if(getParameter("server") != null && getParameter("port") != null) {
			String server = getParameter("server");
			int port = Integer.parseInt(getParameter("port"));

			game.server = server;
			game.port = port;
		}

		game.levelLoaded = true;

		setLayout(new BorderLayout());

		add(canvas, "Center");

		canvas.setFocusable(true);

		validate();
	}

	public void startGameThread()
	{
		if(thread == null)
		{
			thread = new Thread(game);

			thread.start();
		}
	}

	@Override
	public void start()
	{
		game.loop.waiting = false;
	}

	@Override
	public void stop()
	{
		game.loop.waiting = true;
	}

	@Override
	public void destroy()
	{
		stopGameThread();
	}

	public void stopGameThread()
	{
		if(thread != null)
		{
			game.loop.running = false;

			try {
				thread.join(1000L);
			} catch (InterruptedException var3) {
				game.shutdown();
			}

			thread = null;
		}
	}
}
