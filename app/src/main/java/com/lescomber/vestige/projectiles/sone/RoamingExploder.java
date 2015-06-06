package com.lescomber.vestige.projectiles.sone;

import com.lescomber.vestige.Options;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.AudioManager;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.projectiles.Explosion;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.units.AIUnit;

public class RoamingExploder extends Projectile {
	private static final float DESTINATION_BORDER = 20;
	private static final int VELOCITY[] = new int[] { 145, 155, 165 };
	private static final float DAMAGE[] = new float[] { 5, 8, 11 };
	private static final float RADIUS = 50;

	private static final int MAX_DURATION = 22000;
	private int duration;

	private static final int INTERVAL = 1000;
	private int countdown;

	private final AIUnit owner;

	public RoamingExploder(AIUnit owner, float x, float y) {
		super(null, 0, 0);

		final SpriteAnimation anim = new SpriteAnimation(SpriteManager.plasmaBall);
		anim.setSequenceLimit(-1);
		setImage(anim);

		offsetTo(x, y);
		setArrivalRemoval(false);
		setUnitPassThrough(true);
		setWallPassThrough(true);
		disableImageDrop();
		setImageOffsetY(-5);
		setVelocityPerSecond(VELOCITY[Options.difficulty]);

		newDestination();

		this.owner = owner;

		duration = MAX_DURATION;
		countdown = INTERVAL;
	}

	public RoamingExploder(RoamingExploder copyMe) {
		super(copyMe);

		duration = copyMe.duration;
		countdown = copyMe.countdown;
		owner = copyMe.owner;
	}

	@Override
	public void update(int deltaTime) {
		super.update(deltaTime);

		countdown -= deltaTime;
		if (countdown <= 0) {
			countdown += INTERVAL;
			AudioManager.purpleExplosion.play();
			queueExplosion(new Explosion(getX(), getY(), RADIUS, DAMAGE[Options.difficulty]));
		}

		duration -= deltaTime;
		if (duration <= 0)
			isFinished = true;
	}

	@Override
	protected void destinationReached() {
		super.destinationReached();        // Not strictly necessary at this time

		newDestination();
	}

	private void newDestination() {
		// We choose x and y coordinates independently. Each coordinate has an increased chance of being given a minimum or maximum coordinate
		//(compared any other coordinate more towards the middle of the screen). This causes destinations to be more likely to occur somewhere
		//around the border of the map in order to discourage the player from simply hiding in the corners or along the edges of the map
		final float x;
		final float y;

		float decider = Util.rand.nextFloat();

		// Choose x
		if (decider <= 0.15)
			x = DESTINATION_BORDER;
		else if (decider <= 0.3)
			x = Screen.WIDTH - DESTINATION_BORDER;
		else
			x = DESTINATION_BORDER + Util.rand.nextFloat() * (Screen.WIDTH - (DESTINATION_BORDER * 2));

		decider = Util.rand.nextFloat();

		// Choose y
		if (decider <= 0.15)
			y = DESTINATION_BORDER;
		else if (decider <= 0.3)
			y = Screen.HEIGHT - DESTINATION_BORDER;
		else
			y = DESTINATION_BORDER + Util.rand.nextFloat() * (Screen.HEIGHT - (DESTINATION_BORDER * 2));

		// Apply destination
		setDestination(x, y);
	}

	@Override
	public RoamingExploder copy() {
		return new RoamingExploder(this);
	}
}