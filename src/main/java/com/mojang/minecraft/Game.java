package com.mojang.minecraft;

import com.mojang.minecraft.gamemode.CreativeGameMode;
import com.mojang.minecraft.gamemode.GameMode;
import com.mojang.minecraft.gui.*;
import com.mojang.minecraft.entities.Item;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.LevelIO;
import com.mojang.minecraft.level.generator.LevelGenerator;
import com.mojang.minecraft.level.blocks.Block;
import com.mojang.minecraft.entities.mob.Mob;
import com.mojang.minecraft.model.HumanoidModel;
import com.mojang.minecraft.model.ModelManager;
import com.mojang.minecraft.net.NetworkManager;
import com.mojang.minecraft.entities.particle.ParticleManager;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.player.InputHandlerImpl;
import com.mojang.minecraft.player.Player;
import com.mojang.minecraft.render.Renderer;
import com.mojang.minecraft.render.*;
import com.mojang.minecraft.render.texture.TextureLavaFX;
import com.mojang.minecraft.render.texture.TextureWaterFX;
import com.mojang.minecraft.sound.SoundManager;
import com.mojang.minecraft.sound.SoundPlayer;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.IntBuffer;

public final class Game implements Runnable {

	public GameMode gamemode = new CreativeGameMode(this);
	public int width;
	public int height;
	public Level level;
	public LevelRenderer levelRenderer;
	public Player player;
	public ParticleManager particleManager;
	public SessionData session = null;
	public String host;
	public Canvas canvas;
	public boolean levelLoaded = false;
	public TextureManager textureManager;
	public FontRenderer fontRenderer;
	public GuiScreen currentScreen = null;
	public ProgressBarDisplay progressBar = new ProgressBarDisplay(this);
	public Renderer renderer = new Renderer(this);
	public LevelIO levelIo;
	public SoundManager sound;
	public String levelName;
	public int levelId;
	public Robot robot;
	public HUDScreen hud;
	public boolean online;
	public NetworkManager networkManager;
	public SoundPlayer soundPlayer;
	public MovingObjectPosition selected;
	public GameSettings settings;
	public String debug;
	public boolean hasMouse;
	public boolean raining;
	public MainGameLoop loop;
	String server;
	int port;
	private boolean fullscreen = false;
	public final Timer timer = new Timer(20.0F);
	private Cursor cursor;
	private ResourceDownloadThread resourceThread;
	private final MinecraftApplet applet;


	public Game(Canvas canvas, MinecraftApplet applet, int width, int height, boolean fullscreen) {
		levelIo = new LevelIO(progressBar);
		sound = new SoundManager();
		levelName = null;
		levelId = 0;
		online = false;
		new HumanoidModel(0.0F);
		selected = null;
		server = null;
		port = 0;
		debug = "";
		hasMouse = false;
		raining = false;

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.applet = applet;
		new SleepForeverThread(this);
		this.canvas = canvas;
		this.width = width;
		this.height = height;
		this.fullscreen = fullscreen;
		if (canvas != null) {
			try {
				this.robot = new Robot();
				return;
			} catch (AWTException var8) {
				var8.printStackTrace();
			}
		}
	}

	static void checkGLError(String message) {
		int error = GL11.glGetError();
		if (error != 0) {
			String var2 = GLU.gluErrorString(error);
			System.out.println("########## GL ERROR ##########");
			System.out.println("@ " + message);
			System.out.println(error + ": " + var2);
			System.exit(0);
		}

	}

	public final void setCurrentScreen(GuiScreen screen) {
       this.currentScreen = screen;
		if (!(currentScreen instanceof ErrorScreen)) {
			if (currentScreen != null) {
				currentScreen.onClose();
			}

			if (screen == null && player.health <= 0) {
				screen = new GameOverScreen();
			}
			
			if (screen != null) {
				if (hasMouse) {
					player.releaseAllKeys();
					hasMouse = false;
					if (levelLoaded) {
						try {
							Mouse.setNativeCursor(null);
						} catch (LWJGLException var4) {
							var4.printStackTrace();
						}
					} else {
						Mouse.setGrabbed(false);
					}
				}

				int windowWidth = width * 240 / height;
				int windowHeight = 240;
				screen.open(this, windowWidth, windowHeight);
				online = false;
			} else {
				grabMouse();
			}
		}
	}

