package com.lescomber.vestige.framework;

import com.lescomber.vestige.crossover.ColorRectManager;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.TextManager;
import com.lescomber.vestige.geometry.Hitbox;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.geometry.Rectangle;

public abstract class Screen {
	public static final int WIDTH = 800;
	public static final int HEIGHT = 480;

	public static final float MIDX = WIDTH / 2f;
	public static final float MIDY = HEIGHT / 2f;

	private static boolean isChanging = false;

	public static final float SCREEN_HITBOX_PADDING = 50;
	private static final Hitbox SCREEN_HITBOX = new Hitbox(new Rectangle(0, 0, WIDTH, HEIGHT));
	private static final Hitbox SCREEN_HITBOX_PADDED = new Hitbox(new Rectangle(-50, -50, WIDTH + SCREEN_HITBOX_PADDING, HEIGHT + SCREEN_HITBOX_PADDING));

	protected final AndroidGame game;

	public Screen(AndroidGame game) {
		this.game = game;
	}

	protected void prepScreenChange() {
		isChanging = true;
		SpriteManager.getInstance().startBuffer();
		TextManager.switchBuild();
		ColorRectManager.switchBuild();
		AudioManager.queueMode();
	}

	public static void notifyScreenChanged() {
		AudioManager.activateQueue();
		isChanging = false;
	}

	public static boolean isScreenChanging() {
		return isChanging;
	}

	public static boolean contains(Point p) {
		return SCREEN_HITBOX.contains(p);
	}

	public static boolean overlaps(Hitbox hitbox) {
		return overlaps(hitbox, false);
	}

	public static boolean overlaps(Hitbox hitbox, boolean padded) {
		return padded ? SCREEN_HITBOX_PADDED.overlaps(hitbox) : SCREEN_HITBOX.overlaps(hitbox);
	}

	public abstract void update(int deltaTime);

	public abstract void pause();

	public abstract void resume();

	public abstract void dispose();

	public abstract void backButton();
}