package com.lescomber.vestige.units.sone;

import com.lescomber.vestige.aiabilities.sone.MultiCreepingFireShooter;
import com.lescomber.vestige.aiabilities.sone.PerpetualPortalSpawner;
import com.lescomber.vestige.aiabilities.sone.RoamingExploderShooter;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.units.Boss;

public class OneTenBoss extends Boss {
	private final MultiCreepingFireShooter fireShooter;

	public OneTenBoss() {
		super(700 + (200 * OptionsScreen.difficulty), 100 + (20 * OptionsScreen.difficulty));

		fireShooter = new MultiCreepingFireShooter(this, 9.5);
		fireShooter.setCooldownRandomness(false);
		fireShooter.setCooldown(7.4);
		addAbility(fireShooter);

		final RoamingExploderShooter exploderShooter = new RoamingExploderShooter(this);
		exploderShooter.setCooldownRandomness(false);
		exploderShooter.setCooldown(0);
		addAbility(exploderShooter);

		final PerpetualPortalSpawner portalSpawner = new PerpetualPortalSpawner(this, 22);
		portalSpawner.setCooldownRandomness(false);
		portalSpawner.setCooldown(10);
		addAbility(portalSpawner);
	}

	public void cooldownSync() {
		fireShooter.setCooldown(5);
	}
}