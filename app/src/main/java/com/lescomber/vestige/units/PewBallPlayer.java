package com.lescomber.vestige.units;

import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.playerabilities.PewBallDoubleTap;
import com.lescomber.vestige.projectiles.PewBall;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.screens.GameScreen;

public class PewBallPlayer extends Player {
	private static final float SHIELD_RADIUS_SQUARED = 48 * 48;

	public PewBallPlayer() {
		super();

		// Remember CDIndicator position for current doubleTapAbility
		final Point pos = doubleTapAbility.getCDIndicator().getCenter();

		// Create PewBall version of doubleTapAbility
		doubleTapAbility = new PewBallDoubleTap(this);

		// Put doubleTapAbility's CDIndicator back in its place
		doubleTapAbility.offsetCDIndicatorTo(pos.x, pos.y);
	}

	/**
	 * Send ball directly away from the player's hitbox center
	 */
	public void repulse(PewBall ball) {
		final Line centers = new Line(getCenter(), ball.getCenter());
		ball.setDestination(centers.getExtEnd(PewBall.RADIUS, PewBall.RADIUS));
	}

	@Override
	public void update(int deltaTime) {
		super.update(deltaTime);

		// Check for and handle shield-based repulsion. Note: uses image centers instead of hitbox centers for distance check
		if (getShields() > 0) {
			final Point iCenter = getImageCenter();
			for (Projectile p : GameScreen.projectiles) {
				if (p instanceof PewBall && iCenter.distanceToPointSquared(p.getImageCenter()) < SHIELD_RADIUS_SQUARED + PewBall.RADIUS)
					repulse((PewBall) p);
			}
		}
	}
}