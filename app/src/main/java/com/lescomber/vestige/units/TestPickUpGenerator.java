package com.lescomber.vestige.units;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.graphics.Sprite;
import com.lescomber.vestige.projectiles.HealPickUp;
import com.lescomber.vestige.projectiles.PickUp;
import com.lescomber.vestige.statuseffects.HitBundle;
import com.lescomber.vestige.statuseffects.StatPack;

public class TestPickUpGenerator extends AIUnit {
	private PickUp testHealthPickUp;

	public TestPickUpGenerator() {
		super(32, 32, 0, 0);

		final StatPack baseStats = new StatPack();
		baseStats.maxHp = 10000;
		setBaseStats(baseStats);

		setImage(new Sprite(SpriteManager.healthPickUp));

		testHealthPickUp = new HealPickUp(15);
	}

	public TestPickUpGenerator(TestPickUpGenerator copyMe) {
		super(copyMe);
	}

	@Override
	public void hit(HitBundle bundle) {
		super.hit(bundle);

		final PickUp newHealthPickUp = new PickUp(testHealthPickUp);
		newHealthPickUp.offsetTo(new Point(Util.rand.nextFloat() * 780 + 10, Util.rand.nextFloat() * 460 + 10));
		queueAreaEffect(newHealthPickUp);
	}

	@Override
	public AIUnit copy() {
		return new TestPickUpGenerator(this);
	}
}