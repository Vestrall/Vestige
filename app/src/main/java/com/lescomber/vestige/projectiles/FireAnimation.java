package com.lescomber.vestige.projectiles;

import com.lescomber.vestige.audio.AudioManager;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.graphics.Sprite;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.graphics.Swapper;
import com.lescomber.vestige.screens.GameScreen;

import java.util.List;

public class FireAnimation extends SpriteAnimation
{
	public static final float IMAGE_OFFSET_Y = -18;

	private boolean isIgniting;

	private int duration;    // Duration the repeating part of the animation will last. Should only be stored here if we are
	//currently igniting

	private boolean firstRun;

	public static int fireCount = 0;    // For use with AudioManager's fireLoop. 0 = stop. > 0 = play

	public FireAnimation()
	{
		super();

		isIgniting = true;

		// Add ignition frames
		addFrames(SpriteManager.groundFire, 0, 2);

		setHoldLastFrame(true);

		duration = 0;
		firstRun = true;
	}

	public FireAnimation(FireAnimation copyMe)
	{
		super(copyMe);

		isIgniting = copyMe.isIgniting;

		// if we are in the repeating phase of the animation, (re)randomize the frame speed so we don't synchronize with copyMe (or
		//any other copies of copyMe)
		if (!isIgniting)
			setFrameTime(Util.rand.nextInt(55) + 25);

		duration = copyMe.duration;
		firstRun = copyMe.firstRun;
	}

	@Override
	public boolean update(int deltaTime)
	{
		final boolean superRet = super.update(deltaTime);

		if (firstRun)
		{
			start();
			firstRun = false;
		}

		if (isIgniting)
		{
			if (superRet)    // Case: we have just finished the ignition animation
			{
				isIgniting = false;

				// Remember the currently held last frame of ignition for the Swapper
				final Sprite oldFrame = getSprite();

				// Clear ignition frames
				clearFrames();

				// Populate frames with the repeating part of the animation
				addFrames(SpriteManager.groundFire, 3, 10);

				// Randomize the frame speed for the repeating part of the animation so nearby fire animations started at similar
				//times don't synchronize
				setFrameTime(Util.rand.nextInt(55) + 25);

				// Set the sequence limit (defaults to perpetual)
				if (duration > 0)
					setDuration(duration);
				else
					setSequenceLimit(-1);

				// Transition to the repeating part of the animation
				Swapper.swapImages(oldFrame, this);
			}

			return false;
		}
		else
			return superRet;
	}

	private void start()
	{
		if (fireCount == 0)
			AudioManager.fireLoop.play();
		fireCount++;
	}

	public static void varyLocations(List<FireAnimation> anims, float xVariance, float yVariance)
	{
		final float halfXVar = xVariance / 2;
		final float halfYVar = yVariance / 2;
		for (final FireAnimation fa : anims)
			fa.offset(Util.rand.nextFloat() * xVariance - halfXVar, Util.rand.nextFloat() * yVariance - halfYVar);
	}

	public static void varyLocations(FireAnimation[] anims, float xVariance, float yVariance)
	{
		final float halfXVar = xVariance / 2;
		final float halfYVar = yVariance / 2;
		for (final FireAnimation fa : anims)
			fa.offset(Util.rand.nextFloat() * xVariance - halfXVar, Util.rand.nextFloat() * yVariance - halfYVar);
	}

	@Override
	public void setDuration(int duration)
	{
		if (isIgniting)
			this.duration = duration - getTimeRemaining();
		else
			super.setDuration(duration);
	}

	@Override
	public void close()
	{
		super.close();

		fireCount--;
		if (fireCount == 0)
			AudioManager.fireLoop.stop();

		final SpriteAnimation anim = new SpriteAnimation();
		anim.addFrame(SpriteManager.groundFire[2]);
		anim.addFrame(SpriteManager.groundFire[1]);
		anim.addFrame(SpriteManager.groundFire[0]);
		anim.offsetTo(getX(), getY());
		GameScreen.playAnimation(anim);
	}

	@Override
	public FireAnimation copy()
	{
		return new FireAnimation(this);
	}
}