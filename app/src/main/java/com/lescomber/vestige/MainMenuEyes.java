package com.lescomber.vestige;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.graphics.Sprite;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.graphics.Swapper;

public class MainMenuEyes
{
	private Sprite idleSprite;            // Open eyes
	private SpriteAnimation anim;        // Blink animation
	private float alpha;                // Current alpha (< 1.0 during fade in)
	private static final float ALPHA_PER_MS = 0.001f;        // Fade in speed

	private float blinkDelay;                                // Delay (in ms) until next blink
	private static final int BLINK_DELAY_MAX = 9000;        // Max delay between blinks (actual delay will be randomized)
	private static final double DOUBLE_BLINK_CHANCE = 0.15;    // Chance of blinking twice in a row

	public MainMenuEyes(float x, float y, int size)
	{
		if (size == 1)        // Case: small eyes
		{
			idleSprite = new Sprite(SpriteManager.smallEyes[0], x, y);
			anim = new SpriteAnimation(SpriteManager.smallEyes);
		}
		else if (size == 2)    // Case: medium eyes
		{
			idleSprite = new Sprite(SpriteManager.mediumEyes[0], x, y);
			anim = new SpriteAnimation(SpriteManager.mediumEyes);
		}
		else if (size == 3)    // Case: big eyes
		{
			idleSprite = new Sprite(SpriteManager.bigEyes[0], x, y);
			anim = new SpriteAnimation(SpriteManager.bigEyes);
		}

		anim.offsetTo(x, y);
		anim.setHoldLastFrame(true);
		anim.setAlpha(0);
		idleSprite.setAlpha(0);

		blinkDelay = Integer.MAX_VALUE;
		alpha = 1;    // Alpha starts at 1 to avoid triggering "fade in" code in the update method until fadeIn() is called
	}

	public void update(int deltaTime)
	{
		// Handle fade in
		if (alpha < 1)
		{
			alpha += ALPHA_PER_MS * deltaTime;
			if (alpha > 1)
				alpha = 1;
			idleSprite.setAlpha(alpha);
			anim.setAlpha(alpha);
		}

		if (anim.isPlaying())    // Case: currently animating a blink
		{
			if (anim.update(deltaTime))        // Case: anim just finished
				Swapper.swapImages(anim, idleSprite);
		}
		else        // Case: currently idle, awaiting next blink
		{
			blinkDelay -= deltaTime;
			if (blinkDelay < 0)
			{
				// Restart animation
				anim.stop();
				Swapper.swapImages(idleSprite, anim);
				randomizeBlinkDelay();
			}
		}
	}

	public void fadeIn()
	{
		alpha = 0;
		randomizeBlinkDelay();
		idleSprite.setVisible(true);
	}

	public void popIn()
	{
		alpha = 1;
		idleSprite.setAlpha(alpha);
		anim.setAlpha(alpha);
		Swapper.swapImages(anim, idleSprite);
		randomizeBlinkDelay();
	}

	private void randomizeBlinkDelay()
	{
		if (Util.rand.nextDouble() < DOUBLE_BLINK_CHANCE)
			blinkDelay = 0;
		else
			blinkDelay = Util.rand.nextInt(BLINK_DELAY_MAX);
	}
}