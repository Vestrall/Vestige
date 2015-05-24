package com.lescomber.vestige.aiabilities.sone;

import com.lescomber.vestige.aiabilities.AIAbility;
import com.lescomber.vestige.audio.AudioManager;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.statuseffects.DisplacementEffect;
import com.lescomber.vestige.statuseffects.HitBundle;
import com.lescomber.vestige.units.AIUnit;
import com.lescomber.vestige.units.Unit;

public class Charge extends AIAbility {
	private static final float RANGE_SQUARED = 65 * 65;

	private static final int[] VELOCITY_PER_SECOND = new int[] { 300, 450, 600 };
	private static final float[] DAMAGE = new float[] { 10, 15, 20 };

	private final HitBundle hitBundle;

	private Unit target;

	public Charge(AIUnit owner, double cooldownSeconds) {
		super(owner, cooldownSeconds);

		setUsesAnimation(false);

		hitBundle = new HitBundle(DAMAGE[OptionsScreen.difficulty]);

		target = null;
	}

	public Charge(Charge copyMe) {
		super(copyMe);

		hitBundle = new HitBundle(copyMe.hitBundle);
		target = copyMe.target;
	}

	@Override
	public boolean decideToFire() {
		target = Unit.getNearestMember(owner.getCenter(), getTargetFaction());

		if (target != null) {
			owner.faceTowards(target.getX());    // Face target
			return true;
		} else
			return false;
	}

	@Override
	public void activate() {
		final DisplacementEffect dashEffect = new DisplacementEffect(target.getCenter(), VELOCITY_PER_SECOND[OptionsScreen.difficulty]);
		owner.addStatusEffect(dashEffect);
	}

	public boolean isInRange() {
		if (target != null)
			return (owner.getCenter().distanceToPointSquared(target.getCenter()) <= RANGE_SQUARED);
		else
			return false;
	}

	public void strike() {
		if (target != null) {
			AudioManager.oneSixGroundSlam.play();
			target.hit(hitBundle);
		}
	}

	public boolean isTargetLeft() {
		if (target != null)
			return (target.getX() <= owner.getX());
		else
			return true;
	}

	@Override
	public Charge copy() {
		return new Charge(this);
	}
}