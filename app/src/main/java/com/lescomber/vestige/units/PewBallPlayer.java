package com.lescomber.vestige.units;

import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.playerabilities.PewBallDoubleTap;
import com.lescomber.vestige.projectiles.PewBall;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.screens.GameScreen;

public class PewBallPlayer extends Player
{
	private static final float SHIELD_RADIUS_SQUARED = 48 * 48;

	public PewBallPlayer()
	{
		super();

		// Remember CDIndicator position for current doubleTapAbility
		Point pos = doubleTapAbility.getCDIndicator().getCenter();

		// Create PewBall version of doubleTapAbility
		doubleTapAbility = new PewBallDoubleTap(this);

		// Put doubleTapAbility's CDIndicator back in its place
		doubleTapAbility.offsetCDIndicatorTo(pos.x, pos.y);
	}

	public void repulse(PewBall ball)
	{
		Line centers = new Line(getCenter(), ball.getCenter());

		// Offset ball until it is outside the shield. TODO: Offset ball left or right (if needed) to avoid walls
		/*Hitbox pBox = new Hitbox(ball.getHitbox());
		float dx = centers.point1.x - centers.point0.x;
		float dy = centers.point1.y - centers.point0.y;
		float xPortion = dx / (Math.abs(dx) + Math.abs(dy));
		float yPortion = 1 - Math.abs(xPortion);
		if (dy < 0)
			yPortion = -yPortion;
		while (overlaps(pBox))
			pBox.offset(-xPortion, -yPortion);

		ball.offsetTo(pBox.getX(), pBox.getY());*/

		ball.setDestination(centers.getExtEnd(PewBall.RADIUS, PewBall.RADIUS));
	}

	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);

		// Check for and handle shield-based repulsion
		if (getShields() > 0)
		{
			final Point iCenter = getImageCenter();
			for (Projectile p : GameScreen.projectiles)
			{
				if (p instanceof PewBall && iCenter.distanceToPointSquared(p.getImageCenter()) < SHIELD_RADIUS_SQUARED + PewBall.RADIUS)
					repulse((PewBall)p);
			}
		}
	}
}