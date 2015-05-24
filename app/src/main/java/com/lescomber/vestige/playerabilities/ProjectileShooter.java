package com.lescomber.vestige.playerabilities;

import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.units.Player;

public class ProjectileShooter extends SwipeAbility {
	private Projectile prototype;

	public ProjectileShooter(Player player, Projectile prototype, double cooldownSeconds) {
		super(player);

		setMaxCooldown(cooldownSeconds);

		this.prototype = prototype.copy();
		this.prototype.setImageOffsetY(player.getFiringOffsetY());
	}

	public ProjectileShooter(Player player) {
		super(player);

		prototype = null;
		setMaxCooldown(0);
	}

	public ProjectileShooter(ProjectileShooter copyMe) {
		super(copyMe);

		prototype = copyMe.prototype.copy();
	}

	public void setPrototype(Projectile prototype) {
		this.prototype = prototype.copy();
		this.prototype.setImageOffsetY(player.getFiringOffsetY());
	}

	@Override
	public Projectile prepare(Line swipe, float firingOffsetY) {
		final Projectile shot = prototype.copy();
		shot.setDestination(swipe.getExtEnd());
		shot.rotateTo(swipe.getDirection());
		shot.setTargets(GameScreen.steves);

		return shot;
	}

	@Override
	public ProjectileShooter copy() {
		return new ProjectileShooter(this);
	}
}