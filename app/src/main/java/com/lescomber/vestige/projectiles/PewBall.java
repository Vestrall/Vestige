package com.lescomber.vestige.projectiles;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.geometry.Hitbox;
import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.map.Obstacle;
import com.lescomber.vestige.map.PewBallMap;
import com.lescomber.vestige.playerabilities.SOneChargeSwipe;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.units.PewBallGoalie;
import com.lescomber.vestige.units.PewBallPlayer;
import com.lescomber.vestige.units.Unit;

import java.util.ArrayList;

public class PewBall extends Projectile implements Comparable<PewBall> {
	public static final float RADIUS = 22;

	// Only tracks projectiles that pass through units that this PewBall has collided with. PewBall can only collide with each of these once
	private final ArrayList<Projectile> passThroughHits;

	private static final int BASE_VELOCITY = 200;
	private static final int VELOCITY_CHANGE_PER_LEVEL = 10;    // Acts as velocity reduction until KEY_LEVELS[0] and a bonus after KEY_LEVELS[1]
	private final float NORMAL_VELOCITY;    // Refers to base velocity + difficulty scaled velocity increase (but no speed boosts)

	private static final float[] SPEED_BOOST_GOALIE = new float[] { 0.7f, 1.2f };
	private static final float[] SPEED_BOOST_PLAYER = new float[] { 0.5f, 1 };
	private float boost;
	private final float BOOST_DECAY_PER_MS;

	public PewBall(int levelNum) {
		super(null, 0, RADIUS);

		// Calculate PewBall's non-boosted velocity for this levelNum
		final int velocityIncrease;
		if (levelNum < PewBallMap.KEY_LEVELS[0])
			velocityIncrease = (levelNum - PewBallMap.KEY_LEVELS[0]) * VELOCITY_CHANGE_PER_LEVEL;
		else if (levelNum > PewBallMap.KEY_LEVELS[1])
			velocityIncrease = (levelNum - PewBallMap.KEY_LEVELS[1]) * VELOCITY_CHANGE_PER_LEVEL;
		else
			velocityIncrease = 0;
		NORMAL_VELOCITY = BASE_VELOCITY + velocityIncrease;
		setVelocityPerSecond(NORMAL_VELOCITY);

		disableImageDrop();
		setArrivalRemoval(false);
		setUnitPassThrough(true);

		// Init pulsing animation
		final SpriteAnimation anim = new SpriteAnimation(SpriteManager.pewBall);
		anim.scale(2, 2);
		anim.setSequenceLimit(-1);
		setImage(anim);
		setGlow(SpriteManager.redGlow);

		lastHit = null;
		passThroughHits = new ArrayList<>(5);

		boost = 0;
		BOOST_DECAY_PER_MS = NORMAL_VELOCITY / 1200;
	}

	@Override
	public void update(int deltaTime) {
		super.update(deltaTime);

		// Decay speed boost (if one is active) and update velocity accordingly
		if (boost > 0) {
			boost -= BOOST_DECAY_PER_MS * deltaTime;
			if (boost <= 0)
				setVelocityPerSecond(NORMAL_VELOCITY);
			else
				setVelocityPerSecond(NORMAL_VELOCITY + boost);
		}

		// Projectile collision detection
		for (Projectile p : GameScreen.projectiles) {
			if (!p.equals(this) && overlaps(p)) {
				if (p.getUnitPassThrough() && !(p instanceof PewBall)) {
					if (!passThroughHits.contains(p)) {
						lastHit = p;
						passThroughHits.add(p);
						projectileHit(p);
					}
				} else {
					if (lastHit != p || p.lastHit != this) {
						lastHit = p;
						p.lastHit = this;
						projectileHit(p);
					}
				}
			}
		}
	}

	private void speedBoost(boolean fromGoalie) {
		final float boost;
		if (fromGoalie)
			boost = NORMAL_VELOCITY * (SPEED_BOOST_GOALIE[0] + (Util.rand.nextFloat() * (SPEED_BOOST_GOALIE[1] - SPEED_BOOST_GOALIE[0])));
		else
			boost = NORMAL_VELOCITY * (SPEED_BOOST_PLAYER[0] + (Util.rand.nextFloat() * (SPEED_BOOST_PLAYER[1] - SPEED_BOOST_PLAYER[0])));

		if (boost > this.boost) {
			this.boost = boost;
			setVelocityPerSecond(BASE_VELOCITY + boost);
		}
	}

	/**
	 * Handle collisions with goalies
	 */
	@Override
	protected void unitHit(Unit unit) {
		if (lastHit == unit || unit instanceof PewBallPlayer)
			return;

		lastHit = unit;

		//	             			   |-------Base angle-----| + |---------------------Random range----------------|
		final float newAngle = (float) ((Math.PI / 2) + (0.3f)) + (Util.rand.nextFloat() * ((float) Math.PI - 0.6f));
		final Line newPath = new Line(getX(), getY(), getX() + 1, getY());
		Point.rotate(newPath.point1, newAngle, newPath.point0.x, newPath.point0.y);
		setDestination(newPath.getExtEnd());

		// Add random, decaying burst of speed
		speedBoost(true);
	}

