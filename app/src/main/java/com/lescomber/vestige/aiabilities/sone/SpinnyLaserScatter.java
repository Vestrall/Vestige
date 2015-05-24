package com.lescomber.vestige.aiabilities.sone;

import com.lescomber.vestige.aiabilities.AIAbility;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.projectiles.sone.SpinnyLaser;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.units.AIUnit;

public class SpinnyLaserScatter extends AIAbility {
	private static final int[] LASER_COUNT = new int[] { 2, 3, 5 };
	private static final int LASER_DELAY = 2500;    // Total delay (in ms) from when projectile is spawned (not from when it arrives)

	public SpinnyLaserScatter(AIUnit owner, double cooldownSeconds) {
		super(owner, cooldownSeconds);
	}

	public SpinnyLaserScatter(SpinnyLaserScatter copyMe) {
		super(copyMe);
	}

	@Override
	public void activate() {
		final Point firingLocation = owner.getFiringLocation();
		Point dest;

		for (int i = 0; i < LASER_COUNT[OptionsScreen.difficulty]; i++) {
			dest = GameScreen.map.adjustDestination(getRandomLocation());

			final SpinnyLaser laser = new SpinnyLaser(firingLocation.x, firingLocation.y, dest.x, dest.y, owner, LASER_DELAY);
			owner.queueProjectile(laser);
		}
	}

	@Override
	public SpinnyLaserScatter copy() {
		return new SpinnyLaserScatter(this);
	}
}