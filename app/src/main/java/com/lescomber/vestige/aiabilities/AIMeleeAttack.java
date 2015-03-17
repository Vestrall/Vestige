package com.lescomber.vestige.aiabilities;

import com.lescomber.vestige.audio.AudioManager.SoundEffect;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.statuseffects.HitBundle;
import com.lescomber.vestige.units.AIUnit;
import com.lescomber.vestige.units.Unit;

public class AIMeleeAttack extends AIAbility
{
	private static final int CLOSE_RANGE_SQUARED = 250 * 250;    // Close enough to target to update destination frequently
	private static final int MELEE_RANGE_SQUARED = 80 * 80;        // Close enough to attack target

	// Cooldown before updating destination again if distance to target is greater than CLOSE_RANGE_SQUARED)
	private static final double LONG_RANGE_CD = 0.65;

	// Cooldown before updating destination again if distance to target is less than CLOSE_RANGE_SQUARED)
	private static final double SHORT_RANGE_CD = 0.15;

	private static final float[] DAMAGE_SCALING = new float[] { 0.65f, 0.95f, 1.25f };    // Default difficulty damage scaling

	private final HitBundle hitBundle;    // HitBundle to be delivered to target on melee attack

	private Unit target;            // Current target for both moving towards and, if in range, attacking

	public AIMeleeAttack(AIUnit owner, float damage, double cooldownSeconds)
	{
		super(owner, cooldownSeconds);

		hitBundle = new HitBundle(damage);

		target = null;
	}

	public AIMeleeAttack(AIMeleeAttack copyMe)
	{
		super(copyMe);

		hitBundle = new HitBundle(copyMe.hitBundle);

		// Not copied
		target = null;
	}

	@Override
	public boolean scaleForDifficulty()
	{
		if (super.scaleForDifficulty())
		{
			// Scale damage
			hitBundle.setDamage(hitBundle.getDamage() * DAMAGE_SCALING[OptionsScreen.difficulty]);

			return true;
		}
		else
			return false;
	}

	@Override
	public boolean decideToFire()
	{
		// Select nearest enemy
		target = Unit.getNearestMember(owner.getCenter(), owner.getOpponents());
		if (target == null)
			return false;

		final float d2 = (float) owner.getCenter().distanceToPointSquared(target.getCenter());
		if (d2 < MELEE_RANGE_SQUARED)        // If in range, attack target
			return true;
		else if (d2 < CLOSE_RANGE_SQUARED)    // If not in range but near target, trigger short CD before deciding again
		{
			setCooldown(SHORT_RANGE_CD);
			return false;
		}
		else                            // If not in range and far away from  target, trigger long CD before deciding again
		{
			setCooldown(LONG_RANGE_CD);
			return false;
		}
	}

	@Override
	public void activate()
	{
		target.hit(hitBundle);
	}

	public void setHitSound(SoundEffect hitSound)
	{
		hitBundle.setHitSound(hitSound);
	}

	@Override
	public AIMeleeAttack copy()
	{
		return new AIMeleeAttack(this);
	}
}