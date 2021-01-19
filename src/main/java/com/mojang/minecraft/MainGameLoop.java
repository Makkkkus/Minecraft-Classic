package com.mojang.minecraft;


import com.mojang.minecraft.gamemode.CreativeGameMode;
import com.mojang.minecraft.gamemode.SurvivalGameMode;
import com.mojang.minecraft.gui.ChatInputScreen;
import com.mojang.minecraft.gui.ErrorScreen;
import com.mojang.minecraft.gui.HUDScreen;
import com.mojang.minecraft.item.Arrow;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.LevelIO;
import com.mojang.minecraft.level.liquid.LiquidType;
import com.mojang.minecraft.level.tile.Block;
import com.mojang.minecraft.model.ModelPart;
import com.mojang.minecraft.model.Vec3D;
import com.mojang.minecraft.net.NetworkManager;
import com.mojang.minecraft.net.NetworkPlayer;
import com.mojang.minecraft.net.PacketType;
import com.mojang.minecraft.particle.Particle;
import com.mojang.minecraft.particle.ParticleManager;
import com.mojang.minecraft.particle.WaterDropParticle;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.player.Player;
import com.mojang.minecraft.render.*;
import com.mojang.minecraft.render.texture.TextureFX;
import com.mojang.minecraft.sound.SoundManager;
import com.mojang.minecraft.sound.SoundPlayer;
import com.mojang.net.NetworkHandler;
import com.mojang.util.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class MainGameLoop {
	public static boolean running = false;
	public static boolean waiting = false;
	public static Timer timer = null;
	public static Renderer renderer = null;
	public static Game game = null;
	public static int lastClick = 0;
	public static int ticks = 0;
	public static int blockHitTime = 0;

	public static void loop() {
		long curTime = System.currentTimeMillis();
		int var15 = 0;
		try {
			while (running) {
				if (waiting) {
					Thread.sleep(100L);
				} else {
					if (game.canvas == null && Display.isCloseRequested()) {
						running = false;
					}

					try {
						long var16;
						long var18 = (var16 = System.currentTimeMillis()) - timer.lastSysClock;
						long var20 = System.nanoTime() / 1000000L;
						double var24;
						if(var18 > 1000L) {
							long var22 = var20 - timer.lastHRClock;
							var24 = (double)var18 / (double)var22;
							timer.adjustment += (var24 - timer.adjustment) * 0.20000000298023224D;
							timer.lastSysClock = var16;
							timer.lastHRClock = var20;
						}

						if(var18 < 0L) {
							timer.lastSysClock = var16;
							timer.lastHRClock = var20;
						}

						double var95;
						var24 = ((var95 = (double)var20 / 1000.0D) - timer.lastHR) * timer.adjustment;
						timer.lastHR = var95;
						if(var24 < 0.0D) {
							var24 = 0.0D;
						}

						if (var24 > 1.0D) {
							var24 = 1.0D;
						}

						timer.elapsedDelta = (float)((double)timer.elapsedDelta + var24 * (double)timer.speed * (double)timer.tps);
						timer.elapsedTicks = (int)timer.elapsedDelta;
						if(timer.elapsedTicks > 100) {
							timer.elapsedTicks = 100;
						}

						timer.elapsedDelta -= (float) timer.elapsedTicks;
						timer.delta = timer.elapsedDelta;

						for (int var64 = 0; var64 < timer.elapsedTicks; ++var64) {
							++ticks;
							tick();
						}

						game.checkGLError("Pre render");
						GL11.glEnable(3553);
						if (!game.online) {
							game.gamemode.applyCracks(timer.delta);
							float deltaTime = timer.delta;
							if (renderer.displayActive && !Display.isActive()) {
								game.pause();
							}

							renderer.displayActive = Display.isActive();
							int var68;
							int var70;
							int var86;
							int var81;
							if (game.hasMouse) {
								var81 = 0;
								var86 = 0;
								if (game.levelLoaded) {
									if (game.canvas != null) {
										Point var90;
										var70 = (var90 = game.canvas.getLocationOnScreen()).x + game.width / 2;
										var68 = var90.y + game.height / 2;
										Point var75;
										var81 = (var75 = MouseInfo.getPointerInfo().getLocation()).x - var70;
										var86 = -(var75.y - var68);
										game.robot.mouseMove(var70, var68);
									} else {
										Mouse.setCursorPosition(game.width / 2, game.height / 2);
									}
								} else {
									var81 = Mouse.getDX();
									var86 = Mouse.getDY();
								}

								byte var91 = 1;
								if (game.settings.invertMouse) {
									var91 = -1;
								}

								game.player.turn((float) var81, (float) (var86 * var91));
							}

							if (!game.online) {
								var81 = game.width * 240 / game.height;
								var86 = game.height * 240 / game.height;
								int var94 = Mouse.getX() * var81 / game.width;
								var70 = var86 - Mouse.getY() * var86 / game.height - 1;
								if (game.level != null) {
									Player var28;
									float var29 = (var28 = game.player).xRotO + (var28.xRot - var28.xRotO) * deltaTime;
									float var30 = var28.yRotO + (var28.yRot - var28.yRotO) * deltaTime;
									Vec3D var31 = renderer.getPlayerVector(deltaTime);
									float var32 = MathHelper.cos(-var30 * 0.017453292F - 3.1415927F);
									float var69 = MathHelper.sin(-var30 * 0.017453292F - 3.1415927F);
									float var74 = MathHelper.cos(-var29 * 0.017453292F);
									float var33 = MathHelper.sin(-var29 * 0.017453292F);
									float var34 = var69 * var74;
									float var87 = var32 * var74;
									float var36 = game.gamemode.getReachDistance();
									Vec3D var71 = var31.add(var34 * var36, var33 * var36, var87 * var36);
									game.selected = game.level.clip(var31, var71);
									var74 = var36;
									if (game.selected != null) {
										var74 = game.selected.vec.distance(renderer.getPlayerVector(deltaTime));
									}

									var31 = renderer.getPlayerVector(deltaTime);
									if (game.gamemode instanceof CreativeGameMode) {
										var36 = 32.0F;
									} else {
										var36 = var74;
									}

									var71 = var31.add(var34 * var36, var33 * var36, var87 * var36);
									renderer.entity = null;
									List var37 = game.level.blockMap.getEntities(var28, var28.bb.expand(var34 * var36, var33 * var36, var87 * var36));
									float var35 = 0.0F;

									for (var81 = 0; var81 < var37.size(); ++var81) {
										Entity var88;
										if ((var88 = (Entity) var37.get(var81)).isPickable()) {
											var74 = 0.1F;
											MovingObjectPosition var78;
											if ((var78 = var88.bb.grow(var74, var74, var74).clip(var31, var71)) != null && ((var74 = var31.distance(var78.vec)) < var35 || var35 == 0.0F)) {
												renderer.entity = var88;
												var35 = var74;
											}
										}
									}

									if (renderer.entity != null && !(game.gamemode instanceof CreativeGameMode)) {
										game.selected = new MovingObjectPosition(renderer.entity);
									}

									int var77 = 0;

									while (true) {
										if (var77 >= 2) {
											GL11.glColorMask(true, true, true, false);
											break;
										}

										if (game.settings.anaglyph) {
											if (var77 == 0) {
												GL11.glColorMask(false, true, true, false);
											} else {
												GL11.glColorMask(true, false, false, false);
											}
										}

										Player var126 = game.player;
										Level var119 = game.level;
										LevelRenderer var89 = game.levelRenderer;
										ParticleManager var93 = game.particleManager;
										GL11.glViewport(0, 0, game.width, game.height);
										Level var26 = game.level;
										var28 = game.player;
										var29 = 1.0F / (float) (4 - game.settings.viewDistance);
										var29 = 1.0F - (float) Math.pow(var29, 0.25D);
										var30 = (float) (var26.skyColor >> 16 & 255) / 255.0F;
										float var117 = (float) (var26.skyColor >> 8 & 255) / 255.0F;
										var32 = (float) (var26.skyColor & 255) / 255.0F;
										renderer.fogRed = (float) (var26.fogColor >> 16 & 255) / 255.0F;
										renderer.fogBlue = (float) (var26.fogColor >> 8 & 255) / 255.0F;
										renderer.fogGreen = (float) (var26.fogColor & 255) / 255.0F;
										renderer.fogRed += (var30 - renderer.fogRed) * var29;
										renderer.fogBlue += (var117 - renderer.fogBlue) * var29;
										renderer.fogGreen += (var32 - renderer.fogGreen) * var29;
										renderer.fogRed *= renderer.fogColorMultiplier;
										renderer.fogBlue *= renderer.fogColorMultiplier;
										renderer.fogGreen *= renderer.fogColorMultiplier;
										Block var73;
										if ((var73 = Block.blocks[var26.getTile((int) var28.x, (int) (var28.y + 0.12F), (int) var28.z)]) != null && var73.getLiquidType() != LiquidType.NOT_LIQUID) {
											LiquidType var79;
											if ((var79 = var73.getLiquidType()) == LiquidType.WATER) {
												renderer.fogRed = 0.02F;
												renderer.fogBlue = 0.02F;
												renderer.fogGreen = 0.2F;
											} else if (var79 == LiquidType.LAVA) {
												renderer.fogRed = 0.6F;
												renderer.fogBlue = 0.1F;
												renderer.fogGreen = 0.0F;
											}
										}

										if (game.settings.anaglyph) {
											var74 = (renderer.fogRed * 30.0F + renderer.fogBlue * 59.0F + renderer.fogGreen * 11.0F) / 100.0F;
											var33 = (renderer.fogRed * 30.0F + renderer.fogBlue * 70.0F) / 100.0F;
											var34 = (renderer.fogRed * 30.0F + renderer.fogGreen * 70.0F) / 100.0F;
											renderer.fogRed = var74;
											renderer.fogBlue = var33;
											renderer.fogGreen = var34;
										}

										GL11.glClearColor(renderer.fogRed, renderer.fogBlue, renderer.fogGreen, 0.0F);
										GL11.glClear(16640);
										renderer.fogColorMultiplier = 1.0F;
										GL11.glEnable(2884);
										renderer.fogEnd = (float) (512 >> (game.settings.viewDistance << 1));
										GL11.glMatrixMode(5889);
										GL11.glLoadIdentity();
										var29 = 0.07F;
										if (game.settings.anaglyph) {
											GL11.glTranslatef((float) (-((var77 << 1) - 1)) * var29, 0.0F, 0.0F);
										}

										Player var116 = game.player;
										var69 = 70.0F;
										if (var116.health <= 0) {
											var74 = (float) var116.deathTime + deltaTime;
											var69 /= (1.0F - 500.0F / (var74 + 500.0F)) * 2.0F + 1.0F;
										}

										GLU.gluPerspective(var69, (float) game.width / (float) game.height, 0.05F, renderer.fogEnd);
										GL11.glMatrixMode(5888);
										GL11.glLoadIdentity();
										if (game.settings.anaglyph) {
											GL11.glTranslatef((float) ((var77 << 1) - 1) * 0.1F, 0.0F, 0.0F);
										}

										renderer.hurtEffect(deltaTime);
										if (game.settings.viewBobbing) {
											renderer.applyBobbing(deltaTime);
										}

										var116 = game.player;
										GL11.glTranslatef(0.0F, 0.0F, -0.1F);
										GL11.glRotatef(var116.xRotO + (var116.xRot - var116.xRotO) * deltaTime, 1.0F, 0.0F, 0.0F);
										GL11.glRotatef(var116.yRotO + (var116.yRot - var116.yRotO) * deltaTime, 0.0F, 1.0F, 0.0F);
										var69 = var116.xo + (var116.x - var116.xo) * deltaTime;
										var74 = var116.yo + (var116.y - var116.yo) * deltaTime;
										var33 = var116.zo + (var116.z - var116.zo) * deltaTime;
										GL11.glTranslatef(-var69, -var74, -var33);
										Frustrum var76 = FrustrumImpl.update();
										Frustrum var100 = var76;
										LevelRenderer var101 = game.levelRenderer;

										int var98;
										for (var98 = 0; var98 < var101.chunkCache.length; ++var98) {
											var101.chunkCache[var98].clip(var100);
										}

										var101 = game.levelRenderer;
										Collections.sort(game.levelRenderer.chunks, new ChunkDirtyDistanceComparator(var126));
										var98 = var101.chunks.size() - 1;
										int var105;
										if ((var105 = var101.chunks.size()) > 3) {
											var105 = 3;
										}

										int var104;
										for (var104 = 0; var104 < var105; ++var104) {
											Chunk var118;
											(var118 = (Chunk) var101.chunks.remove(var98 - var104)).update();
											var118.loaded = false;
										}

										renderer.updateFog();
										GL11.glEnable(2912);
										var89.sortChunks(var126, 0);
										int var83;
										int var110;
										ShapeRenderer var115;
										int var114;
										int var125;
										int var122;
										int var120;
										if (var119.isSolid(var126.x, var126.y, var126.z, 0.1F)) {
											var120 = (int) var126.x;
											var83 = (int) var126.y;
											var110 = (int) var126.z;

											for (var122 = var120 - 1; var122 <= var120 + 1; ++var122) {
												for (var125 = var83 - 1; var125 <= var83 + 1; ++var125) {
													for (int var38 = var110 - 1; var38 <= var110 + 1; ++var38) {
														var105 = var38;
														var98 = var125;
														int var99 = var122;
														if ((var104 = var89.level.getTile(var122, var125, var38)) != 0 && Block.blocks[var104].isSolid()) {
															GL11.glColor4f(0.2F, 0.2F, 0.2F, 1.0F);
															GL11.glDepthFunc(513);
															var115 = ShapeRenderer.instance;
															ShapeRenderer.instance.begin();

															for (var114 = 0; var114 < 6; ++var114) {
																Block.blocks[var104].renderInside(var115, var99, var98, var105, var114);
															}

															var115.end();
															GL11.glCullFace(1028);
															var115.begin();

															for (var114 = 0; var114 < 6; ++var114) {
																Block.blocks[var104].renderInside(var115, var99, var98, var105, var114);
															}

															var115.end();
															GL11.glCullFace(1029);
															GL11.glDepthFunc(515);
														}
													}
												}
											}
										}

										renderer.setLighting(true);
										Vec3D var103 = renderer.getPlayerVector(deltaTime);
										var89.level.blockMap.render(var103, var76, var89.textureManager, deltaTime);
										renderer.setLighting(false);
										renderer.updateFog();
										var29 = -MathHelper.cos(var126.yRot * 3.1415927F / 180.0F);
										var117 = -(var30 = -MathHelper.sin(var126.yRot * 3.1415927F / 180.0F)) * MathHelper.sin(var126.xRot * 3.1415927F / 180.0F);
										var32 = var29 * MathHelper.sin(var126.xRot * 3.1415927F / 180.0F);
										var69 = MathHelper.cos(var126.xRot * 3.1415927F / 180.0F);

										for (var83 = 0; var83 < 2; ++var83) {
											if (var93.particles[var83].size() != 0) {
												var110 = 0;
												if (var83 == 0) {
													var110 = var93.textureManager.load("/particles.png");
												}

												if (var83 == 1) {
													var110 = var93.textureManager.load("/terrain.png");
												}

												GL11.glBindTexture(3553, var110);
												ShapeRenderer var121 = ShapeRenderer.instance;
												ShapeRenderer.instance.begin();

												for (var120 = 0; var120 < var93.particles[var83].size(); ++var120) {
													((Particle) var93.particles[var83].get(var120)).render(var121, deltaTime, var29, var69, var30, var117, var32);
												}

												var121.end();
											}
										}

										GL11.glBindTexture(3553, var89.textureManager.load("/rock.png"));
										GL11.glEnable(3553);
										GL11.glCallList(var89.listId);
										renderer.updateFog();
										var101 = var89;
										GL11.glBindTexture(3553, var89.textureManager.load("/clouds.png"));
										GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
										deltaTime = (float) (var89.level.cloudColor >> 16 & 255) / 255.0F;
										var29 = (float) (var89.level.cloudColor >> 8 & 255) / 255.0F;
										var30 = (float) (var89.level.cloudColor & 255) / 255.0F;
										if (var89.game.settings.anaglyph) {
											var117 = (deltaTime * 30.0F + var29 * 59.0F + var30 * 11.0F) / 100.0F;
											var32 = (deltaTime * 30.0F + var29 * 70.0F) / 100.0F;
											var69 = (deltaTime * 30.0F + var30 * 70.0F) / 100.0F;
											deltaTime = var117;
											var29 = var32;
											var30 = var69;
										}

										var115 = ShapeRenderer.instance;
										var74 = 0.0F;
										var33 = 4.8828125E-4F;
										var74 = (float) (var89.level.depth + 2);
										var34 = ((float) var89.ticks + deltaTime) * var33 * 0.03F;
										var35 = 0.0F;
										var115.begin();
										var115.color(deltaTime, var29, var30);

										for (var86 = -2048; var86 < var101.level.width + 2048; var86 += 512) {
											for (var125 = -2048; var125 < var101.level.height + 2048; var125 += 512) {
												var115.vertexUV((float) var86, var74, (float) (var125 + 512), (float) var86 * var33 + var34, (float) (var125 + 512) * var33);
												var115.vertexUV((float) (var86 + 512), var74, (float) (var125 + 512), (float) (var86 + 512) * var33 + var34, (float) (var125 + 512) * var33);
												var115.vertexUV((float) (var86 + 512), var74, (float) var125, (float) (var86 + 512) * var33 + var34, (float) var125 * var33);
												var115.vertexUV((float) var86, var74, (float) var125, (float) var86 * var33 + var34, (float) var125 * var33);
												var115.vertexUV((float) var86, var74, (float) var125, (float) var86 * var33 + var34, (float) var125 * var33);
												var115.vertexUV((float) (var86 + 512), var74, (float) var125, (float) (var86 + 512) * var33 + var34, (float) var125 * var33);
												var115.vertexUV((float) (var86 + 512), var74, (float) (var125 + 512), (float) (var86 + 512) * var33 + var34, (float) (var125 + 512) * var33);
												var115.vertexUV((float) var86, var74, (float) (var125 + 512), (float) var86 * var33 + var34, (float) (var125 + 512) * var33);
											}
										}

										var115.end();
										GL11.glDisable(3553);
										var115.begin();
										var34 = (float) (var101.level.skyColor >> 16 & 255) / 255.0F;
										var35 = (float) (var101.level.skyColor >> 8 & 255) / 255.0F;
										var87 = (float) (var101.level.skyColor & 255) / 255.0F;
										if (var101.game.settings.anaglyph) {
											var36 = (var34 * 30.0F + var35 * 59.0F + var87 * 11.0F) / 100.0F;
											var69 = (var34 * 30.0F + var35 * 70.0F) / 100.0F;
											var74 = (var34 * 30.0F + var87 * 70.0F) / 100.0F;
											var34 = var36;
											var35 = var69;
											var87 = var74;
										}

										var115.color(var34, var35, var87);
										var74 = (float) (var101.level.depth + 10);

										for (var125 = -2048; var125 < var101.level.width + 2048; var125 += 512) {
											for (var68 = -2048; var68 < var101.level.height + 2048; var68 += 512) {
												var115.vertex((float) var125, var74, (float) var68);
												var115.vertex((float) (var125 + 512), var74, (float) var68);
												var115.vertex((float) (var125 + 512), var74, (float) (var68 + 512));
												var115.vertex((float) var125, var74, (float) (var68 + 512));
											}
										}

										var115.end();
										GL11.glEnable(3553);
										renderer.updateFog();
										int var108;
										if (game.selected != null) {
											GL11.glDisable(3008);
											MovingObjectPosition var10001 = game.selected;
											var105 = var126.inventory.getSelected();
											boolean var106 = false;
											MovingObjectPosition var102 = var10001;
											var101 = var89;
											ShapeRenderer var113 = ShapeRenderer.instance;
											GL11.glEnable(3042);
											GL11.glEnable(3008);
											GL11.glBlendFunc(770, 1);
											GL11.glColor4f(1.0F, 1.0F, 1.0F, (MathHelper.sin((float) System.currentTimeMillis() / 100.0F) * 0.2F + 0.4F) * 0.5F);
											if (var89.cracks > 0.0F) {
												GL11.glBlendFunc(774, 768);
												var108 = var89.textureManager.load("/terrain.png");
												GL11.glBindTexture(3553, var108);
												GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
												GL11.glPushMatrix();
												Block var10000 = (var114 = var89.level.getTile(var102.x, var102.y, var102.z)) > 0 ? Block.blocks[var114] : null;
												var73 = var10000;
												var74 = (var10000.x1 + var73.x2) / 2.0F;
												var33 = (var73.y1 + var73.y2) / 2.0F;
												var34 = (var73.z1 + var73.z2) / 2.0F;
												GL11.glTranslatef((float) var102.x + var74, (float) var102.y + var33, (float) var102.z + var34);
												var35 = 1.01F;
												GL11.glScalef(1.01F, var35, var35);
												GL11.glTranslatef(-((float) var102.x + var74), -((float) var102.y + var33), -((float) var102.z + var34));
												var113.begin();
												var113.noColor();
												GL11.glDepthMask(false);
												if (var73 == null) {
													var73 = Block.STONE;
												}

												for (var86 = 0; var86 < 6; ++var86) {
													var73.renderSide(var113, var102.x, var102.y, var102.z, var86, 240 + (int) (var101.cracks * 10.0F));
												}

												var113.end();
												GL11.glDepthMask(true);
												GL11.glPopMatrix();
											}

											GL11.glDisable(3042);
											GL11.glDisable(3008);
											var10001 = game.selected;
											var126.inventory.getSelected();
											var106 = false;
											var102 = var10001;
											GL11.glEnable(3042);
											GL11.glBlendFunc(770, 771);
											GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
											GL11.glLineWidth(2.0F);
											GL11.glDisable(3553);
											GL11.glDepthMask(false);
											var29 = 0.002F;
											if ((var104 = var89.level.getTile(var102.x, var102.y, var102.z)) > 0) {
												AABB var111 = Block.blocks[var104].getSelectionBox(var102.x, var102.y, var102.z).grow(var29, var29, var29);
												GL11.glBegin(3);
												GL11.glVertex3f(var111.x0, var111.y0, var111.z0);
												GL11.glVertex3f(var111.x1, var111.y0, var111.z0);
												GL11.glVertex3f(var111.x1, var111.y0, var111.z1);
												GL11.glVertex3f(var111.x0, var111.y0, var111.z1);
												GL11.glVertex3f(var111.x0, var111.y0, var111.z0);
												GL11.glEnd();
												GL11.glBegin(3);
												GL11.glVertex3f(var111.x0, var111.y1, var111.z0);
												GL11.glVertex3f(var111.x1, var111.y1, var111.z0);
												GL11.glVertex3f(var111.x1, var111.y1, var111.z1);
												GL11.glVertex3f(var111.x0, var111.y1, var111.z1);
												GL11.glVertex3f(var111.x0, var111.y1, var111.z0);
												GL11.glEnd();
												GL11.glBegin(1);
												GL11.glVertex3f(var111.x0, var111.y0, var111.z0);
												GL11.glVertex3f(var111.x0, var111.y1, var111.z0);
												GL11.glVertex3f(var111.x1, var111.y0, var111.z0);
												GL11.glVertex3f(var111.x1, var111.y1, var111.z0);
												GL11.glVertex3f(var111.x1, var111.y0, var111.z1);
												GL11.glVertex3f(var111.x1, var111.y1, var111.z1);
												GL11.glVertex3f(var111.x0, var111.y0, var111.z1);
												GL11.glVertex3f(var111.x0, var111.y1, var111.z1);
												GL11.glEnd();
											}

											GL11.glDepthMask(true);
											GL11.glEnable(3553);
											GL11.glDisable(3042);
											GL11.glEnable(3008);
										}

										GL11.glBlendFunc(770, 771);
										renderer.updateFog();
										GL11.glEnable(3553);
										GL11.glEnable(3042);
										GL11.glBindTexture(3553, var89.textureManager.load("/water.png"));
										GL11.glCallList(var89.listId + 1);
										GL11.glDisable(3042);
										GL11.glEnable(3042);
										GL11.glColorMask(false, false, false, false);
										var120 = var89.sortChunks(var126, 1);
										GL11.glColorMask(true, true, true, true);
										if (game.settings.anaglyph) {
											if (var77 == 0) {
												GL11.glColorMask(false, true, true, false);
											} else {
												GL11.glColorMask(true, false, false, false);
											}
										}

										if (var120 > 0) {
											GL11.glBindTexture(3553, var89.textureManager.load("/terrain.png"));
											GL11.glCallLists(var89.buffer);
										}

										GL11.glDepthMask(true);
										GL11.glDisable(3042);
										GL11.glDisable(2912);
										if (game.raining) {
											var28 = game.player;
											Level var109 = game.level;
											var104 = (int) var28.x;
											var108 = (int) var28.y;
											var114 = (int) var28.z;
											ShapeRenderer var84 = ShapeRenderer.instance;
											GL11.glDisable(2884);
											GL11.glNormal3f(0.0F, 1.0F, 0.0F);
											GL11.glEnable(3042);
											GL11.glBlendFunc(770, 771);
											GL11.glBindTexture(3553, game.textureManager.load("/rain.png"));

											for (var110 = var104 - 5; var110 <= var104 + 5; ++var110) {
												for (var122 = var114 - 5; var122 <= var114 + 5; ++var122) {
													var120 = var109.getHighestTile(var110, var122);
													var86 = var108 - 5;
													var125 = var108 + 5;
													if (var86 < var120) {
														var86 = var120;
													}

													if (var125 < var120) {
														var125 = var120;
													}

													if (var86 != var125) {
														var74 = ((float) ((renderer.levelTicks + var110 * 3121 + var122 * 418711) % 32) + deltaTime) / 32.0F;
														float var124 = (float) var110 + 0.5F - var28.x;
														var35 = (float) var122 + 0.5F - var28.z;
														float var92 = MathHelper.sqrt(var124 * var124 + var35 * var35) / (float) 5;
														GL11.glColor4f(1.0F, 1.0F, 1.0F, (1.0F - var92 * var92) * 0.7F);
														var84.begin();
														var84.vertexUV((float) var110, (float) var86, (float) var122, 0.0F, (float) var86 * 2.0F / 8.0F + var74 * 2.0F);
														var84.vertexUV((float) (var110 + 1), (float) var86, (float) (var122 + 1), 2.0F, (float) var86 * 2.0F / 8.0F + var74 * 2.0F);
														var84.vertexUV((float) (var110 + 1), (float) var125, (float) (var122 + 1), 2.0F, (float) var125 * 2.0F / 8.0F + var74 * 2.0F);
														var84.vertexUV((float) var110, (float) var125, (float) var122, 0.0F, (float) var125 * 2.0F / 8.0F + var74 * 2.0F);
														var84.vertexUV((float) var110, (float) var86, (float) (var122 + 1), 0.0F, (float) var86 * 2.0F / 8.0F + var74 * 2.0F);
														var84.vertexUV((float) (var110 + 1), (float) var86, (float) var122, 2.0F, (float) var86 * 2.0F / 8.0F + var74 * 2.0F);
														var84.vertexUV((float) (var110 + 1), (float) var125, (float) var122, 2.0F, (float) var125 * 2.0F / 8.0F + var74 * 2.0F);
														var84.vertexUV((float) var110, (float) var125, (float) (var122 + 1), 0.0F, (float) var125 * 2.0F / 8.0F + var74 * 2.0F);
														var84.end();
													}
												}
											}

											GL11.glEnable(2884);
											GL11.glDisable(3042);
										}

										if (renderer.entity != null) {
											renderer.entity.renderHover(game.textureManager, deltaTime);
										}

										GL11.glClear(256);
										GL11.glLoadIdentity();
										if (game.settings.anaglyph) {
											GL11.glTranslatef((float) ((var77 << 1) - 1) * 0.1F, 0.0F, 0.0F);
										}

										renderer.hurtEffect(deltaTime);
										if (game.settings.viewBobbing) {
											renderer.applyBobbing(deltaTime);
										}

										HeldBlock var112 = renderer.heldBlock;
										var117 = renderer.heldBlock.lastPos + (var112.pos - var112.lastPos) * deltaTime;
										var116 = var112.game.player;
										GL11.glPushMatrix();
										GL11.glRotatef(var116.xRotO + (var116.xRot - var116.xRotO) * deltaTime, 1.0F, 0.0F, 0.0F);
										GL11.glRotatef(var116.yRotO + (var116.yRot - var116.yRotO) * deltaTime, 0.0F, 1.0F, 0.0F);
										var112.game.renderer.setLighting(true);
										GL11.glPopMatrix();
										GL11.glPushMatrix();
										var69 = 0.8F;
										if (var112.moving) {
											var33 = MathHelper.sin((var74 = ((float) var112.offset + deltaTime) / 7.0F) * 3.1415927F);
											GL11.glTranslatef(-MathHelper.sin(MathHelper.sqrt(var74) * 3.1415927F) * 0.4F, MathHelper.sin(MathHelper.sqrt(var74) * 3.1415927F * 2.0F) * 0.2F, -var33 * 0.2F);
										}

										GL11.glTranslatef(0.7F * var69, -0.65F * var69 - (1.0F - var117) * 0.6F, -0.9F * var69);
										GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
										GL11.glEnable(2977);
										if (var112.moving) {
											var33 = MathHelper.sin((var74 = ((float) var112.offset + deltaTime) / 7.0F) * var74 * 3.1415927F);
											GL11.glRotatef(MathHelper.sin(MathHelper.sqrt(var74) * 3.1415927F) * 80.0F, 0.0F, 1.0F, 0.0F);
											GL11.glRotatef(-var33 * 20.0F, 1.0F, 0.0F, 0.0F);
										}

										GL11.glColor4f(var74 = var112.game.level.getBrightness((int) var116.x, (int) var116.y, (int) var116.z), var74, var74, 1.0F);
										ShapeRenderer var123 = ShapeRenderer.instance;
										if (var112.block != null) {
											var34 = 0.4F;
											GL11.glScalef(0.4F, var34, var34);
											GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
											GL11.glBindTexture(3553, var112.game.textureManager.load("/terrain.png"));
											var112.block.renderPreview(var123);
										} else {
											var116.bindTexture(var112.game.textureManager);
											GL11.glScalef(1.0F, -1.0F, -1.0F);
											GL11.glTranslatef(0.0F, 0.2F, 0.0F);
											GL11.glRotatef(-120.0F, 0.0F, 0.0F, 1.0F);
											GL11.glScalef(1.0F, 1.0F, 1.0F);
											var34 = 0.0625F;
											ModelPart var127;
											if (!(var127 = var112.game.player.getModel().leftArm).hasList) {
												var127.generateList(var34);
											}

											GL11.glCallList(var127.list);
										}

										GL11.glDisable(2977);
										GL11.glPopMatrix();
										var112.game.renderer.setLighting(false);
										if (!game.settings.anaglyph) {
											break;
										}

										++var77;
									}

									game.hud.render(deltaTime, game.currentScreen != null, var94, var70);
								} else {
									GL11.glViewport(0, 0, game.width, game.height);
									GL11.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
									GL11.glClear(16640);
									GL11.glMatrixMode(5889);
									GL11.glLoadIdentity();
									GL11.glMatrixMode(5888);
									GL11.glLoadIdentity();
									renderer.enableGuiMode();
								}

								if (game.currentScreen != null) {
									game.currentScreen.render(var94, var70);
								}

								Thread.yield();
								Display.update();
							}
						}

						if (game.settings.limitFramerate) {
							Thread.sleep(5L);
						}

						Game.checkGLError("Post render");
						++var15;
					} catch (Exception var58) {
						game.setCurrentScreen(new ErrorScreen("Client error", "The game broke! [" + var58 + "]"));
						var58.printStackTrace();
					}

					while (System.currentTimeMillis() >= curTime + 1000L) {
						game.debug = var15 + " fps, " + Chunk.chunkUpdates + " chunk updates";
						Chunk.chunkUpdates = 0;
						curTime += 1000L;
						var15 = 0;
					}
				}
			}

			return;
		} catch (StopGameException e) {
		} catch (Exception e) {
			e.printStackTrace();
			return;
		} finally {
			game.shutdown();
		}
	}

	public static void tick() {
		if (game.soundPlayer != null) {
			SoundPlayer var1 = game.soundPlayer;
			SoundManager var2 = game.sound;
			if (System.currentTimeMillis() > var2.lastMusic && var2.playMusic(var1, "calm")) {
				var2.lastMusic = System.currentTimeMillis() + (long) var2.random.nextInt(900000) + 300000L;
			}
		}

		game.gamemode.spawnMob();
		HUDScreen var17 = game.hud;
		++game.hud.ticks;

		int var16;
		for (var16 = 0; var16 < var17.chat.size(); ++var16) {
			++((ChatLine) var17.chat.get(var16)).time;
		}

		GL11.glBindTexture(3553, game.textureManager.load("/terrain.png"));
		TextureManager var19 = game.textureManager;

		for (var16 = 0; var16 < var19.animations.size(); ++var16) {
			TextureFX var3;
			(var3 = (TextureFX) var19.animations.get(var16)).anaglyph = var19.settings.anaglyph;
			var3.animate();
			var19.textureBuffer.clear();
			var19.textureBuffer.put(var3.textureData);
			var19.textureBuffer.position(0).limit(var3.textureData.length);
			GL11.glTexSubImage2D(3553, 0, var3.textureId % 16 << 4, var3.textureId / 16 << 4, 16, 16, 6408, 5121, var19.textureBuffer);
		}

		int var4;
		int var8;
		int var40;
		int var46;
		int var45;
		if (game.networkManager != null && !(game.currentScreen instanceof ErrorScreen)) {
			if (!game.networkManager.isConnected()) {
				game.progressBar.setTitle("Connecting..");
				game.progressBar.setProgress(0);
			} else {
				NetworkManager var20 = game.networkManager;
				if (game.networkManager.successful) {
					NetworkHandler var18 = var20.netHandler;
					if (var20.netHandler.connected) {
						try {
							NetworkHandler var22 = var20.netHandler;
							var20.netHandler.channel.read(var22.in);
							var4 = 0;

							while (var22.in.position() > 0 && var4++ != 100) {
								var22.in.flip();
								byte var5 = var22.in.get(0);
								PacketType var6;
								if ((var6 = PacketType.packets[var5]) == null) {
									throw new IOException("Bad command: " + var5);
								}

								if (var22.in.remaining() < var6.length + 1) {
									var22.in.compact();
									break;
								}

								var22.in.get();
								Object[] var7 = new Object[var6.params.length];

								for (var8 = 0; var8 < var7.length; ++var8) {
									var7[var8] = var22.readObject(var6.params[var8]);
								}

								NetworkManager var42 = var22.netManager;
								if (var22.netManager.successful) {
									if (var6 == PacketType.IDENTIFICATION) {
										var42.game.progressBar.setTitle(var7[1].toString());
										var42.game.progressBar.setText(var7[2].toString());
										var42.game.player.userType = (Byte) var7[3];
									} else if (var6 == PacketType.LEVEL_INIT) {
										var42.game.setLevel(null);
										var42.levelData = new ByteArrayOutputStream();
									} else if (var6 == PacketType.LEVEL_DATA) {
										short var11 = (Short) var7[0];
										byte[] var12 = (byte[]) var7[1];
										byte var13 = (Byte) var7[2];
										var42.game.progressBar.setProgress(var13);
										var42.levelData.write(var12, 0, var11);
									} else if (var6 == PacketType.LEVEL_FINALIZE) {
										try {
											var42.levelData.close();
										} catch (IOException var14) {
											var14.printStackTrace();
										}

										byte[] var51 = LevelIO.decompress(new ByteArrayInputStream(var42.levelData.toByteArray()));
										var42.levelData = null;
										short var55 = (Short) var7[0];
										short var63 = (Short) var7[1];
										short var21 = (Short) var7[2];
										Level var30;
										(var30 = new Level()).setNetworkMode(true);
										var30.setData(var55, var63, var21, var51);
										var42.game.setLevel(var30);
										var42.game.online = false;
										var42.levelLoaded = true;
									} else if (var6 == PacketType.BLOCK_CHANGE) {
										if (var42.game.level != null) {
											var42.game.level.netSetTile((Short) var7[0], (Short) var7[1], (Short) var7[2], (Byte) var7[3]);
										}
									} else {
										byte var9;
										String var34;
										NetworkPlayer var33;
										short var36;
										short var10004;
										byte var10001;
										short var47;
										short var10003;
										if (var6 == PacketType.SPAWN_PLAYER) {
											var10001 = (Byte) var7[0];
											String var10002 = (String) var7[1];
											var10003 = (Short) var7[2];
											var10004 = (Short) var7[3];
											short var10005 = (Short) var7[4];
											byte var10006 = (Byte) var7[5];
											byte var58 = (Byte) var7[6];
											var9 = var10006;
											short var10 = var10005;
											var47 = var10004;
											var36 = var10003;
											var34 = var10002;
											var5 = var10001;
											if (var5 >= 0) {
												var9 = (byte) (var9 + 128);
												var47 = (short) (var47 - 22);
												var33 = new NetworkPlayer(var42.game, var5, var34, var36, var47, var10, (float) (var9 * 360) / 256.0F, (float) (var58 * 360) / 256.0F);
												var42.players.put(Byte.valueOf(var5), var33);
												var42.game.level.addEntity(var33);
											} else {
												var42.game.level.setSpawnPos(var36 / 32, var47 / 32, var10 / 32, (float) (var9 * 320 / 256));
												var42.game.player.moveTo((float) var36 / 32.0F, (float) var47 / 32.0F, (float) var10 / 32.0F, (float) (var9 * 360) / 256.0F, (float) (var58 * 360) / 256.0F);
											}
										} else {
											byte var53;
											NetworkPlayer var61;
											byte var69;
											if (var6 == PacketType.POSITION_ROTATION) {
												var10001 = (Byte) var7[0];
												short var66 = (Short) var7[1];
												var10003 = (Short) var7[2];
												var10004 = (Short) var7[3];
												var69 = (Byte) var7[4];
												var9 = (Byte) var7[5];
												var53 = var69;
												var47 = var10004;
												var36 = var10003;
												short var38 = var66;
												var5 = var10001;
												if (var5 < 0) {
													var42.game.player.moveTo((float) var38 / 32.0F, (float) var36 / 32.0F, (float) var47 / 32.0F, (float) (var53 * 360) / 256.0F, (float) (var9 * 360) / 256.0F);
												} else {
													var53 = (byte) (var53 + 128);
													var36 = (short) (var36 - 22);
													if ((var61 = var42.players.get(Byte.valueOf(var5))) != null) {
														var61.teleport(var38, var36, var47, (float) (var53 * 360) / 256.0F, (float) (var9 * 360) / 256.0F);
													}
												}
											} else {
												byte var37;
												byte var44;
												byte var49;
												byte var65;
												byte var67;
												if (var6 == PacketType.POSITION_ROTATION_UPDATE) {
													var10001 = (Byte) var7[0];
													var67 = (Byte) var7[1];
													var65 = (Byte) var7[2];
													byte var64 = (Byte) var7[3];
													var69 = (Byte) var7[4];
													var9 = (Byte) var7[5];
													var53 = var69;
													var49 = var64;
													var44 = var65;
													var37 = var67;
													var5 = var10001;
													if (var5 >= 0) {
														var53 = (byte) (var53 + 128);
														if ((var61 = var42.players.get(var5)) != null) {
															var61.queue(var37, var44, var49, (float) (var53 * 360) / 256.0F, (float) (var9 * 360) / 256.0F);
														}
													}
												} else if (var6 == PacketType.ROTATION_UPDATE) {
													var10001 = (Byte) var7[0];
													var67 = (Byte) var7[1];
													var44 = (Byte) var7[2];
													var37 = var67;
													var5 = var10001;
													if (var5 >= 0) {
														var37 = (byte) (var37 + 128);
														NetworkPlayer var54;
														if ((var54 = var42.players.get(var5)) != null) {
															var54.queue((float) (var37 * 360) / 256.0F, (float) (var44 * 360) / 256.0F);
														}
													}
												} else if (var6 == PacketType.POSITION_UPDATE) {
													var10001 = (Byte) var7[0];
													var67 = (Byte) var7[1];
													var65 = (Byte) var7[2];
													var49 = (Byte) var7[3];
													var44 = var65;
													var37 = var67;
													var5 = var10001;
													NetworkPlayer var59;
													if (var5 >= 0 && (var59 = var42.players.get(var5)) != null) {
														var59.queue(var37, var44, var49);
													}
												} else if (var6 == PacketType.DESPAWN_PLAYER) {
													var5 = (Byte) var7[0];
													if (var5 >= 0 && (var33 = var42.players.remove(var5)) != null) {
														var33.clear();
														var42.game.level.removeEntity(var33);
													}
												} else if (var6 == PacketType.CHAT_MESSAGE) {
													var10001 = (Byte) var7[0];
													var34 = (String) var7[1];
													var5 = var10001;
													if (var5 < 0) {
														var42.game.hud.addChat("&e" + var34);
													} else {
														var42.players.get(var5);
														var42.game.hud.addChat(var34);
													}
												} else if (var6 == PacketType.DISCONNECT) {
													var42.netHandler.close();
													var42.game.setCurrentScreen(new ErrorScreen("Connection lost", (String) var7[0]));
												} else if (var6 == PacketType.UPDATE_PLAYER_TYPE) {
													var42.game.player.userType = (Byte) var7[0];
												}
											}
										}
									}
								}

								if (!var22.connected) {
									break;
								}

								var22.in.compact();
							}

							if (var22.out.position() > 0) {
								var22.out.flip();
								var22.channel.write(var22.out);
								var22.out.compact();
							}
						} catch (Exception var15) {
							var20.game.setCurrentScreen(new ErrorScreen("Disconnected!", "You've lost connection to the server"));
							var20.game.online = false;
							var15.printStackTrace();
							var20.netHandler.close();
							var20.game.networkManager = null;
						}
					}
				}

				Player var28 = game.player;
				var20 = game.networkManager;
				if (game.networkManager.levelLoaded) {
					int var24 = (int) (var28.x * 32.0F);
					var4 = (int) (var28.y * 32.0F);
					var40 = (int) (var28.z * 32.0F);
					var46 = (int) (var28.yRot * 256.0F / 360.0F) & 255;
					var45 = (int) (var28.xRot * 256.0F / 360.0F) & 255;
					var20.netHandler.send(PacketType.POSITION_ROTATION, Integer.valueOf(-1), Integer.valueOf(var24), Integer.valueOf(var4), Integer.valueOf(var40), Integer.valueOf(var46), Integer.valueOf(var45));
				}
			}
		}

		if (game.currentScreen == null && game.player != null && game.player.health <= 0) {
			game.setCurrentScreen(null);
		}

		if (game.currentScreen == null || game.currentScreen.grabsMouse) {
			int var25;
			while (Mouse.next()) {
				if ((var25 = Mouse.getEventDWheel()) != 0) {
					game.player.inventory.swapPaint(var25);
				}

				if (game.currentScreen == null) {
					if (!game.hasMouse && Mouse.getEventButtonState()) {
						game.grabMouse();
					} else {
						if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState()) {
							game.onMouseClick(0);
							lastClick = ticks;
						}

						if (Mouse.getEventButton() == 1 && Mouse.getEventButtonState()) {
							game.onMouseClick(1);
							lastClick = ticks;
						}

						if (Mouse.getEventButton() == 2 && Mouse.getEventButtonState() && game.selected != null) {
							if ((var16 = game.level.getTile(game.selected.x, game.selected.y, game.selected.z)) == Block.GRASS.id) {
								var16 = Block.DIRT.id;
							}

							if (var16 == Block.DOUBLE_SLAB.id) {
								var16 = Block.SLAB.id;
							}

							if (var16 == Block.BEDROCK.id) {
								var16 = Block.STONE.id;
							}

							game.player.inventory.grabTexture(var16, game.gamemode instanceof CreativeGameMode);
						}
					}
				}

				if (game.currentScreen != null) {
					game.currentScreen.mouseEvent();
				}
			}

			if (blockHitTime > 0) {
				--blockHitTime;
			}

			while (Keyboard.next()) {
				game.player.setKey(Keyboard.getEventKey(), Keyboard.getEventKeyState());
				if (Keyboard.getEventKeyState()) {
					if (game.currentScreen != null) {
						game.currentScreen.keyboardEvent();
					}

					if (game.currentScreen == null) {
						if (Keyboard.getEventKey() == 1) {
							game.pause();
						}

						if (game.gamemode instanceof CreativeGameMode) {
							if (Keyboard.getEventKey() == game.settings.loadLocationKey.key) {
								game.player.resetPos();
							}

							if (Keyboard.getEventKey() == game.settings.saveLocationKey.key) {
								game.level.setSpawnPos((int) game.player.x, (int) game.player.y, (int) game.player.z, game.player.yRot);
								game.player.resetPos();
							}
						}

						Keyboard.getEventKey();
						if (Keyboard.getEventKey() == 63) {
							game.raining = !game.raining;
						}

						if (Keyboard.getEventKey() == 15 && game.gamemode instanceof SurvivalGameMode && game.player.arrows > 0) {
							game.level.addEntity(new Arrow(game.level, game.player, game.player.x, game.player.y, game.player.z, game.player.yRot, game.player.xRot, 1.2F));
							--game.player.arrows;
						}

						if (Keyboard.getEventKey() == game.settings.buildKey.key) {
							game.gamemode.openInventory();
						}

						if (Keyboard.getEventKey() == game.settings.chatKey.key && game.networkManager != null && game.networkManager.isConnected()) {
							game.player.releaseAllKeys();
							game.setCurrentScreen(new ChatInputScreen());
						}
					}

					for (var25 = 0; var25 < 9; ++var25) {
						if (Keyboard.getEventKey() == var25 + 2) {
							game.player.inventory.selected = var25;
						}
					}

					if (Keyboard.getEventKey() == game.settings.toggleFogKey.key) {
						game.settings.toggleSetting(4, !Keyboard.isKeyDown(42) && !Keyboard.isKeyDown(54) ? 1 : -1);
					}
				}
			}

			if (game.currentScreen == null) {
				if (Mouse.isButtonDown(0) && (float) (ticks - lastClick) >= game.timer.tps / 4.0F && game.hasMouse) {
					game.onMouseClick(0);
					lastClick = ticks;
				}

				if (Mouse.isButtonDown(1) && (float) (ticks - lastClick) >= game.timer.tps / 4.0F && game.hasMouse) {
					game.onMouseClick(1);
					lastClick = ticks;
				}
			}

			boolean var26 = game.currentScreen == null && Mouse.isButtonDown(0) && game.hasMouse;
			if (!game.gamemode.instantBreak && blockHitTime <= 0) {
				if (var26 && game.selected != null && game.selected.entityPos == 0) {
					var4 = game.selected.x;
					var40 = game.selected.y;
					var46 = game.selected.z;
					game.gamemode.hitBlock(var4, var40, var46, game.selected.face);
				} else {
					game.gamemode.resetHits();
				}
			}
		}

		if (game.currentScreen != null) {
			lastClick = ticks + 10000;
		}

		if (game.currentScreen != null) {
			game.currentScreen.doInput();
			if (game.currentScreen != null) {
				game.currentScreen.tick();
			}
		}

		if (game.level != null) {
			Renderer var29 = game.renderer;
			++game.renderer.levelTicks;
			HeldBlock var41 = var29.heldBlock;
			var29.heldBlock.lastPos = var41.pos;
			if (var41.moving) {
				++var41.offset;
				if (var41.offset == 7) {
					var41.offset = 0;
					var41.moving = false;
				}
			}

			Player var27;
			var4 = var41.game.player.inventory.getSelected();
			Block var43 = null;
			if (var4 > 0) {
				var43 = Block.blocks[var4];
			}

			float var48 = 0.4F;
			float var50;
			if ((var50 = (var43 == var41.block ? 1.0F : 0.0F) - var41.pos) < -var48) {
				var50 = -var48;
			}

			if (var50 > var48) {
				var50 = var48;
			}

			var41.pos += var50;
			if (var41.pos < 0.1F) {
				var41.block = var43;
			}

			if (var29.game.raining) {
				var27 = var29.game.player;
				Level var32 = var29.game.level;
				var40 = (int) var27.x;
				var46 = (int) var27.y;
				var45 = (int) var27.z;

				for (var8 = 0; var8 < 50; ++var8) {
					int var60 = var40 + var29.random.nextInt(9) - 4;
					int var52 = var45 + var29.random.nextInt(9) - 4;
					int var57;
					if ((var57 = var32.getHighestTile(var60, var52)) <= var46 + 4 && var57 >= var46 - 4) {
						float var56 = var29.random.nextFloat();
						float var62 = var29.random.nextFloat();
						var29.game.particleManager.spawnParticle(new WaterDropParticle(var32, (float) var60 + var56, (float) var57 + 0.1F, (float) var52 + var62));
					}
				}
			}

			++game.levelRenderer.ticks;
			game.level.tickEntities();
			if (!game.isOnline()) {
				game.level.tick();
			}

			game.particleManager.tick();
		}

	}
}
