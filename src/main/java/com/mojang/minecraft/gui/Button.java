package com.mojang.minecraft.gui;

public class Button extends Screen {

	public int x;
	public int y;
	public String text;
	public int id;
	public boolean active;
	public boolean visible;
	int width;
	int height;


	public Button(int id, int x, int y, String text) {
		this(id, x, y, 200, 20, text);
	}

	protected Button(int id, int x, int y, int width, int height, String text) {
		this.width = 200;
		this.height = 20;
		this.active = true;
		this.visible = true;
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.text = text;
	}
}
