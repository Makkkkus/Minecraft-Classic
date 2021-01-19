package com.mojang.minecraft;

import java.awt.Canvas;

public class MinecraftCanvas extends Canvas
{
	public MinecraftCanvas(MinecraftApplet minecraftApplet)
	{
		this.applet = minecraftApplet;
	}

	@Override
	public synchronized void addNotify()
	{
		super.addNotify();

		applet.startGameThread();
	}

	@Override
	public synchronized void removeNotify()
	{
		applet.stopGameThread();

		super.removeNotify();
	}

	private static final long serialVersionUID = 1L;

	private MinecraftApplet applet;
}
