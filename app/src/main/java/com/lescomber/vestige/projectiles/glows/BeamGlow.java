package com.lescomber.vestige.projectiles.glows;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.geometry.Hitbox;

public class BeamGlow extends RotatedRectGlow {
	private static final float WIDTH_SCALE = 1.2f;

	public BeamGlow() {
		super(SpriteManager.rectangleRedGlow);
	}

	public BeamGlow(BeamGlow copyMe) {
		super(copyMe);
	}

	@Override
	public void updateShape(Hitbox hitbox, float sizePercentage) {
		scaleTo(hitbox.getWidth() * WIDTH_SCALE, hitbox.getHeight());
		if (!Util.equals(getDirection(), hitbox.getDirection()))
			rotateTo(hitbox.getDirection());
		offsetTo(hitbox.getX(), hitbox.getY() + (0.4f * hitbox.getHeight()));
	}

	@Override
	public BeamGlow copy() {
		return new BeamGlow(this);
	}
}