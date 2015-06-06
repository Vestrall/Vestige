package com.lescomber.vestige.projectiles.sone;

import com.lescomber.vestige.Options;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.AudioManager;
import com.lescomber.vestige.geometry.Angle;
import com.lescomber.vestige.geometry.Circle;
import com.lescomber.vestige.geometry.Hitbox;
import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.projectiles.Explosion;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.units.Unit;

public class Seeker extends Projectile {
	private static final float RADIANS_PER_MS[] = new float[] { 0.00070f, 0.00085f, 0.00110f };
	private static final float EXPLOSION_RADIUS = 60;
	private static final float DAMAGE[] = new float[] { 7, 9, 11 };

	private final Hitbox glowHitbox;    // Pass this hitbox to glow instead of our enlarged hitbox

	public Seeker() {
		super(null, 0, EXPLOSION_RADIUS * 0.4f);    // Increased hitbox size allows Seeker to explode when it gets near enough to the target

		setVelocityPerSecond(275);        // Reduce velocity a little (default is 300)

		disableImageDrop();

		glowHitbox = new Hitbox(new Circle(0, 0, 8));

		final SpriteAnimation anim = new SpriteAnimation(SpriteManager.plasmaBall);
		anim.setSequenceLimit(-1);
		setImage(anim);
		setGlow(SpriteManager.purpleGlow);

		setExplosion(new Explosion(EXPLOSION_RADIUS, DAMAGE[Options.difficulty]));
		setExplosionSound(AudioManager.purpleExplosion);
	}

	public Seeker(Seeker copyMe) {
		super(copyMe);

		glowHitbox = new Hitbox(copyMe.glowHitbox);
	}

	@Override
	public void offset(float dx, float dy) {
		super.offset(dx, dy);

		glowHitbox.offset(dx, dy);
	}

	@Override
	public void move(float dx, float dy) {
		super.move(dx, dy);

		glowHitbox.offset(dx, dy);
	}

	@Override
	protected void updateGlow(Hitbox hitbox) {
		super.updateGlow(glowHitbox);
	}

	@Override
	public void update(int deltaTime) {
		super.update(deltaTime);

		// Seek
		final Unit target = getNearestTarget();

		if (target == null)
			return;

		final float maxRotation = RADIANS_PER_MS[Options.difficulty] * deltaTime;
		final Line line = new Line(getCenter(), target.getCenter());
		final float targetAngle = line.getDirection();
		final Line curAngleLine = new Line(getCenter(), getDestination());
		final float curAngle = curAngleLine.getDirection();

		final Point dest = new Point(getDestination());

		if (Angle.isInRange(targetAngle, curAngle + maxRotation, curAngle + (float) Math.PI))
			Point.rotate(dest, maxRotation, line.point0.x, line.point0.y);
		else if (Angle.isInRange(targetAngle, curAngle - (float) Math.PI, curAngle - maxRotation))
			Point.rotate(dest, -maxRotation, line.point0.x, line.point0.y);
		else
			Point.rotate(dest, targetAngle - curAngle, line.point0.x, line.point0.y);

		setDestination(dest);
	}

	private Unit getNearestTarget() {
		double minDistanceSquared = Double.MAX_VALUE;
		Unit target = null;
		for (final int i : getTargets()) {
			for (final Unit u : GameScreen.units[i]) {
				final double d2 = getCenter().distanceToPointSquared(u.getCenter());
				if (d2 < minDistanceSquared) {
					minDistanceSquared = d2;
					target = u;
				}
			}
		}

		return target;
	}

	@Override
	public Seeker copy() {
		return new Seeker(this);
	}
}