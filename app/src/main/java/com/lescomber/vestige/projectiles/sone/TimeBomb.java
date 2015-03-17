package com.lescomber.vestige.projectiles.sone;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.projectiles.AIProjectileBehavior;
import com.lescomber.vestige.projectiles.Explosion;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.screens.OptionsScreen;

public class TimeBomb extends Projectile
{
	private static final float[] DAMAGE = new float[] { 5, 7.5f, 10 };
	private static final float EXPLOSION_RADIUS = 50;

	private int timer;

	// FIXME: Whyyyy does the glow from one of these things expand continuously very rarely?

	public TimeBomb(float x, float y, float destX, float destY, int timer)
	{
		super(null, 0, 8);

		setWallPassThrough(true);
		setUnitPassThrough(true);
		setArrivalRemoval(false);
		setOffScreenRemoval(false);

		final SpriteAnimation anim = new SpriteAnimation(SpriteManager.plasmaBall);
		anim.setSequenceLimit(-1);
		setImage(anim);

		offsetTo(x, y);
		setDestination(destX, destY);

		setGlow(SpriteManager.purpleGlow);

		this.timer = timer;

		setVelocityPerSecond(450 + 225 * OptionsScreen.difficulty);

		final Explosion e = new Explosion(EXPLOSION_RADIUS, DAMAGE[OptionsScreen.difficulty]);
		setExplosion(e);
	}

	public TimeBomb(int timer)
	{
		this(0, 0, 0, 0, timer);
	}

	public TimeBomb(TimeBomb copyMe)
	{
		super(copyMe);

		timer = copyMe.timer;
	}

	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);

		timer -= deltaTime;
		if (timer <= 0)
			explode();
	}

	@Override
	public AIProjectileBehavior getBehavior()
	{
		final AIProjectileBehavior aip = new AIProjectileBehavior();
		aip.isExtended = false;
		return aip;
	}

	@Override
	public TimeBomb copy()
	{
		return new TimeBomb(this);
	}
}