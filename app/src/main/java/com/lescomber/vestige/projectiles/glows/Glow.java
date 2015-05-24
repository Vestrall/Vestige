package com.lescomber.vestige.projectiles.glows;

import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;
import com.lescomber.vestige.geometry.Hitbox;
import com.lescomber.vestige.graphics.Sprite;

public abstract class Glow extends Sprite {
	public Glow(SpriteTemplate template) {
		super(template);
	}

	public Glow(Glow copyMe) {
		super(copyMe);
	}

	public abstract void updateShape(Hitbox hitbox, float sizePercentage);

	@Override
	public abstract Glow copy();
}