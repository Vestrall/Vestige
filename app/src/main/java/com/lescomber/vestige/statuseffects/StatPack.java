package com.lescomber.vestige.statuseffects;

import com.lescomber.vestige.framework.Util;

public class StatPack {
	public float maxHp;        // Also behaves as bonusHp when used in StatusEffects
	public float shields;
	public float maxShields;
	public float moveSpeed;
	public double moveSpeedPercent;        // 1.0 = normal. adds multiplicatively

	public StatPack() {
		initDefaults();
	}

	public StatPack(StatPack copyMe) {
		maxHp = copyMe.maxHp;
		shields = copyMe.shields;
		maxShields = copyMe.maxShields;
		moveSpeed = copyMe.moveSpeed;
		moveSpeedPercent = copyMe.moveSpeedPercent;
	}

	private void initDefaults() {
		maxHp = 0;
		shields = 0;
		maxShields = 0;
		moveSpeed = 0;
		moveSpeedPercent = 1.0;
	}

	public void clear() {
		initDefaults();
	}

	public void add(StatPack other) {
		maxHp += other.maxHp;
		shields += other.shields;
		maxShields += other.maxShields;
		moveSpeed += other.moveSpeed;
		moveSpeedPercent *= other.moveSpeedPercent;
	}

	public StatPack portion(float percentage) {
		final StatPack sp = new StatPack(this);

		sp.maxHp *= percentage;
		sp.maxShields *= percentage;
		sp.shields = Math.min(sp.shields, sp.maxShields);	// Cap shields to new maxShields
		sp.moveSpeed *= percentage;
		double slowPercent = 1 - sp.moveSpeedPercent;
		slowPercent *= percentage;
		sp.moveSpeedPercent = 1 - slowPercent;

		return sp;
	}

	public void setMaxShields(float maxShields) {
		this.maxShields = maxShields;
		shields = maxShields;
	}

	public boolean isEmpty() {
		return (Util.equals(maxHp, 0) && shields <= 0 && Util.equals(moveSpeed, 0) && Util.equals(moveSpeedPercent, 1));
	}
}