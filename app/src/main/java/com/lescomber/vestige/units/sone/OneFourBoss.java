package com.lescomber.vestige.units.sone;

import com.lescomber.vestige.Options;
import com.lescomber.vestige.aiabilities.AIShooter;
import com.lescomber.vestige.aiabilities.sone.MirrorImage;
import com.lescomber.vestige.aiabilities.sone.ShieldMeteorShower;
import com.lescomber.vestige.aiabilities.sone.SpinnyLaserScatter;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.AudioManager;
import com.lescomber.vestige.projectiles.HealPickUp;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.units.Boss;

public class OneFourBoss extends Boss {
	public OneFourBoss() {
		super(600 + (350 * Options.difficulty), 200 + (15 * Options.difficulty));

		final SpinnyLaserScatter spinnyLaserScatter = new SpinnyLaserScatter(this, 10);
		spinnyLaserScatter.setCooldown(4);
		addAbility(spinnyLaserScatter);

		final Projectile basicShot = new Projectile(SpriteManager.enemyProjectile, 4, Projectile.ENEMY_PROJECTILE_WIDTH, Projectile
				.ENEMY_PROJECTILE_HEIGHT);
		basicShot.setUnitHitSound(AudioManager.enemyProjectileHit);
		basicShot.scale(0.6, 0.6);
		basicShot.setGlow(SpriteManager.redGlow);
		basicShot.setVelocityPerSecond(200 + (30 * Options.difficulty));
		AIShooter basicShooter = new AIShooter(this, basicShot, 1.3);
		basicShooter.setCooldownRandomness(false);
		basicShooter.setUsesAnimation(false);
		addAbility(basicShooter);

		final float imageHp = 40 + 15 * Options.difficulty;
		final MirrorImage mirrorImage = new MirrorImage(this, imageHp, 14);
		basicShooter = new AIShooter(this, basicShot, 1.3);
		basicShooter.setCooldownRandomness(false);
		basicShooter.setUsesAnimation(false);
		mirrorImage.addImageAbility(basicShooter);
		mirrorImage.setImagePickUp(new HealPickUp(15));
		addAbility(mirrorImage);

		addAbility(new ShieldMeteorShower(this, 18));
	}

	public OneFourBoss(OneFourBoss copyMe) {
		super(copyMe);
	}

	@Override
	public OneFourBoss copy() {
		return new OneFourBoss(this);
	}
}