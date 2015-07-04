package com.lescomber.vestige.aiabilities.sone;

import com.lescomber.vestige.Options;
import com.lescomber.vestige.aiabilities.AIAbility;
import com.lescomber.vestige.framework.AudioManager;
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

		hitBundle = new HitBundle(DAMAGE[Options.difficulty]);

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
		final DisplacementEffect dashEffect = new DisplacementEffect(target.getCenter(), VELOCITY_PER_SECOND[Options.difficulty]);
		owner.addStatusEffect(dashEffect);
	}

	public boolean isInRange() {
		return target != null && (owner.getCenter().distanceToPointSquared(target.getCenter()) <= RANGE_SQUARED);
	}

	public void strike() {
		if (target != null) {
			AudioManager.oneSixGroundSlam.play();
			target.hit(hitBundle);
		}
	}

	public boolean isTargetLeft() {
		return target == null || (target.getX() <= owner.getX());
	}

	@Override
	public Charge copy() {
		return new Charge(this);
	}
}