	public final void shutdown() {
		try {
			if (soundPlayer != null) {
				SoundPlayer var1 = soundPlayer;
				soundPlayer.running = false;
			}

			if (resourceThread != null) {
				ResourceDownloadThread var4 = resourceThread;
				resourceThread.running = true;
			}
		} catch (Exception ignored) {
		}

		if (!levelLoaded) {
			try {
				LevelIO.save(level, new FileOutputStream(new File("level.dat")));
			} catch (Exception var2) {
				var2.printStackTrace();
			}
		}

		Mouse.destroy();
		Keyboard.destroy();
		Display.destroy();
	}

	public final void run() {
		MainGameLoop.renderer = renderer;
		MainGameLoop.timer = timer;
		MainGameLoop.game = this;
		MainGameLoop.running = true;

		try {
			if (canvas != null) {
				Display.setParent(canvas);
			} else if (fullscreen) {
				Display.setFullscreen(true);
				width = Display.getDisplayMode().getWidth();
				height = Display.getDisplayMode().getHeight();
			} else {
				Display.setDisplayMode(new DisplayMode(width, height));
			}

			Display.setTitle("Minecraft 0.30");

			try {
				Display.create();
			} catch (LWJGLException var57) {
				var57.printStackTrace();

				try {
					Thread.sleep(1000L);
				} catch (InterruptedException var56) {
                }

				Display.create();
			}

			Keyboard.create();
			Mouse.create();

			try {
				Controllers.create();
			} catch (Exception var55) {
				var55.printStackTrace();
			}

			checkGLError("Pre startup");
			GL11.glEnable(3553);
			GL11.glShadeModel(7425);
			GL11.glClearDepth(1.0D);
			GL11.glEnable(2929);
			GL11.glDepthFunc(515);
			GL11.glEnable(3008);
			GL11.glAlphaFunc(516, 0.0F);
			GL11.glCullFace(1029);
			GL11.glMatrixMode(5889);
			GL11.glLoadIdentity();
			GL11.glMatrixMode(5888);
			checkGLError("Startup");
			String appName = "minecraftclassic";
			String homeDir = System.getProperty("user.home", ".");
			String osName = System.getProperty("os.name").toLowerCase();
			File appDir;
			switch (OS.GetOperatingSystemAsInt(osName)) {
				case 1, 2 -> appDir = new File(homeDir, '.' + appName + '/');
				case 3 -> {
					String env = System.getenv("APPDATA");
					if (env != null) {
						appDir = new File(env, "." + appName + '/');
					} else {
						appDir = new File(homeDir, '.' + appName + '/');
					}
				}
				case 4 -> appDir = new File(homeDir, "Library/Application Support/" + appName);
				default -> appDir = new File(homeDir, appName + '/');
			}

			if (!appDir.exists() && !appDir.mkdirs()) {
				throw new RuntimeException("The working directory could not be created: " + appDir);
			}

			File var2 = appDir;
			settings = new GameSettings(this, appDir);
			textureManager = new TextureManager(settings);
			textureManager.registerAnimation(new TextureLavaFX());
			textureManager.registerAnimation(new TextureWaterFX());
			fontRenderer = new FontRenderer(settings, "/default.png", textureManager);
			IntBuffer var9;
			(var9 = BufferUtils.createIntBuffer(256)).clear().limit(256);
			levelRenderer = new LevelRenderer(this, textureManager);
			Item.initModels();
			Mob.modelCache = new ModelManager();
			GL11.glViewport(0, 0, width, height);
			if (server != null && session != null) {
				Level level = new Level();
				level.setData(8, 8, 8, new byte[512]);
				setLevel(level);
			} else {

				try {
					if (levelName != null) {
						loadOnlineLevel(levelName, levelId);
					} else if (!levelLoaded) {
						Level levelSave = levelIo.load(new FileInputStream(new File(appDir, "level.dat")));
						if (levelSave != null) {
							setLevel(levelSave);
						}
					}
				} catch (Exception ignored) { }

				if (level == null) {
					generateLevel(1);
				}
			}

			particleManager = new ParticleManager(level, textureManager);
			if (levelLoaded) {
				try {
					cursor = new Cursor(16, 16, 0, 0, 1, var9, null);
				} catch (LWJGLException e) {
					e.printStackTrace();
				}
			}

			try {
				soundPlayer = new SoundPlayer(settings);

				try {
					AudioFormat format = new AudioFormat(44100.0F, 16, 2, true, true);
					soundPlayer.dataLine = AudioSystem.getSourceDataLine(format);
					soundPlayer.dataLine.open(format, 4410);
					soundPlayer.dataLine.start();
					soundPlayer.running = true;
					Thread soundThread = new Thread(soundPlayer);
					soundThread.setDaemon(true);
					soundThread.setPriority(10);
					soundThread.start();
				} catch (Exception e) {
					e.printStackTrace();
					soundPlayer.running = false;
				}

				resourceThread = new ResourceDownloadThread(var2, this);
				resourceThread.start();
			} catch (Exception ignored) {}

			checkGLError("Post startup");
			hud = new HUDScreen(this, width, height);
			(new SkinDownloadThread(this)).start();
			if (server != null && session != null) {
				networkManager = new NetworkManager(this, server, port, this.session.username, this.session.mppass);
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.toString(), "Failed to start Minecraft", JOptionPane.ERROR_MESSAGE);
			return;
		}

		loop.loop();
	}

