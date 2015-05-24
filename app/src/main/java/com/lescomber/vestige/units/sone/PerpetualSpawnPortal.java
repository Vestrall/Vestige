package com.lescomber.vestige.units.sone;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.statuseffects.StatPack;
import com.lescomber.vestige.units.AIUnit;
import com.lescomber.vestige.units.Caster;
import com.lescomber.vestige.units.FloatingCreep;

public class PerpetualSpawnPortal extends AIUnit {
	private static final float SPAWN_DISTANCE = 70;

	private final int portalInitAnim;
	private int portalUnitSpawnAnim;

	private static final int SPAWN_INTERVAL = 11000;
	private static final int INITIAL_COUNTDOWN = 500;
	private int countdown;

	private int spawnCount;

	private static Caster PROTOTYPE_CASTER;

	public PerpetualSpawnPortal(float x, float y) {
		super(48, 26, -22, 25);

		offsetTo(x, y);

		// Init stats
		final StatPack baseStats = new StatPack();
		baseStats.maxHp = 45;
		baseStats.moveSpeed = 0;
		setBaseStats(baseStats);

		SpriteAnimation anim = new SpriteAnimation(SpriteManager.spawnPortalSpawn, 0, 13);
		anim.addFrames(SpriteManager.spawnPortalOpen);
		anim.setHoldLastFrame(true);
		portalInitAnim = addAnimation(anim);
		playAnimation(portalInitAnim);

		anim = new SpriteAnimation(SpriteManager.spawnPortalOpen, 8, 11);
		anim.setSequenceLimit(-1);
		portalUnitSpawnAnim = addAnimation(anim);

		scale(1.5, 1.5);

		countdown = 0;

		spawnCount = 0;

		if (PROTOTYPE_CASTER == null) {
			PROTOTYPE_CASTER = new Caster();
			PROTOTYPE_CASTER.createRailLocations(40);
		}
	}

	public PerpetualSpawnPortal(PerpetualSpawnPortal copyMe) {
		super(copyMe);

		portalInitAnim = copyMe.portalInitAnim;
		countdown = copyMe.countdown;
		spawnCount = copyMe.spawnCount;
	}

	@Override
	public void update(int deltaTime) {
		super.update(deltaTime);

		if (getCurrentAnimID() != portalInitAnim) {
			countdown -= deltaTime;
			if (countdown <= 0) {
				countdown += SPAWN_INTERVAL;
				spawnUnit();
			}
		}
	}

	@Override
	protected void animationFinished(int animID) {
		if (animID == portalInitAnim) {
			countdown = INITIAL_COUNTDOWN;
			playAnimation(portalUnitSpawnAnim);
		} else if (animID == portalUnitSpawnAnim) {
			die();
		}
	}

	private void spawnUnit() {
		if (spawnCount <= 0) {
			final FloatingCreep creep = new FloatingCreep();
			creep.offsetTo(getImageCenter());
			creep.setDestination(getImageCenter().x, getImageCenter().y + SPAWN_DISTANCE);
			creep.setEntering(true);
			queueAIUnit(creep);
			spawnCount++;
		} else {
			final Caster caster = PROTOTYPE_CASTER.copy();
			caster.offsetTo(getImageCenter());
			caster.setDestination(getImageCenter().x, getImageCenter().y + SPAWN_DISTANCE);
			caster.setEntering(true);
			queueAIUnit(caster);

			// Restart portalUnitSpawnAnim one more time so this last unit has a portal to walk through
			getAnimation(portalUnitSpawnAnim).setDuration(200);
			restartAnimation(portalUnitSpawnAnim);
		}
	}

	@Override
	protected void pathDestinationReached() {
		playAnimation(portalInitAnim);
	}

	@Override
	public void die() {
		super.die();

		final SpriteAnimation anim = new SpriteAnimation(SpriteManager.spawnPortalEnd);
		anim.offsetTo(getCenter().x, getCenter().y + getImageOffsetY());
		GameScreen.playAnimation(anim);
	}

	@Override
	public PerpetualSpawnPortal copy() {
		return new PerpetualSpawnPortal(this);
	}
}