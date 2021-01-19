package org.oyasunadev.minecraft;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: Oliver Yasuna
 * Date: 9/23/12
 * Time: 4:04 PM
 */
public class Start
{
	public static void main(String[] args)
	{
		new Start();
	}

	public Start()
	{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		SPFrame spFrame = new SPFrame();

		spFrame.setVisible(true);

		spFrame.startMinecraft();
		spFrame.finish();
	}
}
