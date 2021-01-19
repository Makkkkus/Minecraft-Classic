package com.mojang.minecraft.render;

import com.mojang.minecraft.Game;
import com.mojang.minecraft.level.tile.Block;

public class HeldBlock
{
	public HeldBlock(Game game)
	{
		this.game = game;
	}

	public Game game;

	public Block block = null;

	public float pos = 0.0F;
	public float lastPos = 0.0F;

	public int offset = 0;

	public boolean moving = false;
}
