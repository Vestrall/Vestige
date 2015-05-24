package com.lescomber.vestige.playerabilities;

import com.lescomber.vestige.audio.AudioManager;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.units.Player;

public class SOneChargeSwipe extends ProjectileShooter {
	private static final float DAMAGE = 30;

	public SOneChargeSwipe(Player player) {
		super(player);

		final Projectile shot = new Projectile(SpriteManager.sOneChargeSwipe, DAMAGE);
		shot.setUnitHitSound(AudioManager.sOneChargeSwipeHit);
		shot.createRotatedRectHitbox(35, 74);
		shot.setImageOffsetX(-6);
		shot.setGlow(SpriteManager.bigGlow);
		shot.setVelocityPerSecond(900);
		setMaxCooldown(8);

		shot.setUnitPassThrough(true);
		shot.setDamageReductionOnHit(0.125, 0.5);
		setPrototype(shot);

		setCDIndicator(SpriteManager.cdArcFull, SpriteManager.cdArcEmpty);

		setSoundEffect(AudioManager.playerAttack);
	}
}