package com.lescomber.vestige.units;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.statuseffects.StatPack;

public class TestFloatingCreep extends AIUnit
{
	public TestFloatingCreep()
	{
		super(32, 28, -13, 17);
		
		final StatPack baseStats = new StatPack();
		baseStats.maxHp = 1000;
		baseStats.moveSpeed = 0;
		setDifficultyScaledBaseStats(baseStats);
		
		// Init idle sprites
		setIdleLeftSprite(SpriteManager.floatingCreepWalkLeft[0]);
		setIdleRightSprite(SpriteManager.floatingCreepWalkRight[0]);
		
		// Init walking animations
		final SpriteAnimation walkLeftAnim = new SpriteAnimation(SpriteManager.floatingCreepWalkLeft);
		setWalkLeftAnimation(walkLeftAnim);
		final SpriteAnimation walkRightAnim = new SpriteAnimation(SpriteManager.floatingCreepWalkRight);
		setWalkRightAnimation(walkRightAnim);
		
		// Init attacking animations
		final SpriteAnimation preFiringLeftAnim = new SpriteAnimation(SpriteManager.floatingCreepAttackLeft);
		setPreFiringLeftAnimation(preFiringLeftAnim);
		final SpriteAnimation preFiringRightAnim = new SpriteAnimation(SpriteManager.floatingCreepAttackRight);
		setPreFiringRightAnimation(preFiringRightAnim);
	}
	
	public TestFloatingCreep(TestFloatingCreep copyMe)
	{
		super(copyMe);
	}
	
	@Override
	public TestFloatingCreep copy()
	{
		return new TestFloatingCreep(this);
	}
}