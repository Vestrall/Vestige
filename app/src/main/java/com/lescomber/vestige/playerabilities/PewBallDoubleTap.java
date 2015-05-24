package com.lescomber.vestige.playerabilities;

import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.projectiles.PewBall;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.units.PewBallPlayer;

public class PewBallDoubleTap extends SOneDoubleTap {
	public PewBallDoubleTap(PewBallPlayer player) {
		super(player);
	}

	public PewBallDoubleTap(PewBallDoubleTap copyMe) {
		super(copyMe);
	}

	@Override
	public void fire(Point p) {
		super.fire(p);

		double minDistanceSquared = RANGE_SQUARED;
		PewBall target = null;

		// Find nearest
		for (final Projectile projectile : GameScreen.projectiles) {
			if (projectile instanceof PewBall) {
				final double d2 = p.distanceToPointSquared(projectile.getCenter());
				if (d2 < minDistanceSquared) {
					minDistanceSquared = d2;
					target = (PewBall) projectile;
				}
			}
		}

		// Fire projectile
		if (target != null) {
			final Line path = new Line(player.getImageCenter(), target.getImageCenter());
			activeLasers.add(new SOneDoubleTapLaser(path));
			((PewBallPlayer) player).repulse(target);
		}
	}

	@Override
	public PewBallDoubleTap copy() {
		return new PewBallDoubleTap(this);
	}
}