	public final void grabMouse() {
		if (!this.hasMouse) {
			this.hasMouse = true;
			if (this.levelLoaded) {
				try {
					Mouse.setNativeCursor(this.cursor);
					Mouse.setCursorPosition(this.width / 2, this.height / 2);
				} catch (LWJGLException var2) {
					var2.printStackTrace();
				}

				if (this.canvas == null) {
					this.canvas.requestFocus();
				}
			} else {
				Mouse.setGrabbed(true);
			}

			this.setCurrentScreen(null);
			MainGameLoop.lastClick = MainGameLoop.ticks + 10000;
		}
	}

	public final void pause() {
		if (this.currentScreen == null) {
			this.setCurrentScreen(new PauseScreen());
		}
	}

	void onMouseClick(int var1) {
		if (var1 != 0 || MainGameLoop.blockHitTime <= 0) {
			HeldBlock heldBlock;
			if (var1 == 0) {
				heldBlock = renderer.heldBlock;
				renderer.heldBlock.offset = -1;
				heldBlock.moving = true;
			}

			int var3;
			if (var1 == 1 && (var3 = player.inventory.getSelected()) > 0 && gamemode.useItem(player, var3)) {
				heldBlock = renderer.heldBlock;
				this.renderer.heldBlock.pos = 0.0F;
			} else if (selected == null) {
				if (var1 == 0 && !(gamemode instanceof CreativeGameMode)) {
					MainGameLoop.blockHitTime = 10;
				}

			} else {
				if (selected.entityPos == 1) {
					if (var1 == 0) {
						selected.entity.hurt(player, 4);
						return;
					}
				} else if (selected.entityPos == 0) {
					var3 = selected.x;
					int var4 = selected.y;
					int var5 = selected.z;
					if (var1 != 0) {
						if (selected.face == 0) {
							--var4;
						}

						if (this.selected.face == 1) {
							++var4;
						}

						if (this.selected.face == 2) {
							--var5;
						}

						if (this.selected.face == 3) {
							++var5;
						}

						if (this.selected.face == 4) {
							--var3;
						}

						if (this.selected.face == 5) {
							++var3;
						}
					}

					Block var6 = Block.blocks[this.level.getTile(var3, var4, var5)];
					if (var1 == 0) {
						if (var6 != Block.BEDROCK || this.player.userType >= 100) {
							this.gamemode.hitBlock(var3, var4, var5);
							return;
						}
					} else {
						int var10;
						if ((var10 = this.player.inventory.getSelected()) <= 0) {
							return;
						}

						Block var8;
						AABB var9;
						if (((var8 = Block.blocks[this.level.getTile(var3, var4, var5)]) == null || var8 == Block.WATER || var8 == Block.STATIONARY_WATER || var8 == Block.LAVA || var8 == Block.STATIONARY_LAVA) && ((var9 = Block.blocks[var10].getCollisionBox(var3, var4, var5)) == null || (!this.player.bb.intersects(var9) && this.level.isFree(var9)))) {
							if (!this.gamemode.canPlace(var10)) {
								return;
							}

							if (this.isOnline()) {
								this.networkManager.sendBlockChange(var3, var4, var5, var1, var10);
							}

							this.level.netSetTile(var3, var4, var5, var10);
							heldBlock = this.renderer.heldBlock;
							this.renderer.heldBlock.pos = 0.0F;
							Block.blocks[var10].onPlace(this.level, var3, var4, var5);
						}
					}
				}
			}
		}
	}