	private void projectileHit(Projectile p) {
		// If either projectile is finished, it must be off screen and its destination will be null, which means we can't change its trajectory here
		if (isFinished || p.isFinished)
			return;

		// Crash protection. TODO: Investigate if this is actually needed now that isFinished is checked above
		if (getDestination() == null || p.getDestination() == null)
			return;

		final Hitbox box = new Hitbox(getHitbox());
		final Line path = new Line(getCenter(), getDestination());
		final Hitbox pBox = new Hitbox(p.getHitbox());
		final Line pPath = new Line(p.getCenter(), p.getDestination());

		// Correct for overlap by backing up (a copy of) the faster projectile's hitbox along its path. We must back up the faster projectile
		//because otherwise we may get strange results when two projectiles are travelling in more or less the same direction. In such a case,
		//because the slower projectile is being overtaken by the faster one (from behind), it would be erroneous to back up the slower
		//projectile's hitbox until it was behind the faster projectile.
		if (getVelocity() > p.getVelocity()) {
			final float dx = path.point1.x - path.point0.x;
			final float dy = path.point1.y - path.point0.y;
			final float xPortion = dx / (Math.abs(dx) + Math.abs(dy));
			float yPortion = 1 - Math.abs(xPortion);
			if (dy < 0)
				yPortion = -yPortion;
			while (box.overlaps(pBox))
				box.offset(-xPortion, -yPortion);
		} else {
			final float dx = pPath.point1.x - pPath.point0.x;
			final float dy = pPath.point1.y - pPath.point0.y;
			final float xPortion = dx / (Math.abs(dx) + Math.abs(dy));
			float yPortion = 1 - Math.abs(xPortion);
			if (dy < 0)
				yPortion = -yPortion;
			while (box.overlaps(pBox))
				pBox.offset(-xPortion, -yPortion);
		}

		final Line centers = new Line(pBox.getCenter(), box.getCenter());
		setDestination(centers.getExtEnd());

		// Case: p is another PewBall
		if (p instanceof PewBall) {
			final Line reverseCenters = new Line(centers.point1, centers.point0);
			p.setDestination(reverseCenters.getExtEnd());
		}

		// Case: p is a player projectile that can deflect (i.e. does not pass through units)
		else if (!p.getUnitPassThrough()) {
			final int isRight = pPath.isPointRight(getX(), getY());
			float deflectAngle = 0;

			// Case: p is heading at least slightly south or is travelling exactly horizontally east
			if (pPath.point1.y > pPath.point0.y || ((pPath.point1.y == pPath.point0.y) && (pPath.point1.x < pPath.point0.x))) {
				if (isRight > 0)
					deflectAngle = (float) Math.PI / 2;
				else if (isRight < 0)
					deflectAngle = (float) -Math.PI / 2;
			} else {
				if (isRight > 0)
					deflectAngle = (float) -Math.PI / 2;
				else if (isRight < 0)
					deflectAngle = (float) Math.PI / 2;
			}

			Point.rotate(centers.point1, deflectAngle, centers.point0.x, centers.point0.y);
			p.rotateTo(centers.getDirection());
			p.setDestination(centers.getExtEnd());
		}

		// Case: SOneChargeSwipe. TODO: Refactor to be more reusable and to not break when there are other non-deflecting projectiles
		else //if (p instanceof SOneChargeSwipeProjectile)
			speedBoost(false);
	}

	/**
	 * Handle bouncing off walls
	 */
	@Override
	protected void obstacleHit(Obstacle o) {
		lastHit = o;

		final Line path = new Line(getCenter(), getDestination());
		final float rotationAngle = -2 * path.getDirection();

		Point.rotate(path.point1, rotationAngle, path.point0.x, path.point0.y);
		path.point1 = path.getExtEnd();

		// Move the ball slightly so it is no longer overlapping o to prevent repeatedly hitting the same obstacle
		final float dx = path.point1.x - path.point0.x;
		final float dy = path.point1.y - path.point0.y;
		final float xPortion = dx / (Math.abs(dx) + Math.abs(dy));
		float yPortion = 1 - Math.abs(xPortion);
		if (dy < 0)
			yPortion = -yPortion;
		while (o.overlaps(this))
			offset(xPortion, yPortion);

		setDestination(path.point1);
	}

	private float getGoalieX() {
		return getX() > PewBallGoalie.RIGHT_EDGE + RADIUS ? Integer.MIN_VALUE : getX();
	}

	@Override
	public int compareTo(PewBall another) {
		return Math.round(another.getGoalieX()) - Math.round(getGoalieX());
	}
}