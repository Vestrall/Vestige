package com.lescomber.vestige.playerabilities;

import com.lescomber.vestige.audio.AudioManager;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.statuseffects.StatPack;
import com.lescomber.vestige.statuseffects.StatusEffect;
import com.lescomber.vestige.units.Player;

public class SOneSwipe extends ProjectileShooter {
	private static final float DAMAGE = 12;

	public SOneSwipe(Player player) {
		super(player);

		final Projectile shot = new Projectile(SpriteManager.sOneSwipe, DAMAGE);
		shot.setUnitHitSound(AudioManager.sOneSwipeHit);
		shot.createRotatedRectHitbox(31, 19);
		shot.setImageOffsetX(-13);
		shot.setGlow(SpriteManager.smallGlow);

		shot.setVelocityPerSecond(1200);

		// Slow effect
		final StatPack sp = new StatPack();
		sp.moveSpeedPercent = 0.65;
		final StatusEffect se = new StatusEffect(sp, 1.75);
		shot.addStatusEffect(se);

		setPrototype(shot);

		setMaxCooldown(0.75);

		setSoundEffect(AudioManager.playerAttack);
	}
}