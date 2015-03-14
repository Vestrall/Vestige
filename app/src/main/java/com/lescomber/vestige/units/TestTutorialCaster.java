package com.lescomber.vestige.units;

import com.lescomber.vestige.aiabilities.AIAbility;
import com.lescomber.vestige.aiabilities.AIShooter;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.statuseffects.HitBundle;
import com.lescomber.vestige.statuseffects.StatPack;

import java.util.List;

public class TestTutorialCaster extends AIUnit
{
	private boolean isAngry;
	private final List<AIAbility> abilities;
	
	public TestTutorialCaster()
	{
		super(60, 36, -21, 30);
		
		setIdleLeftSprite(SpriteManager.casterFiringLeft[0]);
		setIdleRightSprite(SpriteManager.casterFiringRight[0]);
		
		isAngry = false;
		
		// Init default stats
		final StatPack baseStats = new StatPack();
		baseStats.maxHp = 1000;
		baseStats.moveSpeed = 0;
		setDifficultyScaledBaseStats(baseStats);
		
		// Init abilities
		final Projectile shot = new Projectile(SpriteManager.enemyProjectile, 0,
				Projectile.ENEMY_PROJECTILE_WIDTH, Projectile.ENEMY_PROJECTILE_HEIGHT);
		shot.setGlow(SpriteManager.redGlow);
		final AIShooter shooter = new AIShooter(this, shot, 3);
		shooter.setCooldown(1000);
		addAbility(shooter);
		abilities = getAbilities();
		
		// Init firing animation
		final SpriteAnimation preFiringLeftAnim = new SpriteAnimation(SpriteManager.casterFiringLeft, 0, 3);
		setPreFiringLeftAnimation(preFiringLeftAnim);
		final SpriteAnimation postFiringLeftAnim = new SpriteAnimation(SpriteManager.casterFiringLeft, 4, 6);
		setPostFiringLeftAnimation(postFiringLeftAnim);
		
		final SpriteAnimation preFiringRightAnim = new SpriteAnimation(SpriteManager.casterFiringRight, 0, 3);
		setPreFiringRightAnimation(preFiringRightAnim);
		final SpriteAnimation postFiringRightAnim = new SpriteAnimation(SpriteManager.casterFiringRight, 4, 6);
		setPostFiringRightAnimation(postFiringRightAnim);
	}
	
	public TestTutorialCaster(TestTutorialCaster copyMe)
	{
		super(copyMe);
		
		isAngry = copyMe.isAngry;
		abilities = getAbilities();
	}
	
	@Override
	public void hit(HitBundle bundle)
	{
		if (!isAngry)
		{
			for (final AIAbility aia : abilities)
				aia.setCooldown(0.5);
			isAngry = true;
		}
		
		super.hit(bundle);
	}
	
	@Override
	public TestTutorialCaster copy()
	{
		return new TestTutorialCaster(this);
	}
}