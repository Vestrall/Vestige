package com.lescomber.vestige.projectiles;

import com.lescomber.vestige.Entity;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.statuseffects.HitBundle;
import com.lescomber.vestige.units.Unit;

import java.util.ArrayList;

public class Explosion extends Entity {
	// Note: Update these numbers if the images or image sizes change!
	private static final float DEFAULT_IMAGE_RADIUS = 48;
	private static final float RADIUS_TO_INIT_HITBOX_FACTOR = 9 / DEFAULT_IMAGE_RADIUS;

	private final float FINAL_DIAMETER;
	private final float DIAMETER_PER_MS;

	private final HitBundle hitBundle;
	private final ArrayList<Integer> targets;
	private final ArrayList<Unit> unitsHit;

	private int duration;
	private boolean isFinished;

	public Explosion(float x, float y, float radius, float damage) {
		super();

		offsetTo(x, y);

		createCircleHitbox(radius * RADIUS_TO_INIT_HITBOX_FACTOR);

		final SpriteAnimation anim = new SpriteAnimation();
		anim.addFrames(SpriteManager.explosion);
		final double scaleFactor = radius / DEFAULT_IMAGE_RADIUS;
		anim.scale(scaleFactor, scaleFactor);
		setImage(anim);

		// Only deal damage during the first 9 frames of the explosion (i.e. not the last 4 frames)
		duration = 9 * anim.getFrameTime();

		FINAL_DIAMETER = radius * 2;                    // Magic number 4 = number of frames during which the explosion is expanding
		DIAMETER_PER_MS = (FINAL_DIAMETER - hitbox.getWidth()) / (4 * anim.getFrameTime());

		hitBundle = new HitBundle(damage);
		hitBundle.setAbsorbSound(false);
		targets = new ArrayList<>(2);
		targets.add(GameScreen.gregs);
		unitsHit = new ArrayList<>(4);
		isFinished = false;
	}

	public Explosion(Point position, float radius, float damage) {
		this(position.x, position.y, radius, damage);
	}

	public Explosion(float radius, float damage) {
		this(0, 0, radius, damage);
	}

	public Explosion(Explosion copyMe) {
		super(copyMe);

		FINAL_DIAMETER = copyMe.FINAL_DIAMETER;
		DIAMETER_PER_MS = copyMe.DIAMETER_PER_MS;
		hitBundle = new HitBundle(copyMe.hitBundle);
		targets = new ArrayList<>(2);
		targets.addAll(copyMe.targets);
		unitsHit = new ArrayList<>(4);
		unitsHit.addAll(copyMe.unitsHit);
		duration = copyMe.duration;
		isFinished = copyMe.isFinished;
	}

	@Override
	public void update(int deltaTime) {
		super.update(deltaTime);

		// Expand hitbox if image is still expanding
		float width = hitbox.getWidth();
		if (width < FINAL_DIAMETER) {
			width += DIAMETER_PER_MS * deltaTime;
			width = Math.min(width, FINAL_DIAMETER);
			hitbox.scaleTo(width, width);
		}

		// Check hitboxes and hit things (only during first 10 frames)
		duration -= deltaTime;
		if (duration > 0) {
			for (final Integer i : targets) {
				for (final Unit u : GameScreen.units[i]) {
					if (overlaps(u)) {
						if (!unitsHit.contains(u)) {
							u.hit(hitBundle);
							unitsHit.add(u);
						}
					}
				}
			}
		}
	}

	@Override
	protected void animationFinished(int animID) {
		isFinished = true;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public Explosion copy() {
		return new Explosion(this);
	}
}