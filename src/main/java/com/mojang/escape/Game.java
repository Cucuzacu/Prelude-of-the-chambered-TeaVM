package com.mojang.escape;

import com.mojang.escape.entities.*;
import com.mojang.escape.level.Level;
import com.mojang.escape.level.block.LadderBlock;
import com.mojang.escape.menu.*;

public class Game {
	public int time;
	public Level level;
	public Player player;
	public int pauseTime;
	public Menu menu;

	public Game() {
		setMenu(new TitleMenu());
	}

	public void newGame() {
		Level.clear();
		level = Level.loadLevel(this, "start");

		player = new Player();
		player.level = level;
		level.player = player;
		player.x = level.xSpawn;
		player.z = level.ySpawn;
		level.addEntity(player);
		player.rot = Math.PI + 0.4;
	}

	public void switchLevel(String name, int id) {
		pauseTime = 30;
		level.removeEntityImmediately(player);
		level = Level.loadLevel(this, name);
		level.findSpawn(id);
		player.x = level.xSpawn;
		player.z = level.ySpawn;
		((LadderBlock) level.getBlock(level.xSpawn, level.ySpawn)).wait = true;
		player.x += Math.sin(player.rot) * 0.2;
		player.z += Math.cos(player.rot) * 0.2;
		level.addEntity(player);
	}

	public void tick(boolean[] keys) {
		if (pauseTime > 0) {
			pauseTime--;
			return;
		}

		time++;

		boolean strafe = keys[KeyEvent123.VK_CONTROL] || keys[KeyEvent123.VK_ALT] || keys[KeyEvent123.VK_ALT_GRAPH] || keys[KeyEvent123.VK_SHIFT];

		boolean lk = keys[KeyEvent123.VK_LEFT] || keys[KeyEvent123.VK_NUMPAD4];
		boolean rk = keys[KeyEvent123.VK_RIGHT] || keys[KeyEvent123.VK_NUMPAD6];

		boolean up = keys[KeyEvent123.VK_W] || keys[KeyEvent123.VK_UP] || keys[KeyEvent123.VK_NUMPAD8];
		boolean down = keys[KeyEvent123.VK_S] || keys[KeyEvent123.VK_DOWN] || keys[KeyEvent123.VK_NUMPAD2];
		boolean left = keys[KeyEvent123.VK_A] || (strafe && lk);
		boolean right = keys[KeyEvent123.VK_D] || (strafe && rk);

		boolean turnLeft = keys[KeyEvent123.VK_Q] || (!strafe && lk);
		boolean turnRight = keys[KeyEvent123.VK_E] || (!strafe && rk);

		boolean use = keys[KeyEvent123.VK_SPACE];

		for (int i = 0; i < 8; i++) {
			if (keys[KeyEvent123.VK_1 + i]) {
				keys[KeyEvent123.VK_1 + i] = false;
				player.selectedSlot = i;
				player.itemUseTime = 0;
			}
		}

		if (keys[KeyEvent123.VK_ESCAPE]) {
			keys[KeyEvent123.VK_ESCAPE] = false;
			if (menu == null) {
				setMenu(new PauseMenu());
			}
		}

		if (use) {
			keys[KeyEvent123.VK_SPACE] = false;
		}

		if (menu != null) {
			keys[KeyEvent123.VK_W] = keys[KeyEvent123.VK_UP] = keys[KeyEvent123.VK_NUMPAD8] = false;
			keys[KeyEvent123.VK_S] = keys[KeyEvent123.VK_DOWN] = keys[KeyEvent123.VK_NUMPAD2] = false;
			keys[KeyEvent123.VK_A] = false;
			keys[KeyEvent123.VK_D] = false;

			menu.tick(this, up, down, left, right, use);
		} else {
			player.tick(up, down, left, right, turnLeft, turnRight);
			if (use) {
				player.activate();
			}

			level.tick();
		}
	}

	public void getLoot(Item item) {
		player.addLoot(item);
	}

	public void win(Player player) {
		setMenu(new WinMenu(player));
	}

	public void setMenu(Menu menu) {
		this.menu = menu;
	}

	public void lose(Player player) {
		setMenu(new LoseMenu(player));
	}
}