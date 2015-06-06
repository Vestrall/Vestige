package com.lescomber.vestige.projectiles;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.AudioManager;

public class HealPickUp extends PickUp {
	public HealPickUp(float healAmount) {
		super(SpriteManager.healthPickUp, 40, 30, -healAmount);

		setSoundEffect(AudioManager.healPickUp);
	}
}