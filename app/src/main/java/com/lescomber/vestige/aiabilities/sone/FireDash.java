package com.lescomber.vestige.aiabilities.sone;

import com.lescomber.vestige.aiabilities.AIAbility;
import com.lescomber.vestige.aiabilities.Dash;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.projectiles.AreaEffect;
import com.lescomber.vestige.projectiles.FireAnimation;
import com.lescomber.vestige.projectiles.HitGroup;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.statuseffects.DisplacementEffect;
import com.lescomber.vestige.units.AIRailUnit;
import com.lescomber.vestige.units.sone.OneEightBoss;

public class FireDash extends AIAbility implements Dash
{
	private static final int[] VELOCITY_PER_SECOND = new int[] { 500, 600, 700 };

	private static final float FIRE_WIDTH = 28;
	private static final float FIRE_HEIGHT = 20;
	private static final float[] FIRE_DPS = new float[] { 20, 28, 36 };
	private static final double FIRE_DURATION = 6;

	private boolean firing;
	private int firingCooldown;
	private static final int FIRING_INTERVAL = 30;

	private final HitGroup fireGroup;

	public FireDash(OneEightBoss owner, double cooldownSeconds)
	{
		super(owner, cooldownSeconds);

		setUsesAnimation(false);

		firing = false;
		firingCooldown = FIRING_INTERVAL;

		fireGroup = new HitGroup();
	}

	public FireDash(FireDash copyMe)
	{
		super(copyMe);

		firing = copyMe.firing;
		firingCooldown = copyMe.firingCooldown;

		// Not copied
		fireGroup = new HitGroup();
	}

	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);

		fireGroup.update(deltaTime);

		if (firing)
		{
			firingCooldown -= deltaTime;
			if (firingCooldown <= 0)
			{
				firingCooldown += FIRING_INTERVAL;

				final AreaEffect ae = new AreaEffect(FIRE_WIDTH, FIRE_HEIGHT, 0, FIRE_DURATION);
				ae.setDamagePerSecond(FIRE_DPS[OptionsScreen.difficulty]);
				ae.setImage(new FireAnimation());
				ae.setImageOffsetY(FireAnimation.IMAGE_OFFSET_Y);
				ae.setHitGroup(fireGroup);
				ae.offsetTo(owner.getCenter());
				ae.setTickFrequency(100);
				owner.queueAreaEffect(ae);
			}
		}
	}

	@Override
	public void activate()
	{
		final float dashX = Util.rand.nextFloat() * Screen.WIDTH;
		final float dashY = Util.rand.nextFloat() * Screen.HEIGHT;
		final Point dashPoint = GameScreen.map.adjustDestination(dashX, dashY, owner.getTopGap());

		// Create and apply DisplacementEffect
		final DisplacementEffect dashEffect = new DisplacementEffect(dashPoint, VELOCITY_PER_SECOND[OptionsScreen.difficulty]);
		owner.addStatusEffect(dashEffect, this);

		// Cause owner to pick a new walking destination once dash is completed
		if (owner instanceof AIRailUnit)
			((AIRailUnit) owner).chooseDestination();

		// Toggle the dropping of fire AreaEffects
		firing = true;
	}

	@Override
	public void dashComplete()
	{
		firing = false;
		triggerCooldown();
	}

	@Override
	public FireDash copy()
	{
		return new FireDash(this);
	}
}