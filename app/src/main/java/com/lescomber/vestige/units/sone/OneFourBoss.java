package com.lescomber.vestige.units.sone;

import com.lescomber.vestige.aiabilities.AIAbility;
import com.lescomber.vestige.aiabilities.AIShooter;
import com.lescomber.vestige.aiabilities.sone.MirrorImage;
import com.lescomber.vestige.aiabilities.sone.ShieldMeteorShower;
import com.lescomber.vestige.aiabilities.sone.SpinnyLaserScatter;
import com.lescomber.vestige.audio.AudioManager;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.projectiles.HealPickUp;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.statuseffects.HitBundle;
import com.lescomber.vestige.units.Boss;

public class OneFourBoss extends Boss
{
	public OneFourBoss()
	{
		super(600 + (350 * OptionsScreen.difficulty), 200 + (15 * OptionsScreen.difficulty));

		final SpinnyLaserScatter sls = new SpinnyLaserScatter(this, 10);
		sls.setCooldown(4);
		addAbility(sls);

		final Projectile basicShot = new Projectile(SpriteManager.enemyProjectile, 4, Projectile.ENEMY_PROJECTILE_WIDTH,
				Projectile.ENEMY_PROJECTILE_HEIGHT);
		basicShot.setUnitHitSound(AudioManager.enemyProjectileHit);
		basicShot.scale(0.6, 0.6);
		basicShot.setGlow(SpriteManager.redGlow);
		basicShot.setVelocityPerSecond(200 + (30 * OptionsScreen.difficulty));
		AIShooter basicShooter = new AIShooter(this, basicShot, 1.3);
		basicShooter.setCooldownRandomness(false);
		basicShooter.setUsesAnimation(false);
		addAbility(basicShooter);

		final float imageHp = 40 + 15 * OptionsScreen.difficulty;
		final MirrorImage mirrorImage = new MirrorImage(this, imageHp, 14);
		basicShooter = new AIShooter(this, basicShot, 1.3);
		basicShooter.setCooldownRandomness(false);
		basicShooter.setUsesAnimation(false);
		mirrorImage.addImageAbility(basicShooter);
		mirrorImage.setImagePickUp(new HealPickUp(15));
		addAbility(mirrorImage);

		addAbility(new ShieldMeteorShower(this, 18));
	}

	public OneFourBoss(OneFourBoss copyMe)
	{
		super(copyMe);
	}

	@Override
	protected void updateHealthBar()
	{
		if (getShields() > 0)
		{
			final float shieldPercentage = getShields() / ShieldMeteorShower.SHIELD_STRENGTH[OptionsScreen.difficulty];
			healthBar.setTexWidth(shieldPercentage);
		}
		else
			super.updateHealthBar();
	}

	@Override
	public void startAbility(AIAbility ability)
	{
		if (ability instanceof ShieldMeteorShower)
			createHealthBar(SpriteManager.hpBarBackground, SpriteManager.hpShieldHealth);

		super.startAbility(ability);
	}

	@Override
	public void hit(HitBundle bundle)
	{
		final boolean hadShields = getShields() > 0;

		super.hit(bundle);

		if (hadShields && getShields() <= 0)
		{
			createHealthBar(SpriteManager.hpBossBackground, SpriteManager.hpBossHealth);
			updateHealthBar();
		}
	}

	@Override
	public OneFourBoss copy()
	{
		return new OneFourBoss(this);
	}
}