package com.lescomber.vestige.projectiles;

import com.lescomber.vestige.audio.AudioManager;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;
import com.lescomber.vestige.graphics.Swapper;
import com.lescomber.vestige.graphics.ThreePatchSwingSprite;
import com.lescomber.vestige.map.Obstacle;
import com.lescomber.vestige.projectiles.glows.BeamGlow;

public class Beam extends Projectile
{
	private static final float HEIGHT = 8;
	private static final float GROWTH_PER_MS = 0.7f;

	private float offsetXPerMs;
	private float offsetYPerMs;

	private boolean growing;
	private boolean wallHit;
	private float width;

	//private final BeamSprite beamSprite;
	private ThreePatchSwingSprite beamSprite;

	public Beam(float x, float y, float direction, float damage)
	{
		super(null, damage);

		final SpriteTemplate[] templates = new SpriteTemplate[3];
		templates[0] = SpriteManager.enemyLaserBody;
		templates[1] = null;
		templates[2] = SpriteManager.enemyLaserHead;
		beamSprite = new ThreePatchSwingSprite(templates, 0, 0);
		//beamSprite = new BeamSprite();

		setImage(beamSprite);
		setUnitHitSound(AudioManager.enemyLaserHit);

		setImageOffsetY(DEFAULT_IMAGE_OFFSET_Y);
		setGlow(new BeamGlow());

		width = beamSprite.getWidth();

		offsetTo(x, y);
		createRotatedRectHitbox(width, HEIGHT);

		setVelocity(0);
		setDestination(null);
		setUnitPassThrough(true);
		setWallPassThrough(true);    // Not really necessary but here just in case. We will handle wall collisions manually

		rotateTo(direction);

		offsetXPerMs = ((float) Math.cos(direction) * GROWTH_PER_MS) / 2;
		offsetYPerMs = ((float) Math.sin(direction) * GROWTH_PER_MS) / 2;
		growing = true;
		wallHit = false;
	}

	public Beam(Beam copyMe)
	{
		super(copyMe);

		//beamSprite = new BeamSprite(copyMe.beamSprite);
		beamSprite = new ThreePatchSwingSprite(copyMe.beamSprite);
		setImage(beamSprite);
		offsetXPerMs = copyMe.offsetXPerMs;
		offsetYPerMs = copyMe.offsetYPerMs;
		growing = copyMe.growing;
		wallHit = copyMe.wallHit;
	}

	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);

		if (growing)
		{
			if (!wallHit)
			{
				width += GROWTH_PER_MS * deltaTime;
				final float dx = offsetXPerMs * deltaTime;
				final float dy = offsetYPerMs * deltaTime;

				hitbox.scaleWidthTo(width);
				hitbox.offset(dx, dy);

				//beamSprite.age(width, dx, dy);
				beamSprite.scaleWidthTo(width);
				//beamSprite.offset(dx, dy);
				updateGlow(hitbox);
			}
		}
		else
		{
			width -= GROWTH_PER_MS * deltaTime;
			if (width <= 0)
				explode();
			else
			{
				final float dx = offsetXPerMs * deltaTime;
				final float dy = offsetYPerMs * deltaTime;

				hitbox.scaleWidthTo(width);
				hitbox.offset(dx, dy);

				//beamSprite.age(width, dx, dy);
				beamSprite.scaleWidthTo(width);
				beamSprite.offset(dx * 2, dy * 2);
				updateGlow(hitbox);
			}
		}
	}

	public void stopGrowth()
	{
		growing = false;
	}

	@Override
	protected void obstacleHit(Obstacle o)
	{
		if (wallHit == true)
			return;

		wallHit = true;
		//beamSprite.loseHead();
		final SpriteTemplate[] templates = new SpriteTemplate[3];
		templates[0] = SpriteManager.enemyLaserBody;
		templates[1] = null;
		templates[2] = null;
		final ThreePatchSwingSprite newBeamSprite = new ThreePatchSwingSprite(templates, 0, 0);
		newBeamSprite.rotate(beamSprite.getDirection());
		newBeamSprite.scaleWidthTo(beamSprite.getWidth());
		newBeamSprite.offsetTo(beamSprite.getSwingX(), beamSprite.getSwingY());
		Swapper.swapImages(beamSprite, newBeamSprite);
		beamSprite = newBeamSprite;
		imageRef(beamSprite);
	}

	@Override
	public void rotate(float radians)
	{
		super.rotate(radians);

		final double direction = getDirection();

		offsetXPerMs = ((float) Math.cos(direction) * GROWTH_PER_MS) / 2;
		offsetYPerMs = ((float) Math.sin(direction) * GROWTH_PER_MS) / 2;
	}

	@Override
	public void rotateAbout(float radians, float rotateX, float rotateY)
	{
		super.rotateAbout(radians, rotateX, rotateY);

		offsetXPerMs = ((float) Math.cos(getDirection()) * GROWTH_PER_MS) / 2;
		offsetYPerMs = ((float) Math.sin(getDirection()) * GROWTH_PER_MS) / 2;
	}

	@Override
	public Beam copy()
	{
		return new Beam(this);
	}
}