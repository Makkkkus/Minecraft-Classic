package org.oyasunadev.minecraft;

import com.mojang.minecraft.Game;
import com.mojang.minecraft.MinecraftCanvas;

import javax.swing.*;
import java.awt.*;

public final class SPFrame extends JFrame
{
	public SPFrame()
	{
		setTitle("Minecraft");
		setSize(854, 480);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BorderLayout());

		int width = Toolkit.getDefaultToolkit().getScreenSize().width;
		int height = Toolkit.getDefaultToolkit().getScreenSize().height;

		if(getWidth() == width && getHeight() == height)
		{
			setUndecorated(true);
		}
	}

	public void startMinecraft()
	{
		MCApplet applet = new MCApplet();
		MinecraftCanvas canvas = new MinecraftCanvas(applet);

		Game game = new Game(canvas, applet, getWidth(), getHeight(), false);

		canvas.setSize(getWidth(), getHeight());

		add(canvas, "Center");

		pack();

		new Thread(game).start();
	}

	public void finish()
	{
		setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - getWidth()) / 2,
				(Toolkit.getDefaultToolkit().getScreenSize().height - getHeight()) / 2);
	}
}
