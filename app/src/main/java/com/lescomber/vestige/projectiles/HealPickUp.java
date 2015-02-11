package com.lescomber.vestige.projectiles;

import com.lescomber.vestige.audio.AudioManager;
import com.lescomber.vestige.crossover.SpriteManager;

public class HealPickUp extends PickUp
{
	public HealPickUp(float healAmount)
	{
		super(SpriteManager.healthPickUp, 40, 30, -healAmount);
		
		setSoundEffect(AudioManager.healPickUp);
	}
}