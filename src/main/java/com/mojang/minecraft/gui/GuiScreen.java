package com.mojang.minecraft.gui;

import com.mojang.minecraft.Minecraft;

import java.util.ArrayList;
import java.util.List;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiScreen extends Screen {

   protected Minecraft minecraft;
   protected int width;
   protected int height;
   protected List<Button> buttons = new ArrayList<Button>();
   public boolean grabsMouse = false;
   protected FontRenderer fontRenderer;


   public void render(int var1, int var2) {
      for (Button button : this.buttons) {
         if (button.visible) {
            FontRenderer var8 = minecraft.fontRenderer;
            GL11.glBindTexture(3553, minecraft.textureManager.load("/gui/gui.png"));
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            byte var9 = 1;
            boolean var6 = var1 >= button.x && var2 >= button.y && var1 < button.x + button.width && var2 < button.y + button.height;
            if (!button.active) {
               var9 = 0;
            } else if (var6) {
               var9 = 2;
            }

            button.drawImage(button.x, button.y, 0, 46 + var9 * 20, button.width / 2, button.height);
            button.drawImage(button.x + button.width / 2, button.y, 200 - button.width / 2, 46 + var9 * 20, button.width / 2, button.height);
            if (!button.active) {
               Button.drawCenteredString(var8, button.text, button.x + button.width / 2, button.y + (button.height - 8) / 2, -6250336);
            } else if (var6) {
               Button.drawCenteredString(var8, button.text, button.x + button.width / 2, button.y + (button.height - 8) / 2, 16777120);
            } else {
               Button.drawCenteredString(var8, button.text, button.x + button.width / 2, button.y + (button.height - 8) / 2, 14737632);
            }
         }
      }

   }

   protected void onKeyPress(char var1, int var2) {
      if(var2 == 1) {
         this.minecraft.setCurrentScreen((GuiScreen)null);
         this.minecraft.grabMouse();
      }

   }

   protected void onMouseClick(int var1, int var2, int var3) {
      if(var3 == 0) {
         for(var3 = 0; var3 < this.buttons.size(); ++var3) {
            Button var4;
            Button var7;
            if((var7 = var4 = (Button)this.buttons.get(var3)).active && var1 >= var7.x && var2 >= var7.y && var1 < var7.x + var7.width && var2 < var7.y + var7.height) {
               this.onButtonClick(var4);
            }
         }
      }

   }

   protected void onButtonClick(Button var1) {}

   public final void open(Minecraft game, int windowWidth, int windowHeight) {
      this.minecraft = game;
      this.fontRenderer = game.fontRenderer;
      this.width = windowWidth;
      this.height = windowHeight;
      this.onOpen();
   }

   public void onOpen() {}

   public final void doInput() {
      while(Mouse.next()) {
         this.mouseEvent();
      }

      while(Keyboard.next()) {
         this.keyboardEvent();
      }

   }

   public final void mouseEvent() {
      if(Mouse.getEventButtonState()) {
         int var1 = Mouse.getEventX() * this.width / this.minecraft.width;
         int var2 = this.height - Mouse.getEventY() * this.height / this.minecraft.height - 1;
         this.onMouseClick(var1, var2, Mouse.getEventButton());
      }

   }

   public final void keyboardEvent() {
      if(Keyboard.getEventKeyState()) {
         this.onKeyPress(Keyboard.getEventCharacter(), Keyboard.getEventKey());
      }

   }

   public void tick() {}

   public void onClose() {}
}
