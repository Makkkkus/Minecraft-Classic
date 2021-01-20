package com.mojang.minecraft.gui;

import org.lwjgl.opengl.GL11;

public final class GameOverScreen extends GuiScreen {

	public final void onOpen() {
		buttons.clear();
		buttons.add(new Button(1, width / 2 - 100, height / 4 + 72, "Generate new level..."));
		buttons.add(new Button(2, width / 2 - 100, height / 4 + 96, "Load level.."));
		if (game.session == null) {
			buttons.get(1).active = false;
		}

	}

	protected final void onButtonClick(Button button) {
		if (button.id == 0) {
			game.setCurrentScreen(new OptionsScreen(this, this.game.settings));
		}

		//TODO: Add respawning
		if (button.id == 1) {
			game.setCurrentScreen(new GenerateLevelScreen(this));
		}

		if (game.session != null && button.id == 2) {
			game.setCurrentScreen(new LoadLevelScreen(this));
		}

	}

	public final void render(int var1, int var2) {
		drawFadingBox(0, 0, this.width, this.height, 1615855616, -1602211792);
		GL11.glPushMatrix();
		GL11.glScalef(2.0F, 2.0F, 2.0F);
		drawCenteredString(this.fontRenderer, "Game over!", this.width / 2 / 2, 30, 16777215);
		GL11.glPopMatrix();
		drawCenteredString(this.fontRenderer, "Score: &e" + this.game.player.getScore(), this.width / 2, 100, 16777215);
		super.render(var1, var2);
	}
}
