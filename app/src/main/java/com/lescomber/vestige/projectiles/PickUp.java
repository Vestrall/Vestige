package com.lescomber.vestige.projectiles;

import com.lescomber.vestige.audio.AudioManager.SoundEffect;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;
import com.lescomber.vestige.graphics.Sprite;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.statuseffects.AnimationEffect;
import com.lescomber.vestige.statuseffects.HitBundle;
import com.lescomber.vestige.units.Player;
import com.lescomber.vestige.units.Unit;

public class PickUp extends AreaEffect
{
	private static AnimationEffect PICK_UP_EFFECT;

	private static final float[] IMAGE_OFFSET_LIMITS = new float[] { -17, -27 };
	private static final double[] DURATION = new double[] { 10, 7.5, 5 };

	// 3 seconds from ground to peak and back to ground again
	private static final float IMAGE_OFFSET_SPEED = (IMAGE_OFFSET_LIMITS[1] - IMAGE_OFFSET_LIMITS[0]) / 1500;
	private float imageOffsetY;
	private float imageOffsetYPerMS;

	private Sprite glow;

	private SoundEffect soundEffect;

	private boolean isAlphaVisible;        // true when pickup is visible, false when it is in the "invisible" phase of a flicker
	private static final int MAX_FLICKER_COUNTDOWN = 200;
	private int flickerCountdown;        // Time (in ms) until next flicker (either on or off)

	public PickUp(SpriteTemplate template, float width, float height, float damage)
	{
		super(40, 30, 0, DURATION[OptionsScreen.difficulty]);

		init(template, damage);
	}

	public PickUp(SpriteTemplate template, float radius, float damage)
	{
		super(radius, 0, DURATION[OptionsScreen.difficulty]);

		init(template, damage);
	}

	public PickUp(PickUp copyMe)
	{
		super(copyMe);

		imageOffsetY = copyMe.imageOffsetY;
		imageOffsetYPerMS = copyMe.imageOffsetYPerMS;
		glow = copyMe.glow.copy();
		soundEffect = copyMe.soundEffect;
		isAlphaVisible = copyMe.isAlphaVisible;
		flickerCountdown = copyMe.flickerCountdown;
	}

	private void init(SpriteTemplate template, float damage)
	{
		// Set pick up animation
		if (PICK_UP_EFFECT == null)
			PICK_UP_EFFECT = new AnimationEffect(new SpriteAnimation(SpriteManager.healthPickUpAnimation));
		addStatusEffect(PICK_UP_EFFECT.copy());

		if (template != null)
			setImage(new Sprite(template));

		setTickFrequency(50);
		setDamagePerTick(damage);
		setTargets(GameScreen.gregs);

		imageOffsetY = IMAGE_OFFSET_LIMITS[0];
		imageOffsetYPerMS = IMAGE_OFFSET_SPEED;

		glow = new Sprite(SpriteManager.pickUpGlow, getX(), getY());

		soundEffect = null;

		isAlphaVisible = true;
		final int flickerLifespan = (4 - OptionsScreen.difficulty);    // Flicker for the last 2-4 seconds of this PickUp's life
		flickerCountdown = (int) (1000 * (DURATION[OptionsScreen.difficulty] - flickerLifespan));
	}

	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);

		// Bounce up and down a little
		imageOffsetY += (imageOffsetYPerMS * deltaTime);

		if (imageOffsetY >= IMAGE_OFFSET_LIMITS[0])
			imageOffsetYPerMS = IMAGE_OFFSET_SPEED;
		else if (imageOffsetY <= IMAGE_OFFSET_LIMITS[1])
			imageOffsetYPerMS = -IMAGE_OFFSET_SPEED;

		setImageOffsetY(imageOffsetY);

		// Modify ground glow size
		final float heightPercent = (imageOffsetY - IMAGE_OFFSET_LIMITS[0]) / (IMAGE_OFFSET_LIMITS[1] - IMAGE_OFFSET_LIMITS[0]);
		final float glowScale = 1.0f - (heightPercent * 0.5f);
		glow.scaleTo(glowScale * glow.getTemplate().getWidth(), glowScale * glow.getTemplate().getHeight());

		// Update flicker
		flickerCountdown -= deltaTime;
		if (flickerCountdown <= 0)
		{
			flickerCountdown += MAX_FLICKER_COUNTDOWN;
			flicker();
		}
	}

	private void flicker()
	{
		if (isAlphaVisible)
		{
			getSprite().setAlpha(0);
			glow.setAlpha(0);
		}
		else
		{
			getSprite().setAlpha(1);
			glow.setAlpha(1);
		}

		isAlphaVisible = !isAlphaVisible;
	}

	@Override
	public void offset(float dx, float dy)
	{
		super.offset(dx, dy);

		glow.offset(dx, dy);
	}

	@Override
	protected void unitHit(Unit unit, HitBundle bundle)
	{
		if (isValidTarget(unit))
		{
			super.unitHit(unit, bundle);
			if (soundEffect != null)
				soundEffect.play();
			die();
		}
	}

	@Override
	protected void groupHit(Unit unit, HitBundle bundle, int suggestedCooldown)
	{
		if (isValidTarget(unit))
		{
			super.groupHit(unit, bundle, suggestedCooldown);
			die();
		}
	}

	private boolean isValidTarget(Unit unit)
	{
		return (unit instanceof Player);
	}

	public void setSoundEffect(SoundEffect soundEffect)
	{
		this.soundEffect = soundEffect;
	}

	@Override
	public void setVisible(boolean isVisible)
	{
		super.setVisible(isVisible);

		glow.setVisible(isVisible);
	}

	@Override
	public PickUp copy()
	{
		return new PickUp(this);
	}
}