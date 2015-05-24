package com.lescomber.vestige.projectiles.glows;

import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;
import com.lescomber.vestige.geometry.Hitbox;

public class OvalGlow extends Glow {
	public OvalGlow(SpriteTemplate template) {
		super(template);
	}

	public OvalGlow(OvalGlow copyMe) {
		super(copyMe);
	}

	@Override
	public void updateShape(Hitbox hitbox, float sizePercentage) {
		scaleWidthTo(sizePercentage * (hitbox.getRight() - hitbox.getLeft()));
		scaleHeightTo(sizePercentage * getSprite().getTemplate().getHeight());
		offsetTo(hitbox.getX(), hitbox.getBottom());
	}

	@Override
	public OvalGlow copy() {
		return new OvalGlow(this);
	}
}