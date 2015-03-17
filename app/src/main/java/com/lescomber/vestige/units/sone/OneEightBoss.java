package com.lescomber.vestige.units.sone;

import com.lescomber.vestige.aiabilities.AIUnitSpawner;
import com.lescomber.vestige.aiabilities.sone.FireDash;
import com.lescomber.vestige.aiabilities.sone.TimeBombScatter;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.geometry.Hitbox;
import com.lescomber.vestige.geometry.Rectangle;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.units.Boss;

public class OneEightBoss extends Boss
{
	public OneEightBoss()
	{
		super(450 + (200 * OptionsScreen.difficulty), 100 + (15 * OptionsScreen.difficulty));

		final TimeBombScatter tbs = new TimeBombScatter(this, 6);
		tbs.setCooldownRandomness(false);
		addAbility(tbs);

		final SpawnPortal prototype = new SpawnPortal();
		final AIUnitSpawner podSpawner = new AIUnitSpawner(this, 20, prototype);
		podSpawner.setCooldown(12);
		podSpawner.setDestinationBox(new Hitbox(new Rectangle(5, 25, Screen.WIDTH - 5, Screen.HEIGHT - 5)));
		podSpawner.setCopiesPerSpawn(2);
		podSpawner.setCooldownRandomness(false);
		addAbility(podSpawner);

		final FireDash fireDash = new FireDash(this, 1.3);
		fireDash.scaleForDifficulty();
		addAbility(fireDash);

		scale(0.65, 0.65);
	}
}