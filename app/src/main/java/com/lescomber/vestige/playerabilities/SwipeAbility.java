package com.lescomber.vestige.playerabilities;

import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.units.Player;

public abstract class SwipeAbility extends PlayerAbility {
	public SwipeAbility(Player player) {
		super(player);
	}

	public SwipeAbility(SwipeAbility copyMe) {
		super(copyMe);

		player = copyMe.player;
	}

	public abstract Projectile prepare(Line swipe, float firingOffsetX);

	@Override
	public abstract SwipeAbility copy();
}