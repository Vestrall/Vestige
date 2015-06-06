package com.lescomber.vestige;

import com.lescomber.vestige.framework.AudioManager.SoundEffect;

public abstract class Ability {
	private SoundEffect soundEffect;

	private int maxCooldown;    // Maximum cooldown (in ms)
	protected int cooldown;        // Current cooldown (in ms)

	public Ability() {
		soundEffect = null;

		maxCooldown = 0;
		cooldown = 0;
	}

	public Ability(Ability copyMe) {
		soundEffect = null;
		if (copyMe.soundEffect != null)
			soundEffect = copyMe.soundEffect;
		maxCooldown = copyMe.maxCooldown;
		cooldown = copyMe.cooldown;
	}

	public void update(int deltaTime) {
		cooldown -= deltaTime;
	}

	public boolean isReadyToFire() {
		return cooldown <= 0;
	}

	public void playSoundEffect() {
		if (soundEffect != null)
			soundEffect.play();
	}

	public int getMaxCooldown() {
		return maxCooldown;
	}

	public int getCooldown() {
		return cooldown;
	}

	public void setSoundEffect(SoundEffect soundEffect) {
		this.soundEffect = soundEffect;
	}

	public void setMaxCooldown(double cooldownSeconds) {
		maxCooldown = (int) (cooldownSeconds * 1000);
	}

	public void setCooldown(double cooldownSeconds) {
		cooldown = (int) (cooldownSeconds * 1000);
	}

	public void triggerCooldown() {
		cooldown = maxCooldown;
	}

	public abstract Ability copy();
}