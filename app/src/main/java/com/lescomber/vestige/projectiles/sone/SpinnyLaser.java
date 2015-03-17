package com.lescomber.vestige.projectiles.sone;

import com.lescomber.vestige.audio.AudioManager;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.graphics.Sprite;
import com.lescomber.vestige.projectiles.Beam;
import com.lescomber.vestige.projectiles.HitGroup;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.projectiles.glows.BeamGlow;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.units.AIUnit;

public class SpinnyLaser extends Projectile
{
	private static final float HITBOX_WIDTH = 35;
	private static final float HITBOX_HEIGHT = 8;
	private static final float ROTATION_PER_MS = (float) Math.PI / 250;    // Two rotations per second
	private static final int CHANNEL_DURATION_MS = 1250;

	private static final float[] BEAM_DAMAGE = new float[] { 8, 11, 14 };
	private static final float GROWTH_PER_MS = 1.4f;

	private final AIUnit owner;

	private int delayRemaining;

	private boolean hasArrived;
	private int channelRemaining;

	private float width;

	private static final float HEAD_VISIBLE_WIDTH = 16;
	private Sprite headOne;
	private Sprite headTwo;
	private float headOffsetXPerMS;
	private float headOffsetYPerMS;

	private final HitGroup hitGroup;

	public SpinnyLaser(float x, float y, float destX, float destY, AIUnit owner, int delayMS)
	{
		super(SpriteManager.doubleEnemyLaser, 0, HITBOX_WIDTH, HITBOX_HEIGHT);

		width = HITBOX_WIDTH;

		disableImageDrop();

		setGlow(new BeamGlow());

		offsetTo(x, y);
		setDestination(destX, destY);

		setOffScreenRemoval(false);
		setArrivalRemoval(false);
		setUnitPassThrough(true);
		setWallPassThrough(true);

		setVelocityPerSecond(350 + (50 * OptionsScreen.difficulty));

		this.owner = owner;
		delayRemaining = delayMS;

		hasArrived = false;
		channelRemaining = CHANNEL_DURATION_MS;

		hitGroup = new HitGroup();
	}

	public SpinnyLaser(SpinnyLaser copyMe)
	{
		super(copyMe);

		owner = copyMe.owner;    // Not a copy. References the same owner
		delayRemaining = copyMe.delayRemaining;
		hasArrived = copyMe.hasArrived;
		channelRemaining = copyMe.channelRemaining;
		width = copyMe.width;
		if (copyMe.headOne != null)
			headOne = new Sprite(copyMe.headOne);
		if (copyMe.headTwo != null)
			headTwo = new Sprite(copyMe.headTwo);
		headOffsetXPerMS = copyMe.headOffsetXPerMS;
		headOffsetYPerMS = copyMe.headOffsetYPerMS;
		hitGroup = new HitGroup();
	}

	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);

		// Note: hitGroup is not updated because we never want it to remove any units that have been added to it anyway

		// Rotate projectile if it is still en route
		if (!hasArrived)
			rotate(ROTATION_PER_MS * deltaTime);

		if (delayRemaining > 0)
		{
			delayRemaining -= deltaTime;

			if (delayRemaining <= 0)
			{
				// Turn on the damage
				setDamage(BEAM_DAMAGE[OptionsScreen.difficulty]);
				setHitGroup(hitGroup);
				setUnitHitSound(AudioManager.enemyLaserHit);
				clearHitList();

				// Switch over to movable images
				headOne.setVisible(isVisible());
				headTwo.setVisible(isVisible());
				setImage(new Sprite(SpriteManager.enemyLaserBody));
			}
		}
		else
		{
			width += GROWTH_PER_MS * deltaTime;
			getHitbox().scaleWidthTo(width);
			getSprite().scaleWidthTo(width - (HEAD_VISIBLE_WIDTH * 2) + 1);

			headOne.offset(-headOffsetXPerMS * deltaTime, -headOffsetYPerMS * deltaTime);
			headTwo.offset(headOffsetXPerMS * deltaTime, headOffsetYPerMS * deltaTime);

			channelRemaining -= deltaTime;
			if (channelRemaining <= 0)
			{
				// Create two beams and set them on their way with the same HitGroup as this SpinnyLaser
				final Beam beamOne = new Beam(getX(), getY(), getDirection(), getHitBundle().getDamage());
				final Beam beamTwo = new Beam(getX(), getY(), getDirection() + (float) Math.PI, getHitBundle().getDamage());

				beamOne.setHitGroup(hitGroup);
				beamTwo.setHitGroup(hitGroup);

				// Grow off screen and then some
				beamOne.update(2000);
				beamTwo.update(2000);

				// Immediately stop growing since we're only interested in the fade away part
				beamOne.stopGrowth();
				beamTwo.stopGrowth();

				queueProjectile(beamOne);
				queueProjectile(beamTwo);

				// Get rid of the original SpinnyLaser. Beams are on their own now.
				explode();
			}
		}
	}

	@Override
	protected void destinationReached()
	{
		super.destinationReached();

		final float direction = getDirection();
		headOffsetXPerMS = ((float) Math.cos(direction) * GROWTH_PER_MS) / 2;
		headOffsetYPerMS = ((float) Math.sin(direction) * GROWTH_PER_MS) / 2;

		// Set up movable images
		final Point imageCenter = getImageCenter();
		final float headOffset = (SpriteManager.enemyLaserHead.getWidth() + SpriteManager.enemyLaserBody.getWidth()) / 2;
		headOne = new Sprite(SpriteManager.enemyLaserHead, imageCenter.x - headOffset, imageCenter.y);
		headTwo = new Sprite(SpriteManager.enemyLaserHead, imageCenter.x + headOffset, imageCenter.y);
		headOne.rotate((float) Math.PI);
		headOne.rotateAbout(getDirection(), imageCenter.x, imageCenter.y);
		headTwo.rotateAbout(getDirection(), imageCenter.x, imageCenter.y);
		headOne.setLayerHeight(getSprite().getLayerHeight() + 1);
		headTwo.setLayerHeight(getSprite().getLayerHeight() + 1);

		hasArrived = true;
	}

	@Override
	public void setVisible(boolean isVisible)
	{
		super.setVisible(isVisible);

		if (headOne != null)
			headOne.setVisible(isVisible);
		if (headTwo != null)
			headTwo.setVisible(isVisible);
	}

	@Override
	public void close()
	{
		super.close();

		if (headOne != null)
			headOne.close();
		if (headTwo != null)
			headTwo.close();
	}

	@Override
	public SpinnyLaser copy()
	{
		return new SpinnyLaser(this);
	}
}