	public final boolean isOnline() {
		return this.networkManager != null;
	}

	public final void generateLevel(int var1) {
		String var2 = this.session != null ? this.session.username : "anonymous";
		Level var4 = (new LevelGenerator(this.progressBar)).generate(var2, 128 << var1, 128 << var1, 64);
		this.gamemode.prepareLevel(var4);
		this.setLevel(var4);
	}

	public final boolean loadOnlineLevel(String levelName, int id) {
		Level level = levelIo.loadOnline(host, levelName, id);
		if (level == null) {
			return false;
		} else {
			this.setLevel(level);
			return true;
		}
	}

	public final void setLevel(Level level) {
		if (this.applet == null || !this.applet.getDocumentBase().getHost().equalsIgnoreCase("minecraft.net") && !this.applet.getDocumentBase().getHost().equalsIgnoreCase("www.minecraft.net") || !this.applet.getCodeBase().getHost().equalsIgnoreCase("minecraft.net") && !this.applet.getCodeBase().getHost().equalsIgnoreCase("www.minecraft.net")) {
			level = null;
		}

		this.level = level;
		if (level != null) {
			level.initTransient();
			this.gamemode.apply(level);
			level.font = this.fontRenderer;
			level.rendererContext$5cd64a7f = this;
			if (!this.isOnline()) {
				this.player = (Player) level.findSubclassOf(Player.class);
			} else if (this.player != null) {
				this.player.resetPos();
				this.gamemode.preparePlayer(this.player);
				level.player = this.player;
				level.addEntity(this.player);
			}
		}

		if (this.player == null) {
			this.player = new Player(level);
			this.player.resetPos();
			this.gamemode.preparePlayer(this.player);
			if (level != null) {
				level.player = this.player;
			}
		}

		if (this.player != null) {
			this.player.input = new InputHandlerImpl(this.settings);
			this.gamemode.apply(this.player);
		}

		if (this.levelRenderer != null) {
			LevelRenderer var3 = this.levelRenderer;
			if (this.levelRenderer.level != null) {
				var3.level.removeListener(var3);
			}

			var3.level = level;
			if (level != null) {
				level.addListener(var3);
				var3.refresh();
			}
		}

		if (this.particleManager != null) {
			ParticleManager var5 = this.particleManager;
			if (level != null) {
				level.particleEngine = var5;
			}

			for (int var4 = 0; var4 < 2; ++var4) {
				var5.particles[var4].clear();
			}
		}

		System.gc();
	}
}
