package com.lescomber.vestige.projectiles.glows;

import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.geometry.Hitbox;

public class RotatedRectGlow extends Glow {
	public RotatedRectGlow(SpriteTemplate template) {
		super(template);
	}

	public RotatedRectGlow(RotatedRectGlow copyMe) {
		super(copyMe);
	}

	/**
	 * // Note: disregards sizePercentage
	 */
	@Override
	public void updateShape(Hitbox hitbox, float sizePercentage) {
		scaleTo(hitbox.getWidth(), hitbox.getHeight());
		if (!Util.equals(getDirection(), hitbox.getDirection()))
			rotateTo(hitbox.getDirection());
		offsetTo(hitbox.getX(), hitbox.getY() + (0.4f * hitbox.getHeight()));
	}

	@Override
	public RotatedRectGlow copy() {
		return new RotatedRectGlow(this);
	}
}