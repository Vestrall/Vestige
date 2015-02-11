package com.lescomber.vestige.units;

import com.lescomber.vestige.aiabilities.AIAbility;
import com.lescomber.vestige.aiabilities.AIShooter;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.statuseffects.HitBundle;
import com.lescomber.vestige.statuseffects.StatPack;

public class TutorialCaster extends AIUnit
{
	private boolean isAngry;
	
	public TutorialCaster()
	{
		super(60, 40, -23, 30);
		
		setIdleLeftSprite(SpriteManager.casterFiringLeft[0]);
		setIdleRightSprite(SpriteManager.casterFiringRight[0]);
		
		isAngry = false;
		
		// Init default stats
		final StatPack baseStats = new StatPack();
		baseStats.maxHp = 150;
		baseStats.moveSpeed = 0;
		setDifficultyScaledBaseStats(baseStats);
		
		// Init abilities
		final Projectile shot = new Projectile(SpriteManager.enemyProjectile, 2,
				Projectile.ENEMY_PROJECTILE_WIDTH, Projectile.ENEMY_PROJECTILE_HEIGHT);
		shot.setGlow(SpriteManager.redGlow);
		final AIShooter shooter = new AIShooter(this, shot, 3);
		shooter.setCooldown(1000);
		addAbility(shooter);
		
		// Init firing animation
		final SpriteAnimation preFiringLeftAnim = new SpriteAnimation(SpriteManager.casterFiringLeft, 0, 3);
		setPreFiringLeftAnimation(preFiringLeftAnim);
		final SpriteAnimation postFiringLeftAnim = new SpriteAnimation(SpriteManager.casterFiringLeft, 4, 6);
		setPostFiringLeftAnimation(postFiringLeftAnim);
		
		final SpriteAnimation preFiringRightAnim = new SpriteAnimation(SpriteManager.casterFiringRight, 0, 3);
		setPreFiringRightAnimation(preFiringRightAnim);
		final SpriteAnimation postFiringRightAnim = new SpriteAnimation(SpriteManager.casterFiringRight, 4, 6);
		setPostFiringRightAnimation(postFiringRightAnim);
		
		// Init death animations
		final SpriteAnimation deathLeftAnim = new SpriteAnimation(SpriteManager.casterDeathLeft);
		for (int i=0; i<13; i++)
			deathLeftAnim.addFrame(SpriteManager.casterDeathLeft[11]);
		setDeathAnimationLeft(deathLeftAnim);
		final SpriteAnimation deathRightAnim = new SpriteAnimation(SpriteManager.casterDeathRight);
		for (int i=0; i<13; i++)
			deathRightAnim.addFrame(SpriteManager.casterDeathRight[11]);
		setDeathAnimationRight(deathRightAnim);
		setDeathAnimXOffset(4);
	}
	
	public TutorialCaster(TutorialCaster copyMe)
	{
		super(copyMe);
		
		isAngry = copyMe.isAngry;
	}
	
	@Override
	public void hit(HitBundle bundle)
	{
		if (!isAngry)
		{
			for (final AIAbility aia : getAbilities())
				aia.setCooldown(0.5);
			isAngry = true;
		}
		
		super.hit(bundle);
	}
	
	@Override
	public TutorialCaster copy()
	{
		return new TutorialCaster(this);
	}
}