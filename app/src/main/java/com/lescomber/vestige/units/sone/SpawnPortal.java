package com.lescomber.vestige.units.sone;

import com.lescomber.vestige.Options;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.statuseffects.StatPack;
import com.lescomber.vestige.statuseffects.StatusEffect;
import com.lescomber.vestige.units.AIUnit;
import com.lescomber.vestige.units.FloatingCreep;

public class SpawnPortal extends AIUnit {
	private static final int[] GROWTH_INTERVAL = new int[] { 180, 145, 110 };
	private static final int SIZE_MAX = 100;
	private static final float SPAWN_DISTANCE = 70;

	private int size;
	private int growthCooldown;
	private double curSize;
	private final StatusEffect growthPrototype;

	private final int portalInitAnim;
	private final int portalSpawnAnim;
	private final int portalOpenAnim;
	private final int portalUnitSpawnAnim;
	private boolean allSpawned;

	public SpawnPortal() {
		super(GameScreen.steves, 48, 34, -22, 25);

		setIdleLeftSprite(SpriteManager.spawnPortalSpawn[0]);

		// Init stats
		final StatPack baseStats = new StatPack();
		baseStats.maxHp = 36 + (Options.difficulty * 5);
		baseStats.moveSpeed = 350 + (Options.difficulty * 25);
		setBaseStats(baseStats);

		setSlowable(false);

		// Init portal animations
		portalInitAnim = addAnimation(new SpriteAnimation(SpriteManager.spawnPortalSpawn, 0, 10));
		SpriteAnimation anim = new SpriteAnimation(SpriteManager.spawnPortalSpawn, 11, 13);
		anim.setSequenceLimit(-1);
		portalSpawnAnim = addAnimation(anim);
		portalOpenAnim = addAnimation(new SpriteAnimation(SpriteManager.spawnPortalOpen));
		anim = new SpriteAnimation(SpriteManager.spawnPortalOpen, 8, 11);
		anim.setDuration(200);
		portalUnitSpawnAnim = addAnimation(anim);

		allSpawned = false;

		size = 0;
		growthCooldown = GROWTH_INTERVAL[Options.difficulty];
		curSize = 100;
		final StatPack sp = new StatPack();
		sp.maxHp = 0.3f * (1 + Options.difficulty);
		growthPrototype = new StatusEffect(sp, 1000);
		growthPrototype.setStacks(1, 100, 0);

		scale(0.7, 0.7);
	}

	public SpawnPortal(SpawnPortal copyMe) {
		super(copyMe);

		portalInitAnim = copyMe.portalInitAnim;
		portalSpawnAnim = copyMe.portalSpawnAnim;
		portalOpenAnim = copyMe.portalOpenAnim;
		portalUnitSpawnAnim = copyMe.portalUnitSpawnAnim;
		allSpawned = copyMe.allSpawned;
		size = copyMe.size;
		growthCooldown = copyMe.growthCooldown;
		curSize = copyMe.curSize;
		growthPrototype = new StatusEffect(copyMe.growthPrototype);
	}

	@Override
	public void update(int deltaTime) {
		super.update(deltaTime);

		growthCooldown -= deltaTime;

		while (growthCooldown <= 0) {
			growthCooldown += GROWTH_INTERVAL[Options.difficulty];

			size++;
			if (size == SIZE_MAX)
				playAnimation(portalOpenAnim);
			else if (size < SIZE_MAX) {
				// Increase size of animation/hitbox
				final double scale = (curSize + 1.2) / curSize;
				scale(scale, scale);
				curSize += 1.2;

				// Increase maxHp
				addStatusEffect(growthPrototype.copy());
			}
		}
	}

	@Override
	protected void animationFinished(int animID) {
		if (animID == portalInitAnim) {
			playAnimation(portalSpawnAnim);
		} else if (animID == portalOpenAnim) {
			spawnUnit();

			// Begin unitSpawning animation which will trigger the 2nd unit to spawn once it has completed its sequence
			playAnimation(portalUnitSpawnAnim);
		} else if (animID == portalUnitSpawnAnim) {
			if (!allSpawned) {
				spawnUnit();

				// Restart portalUnitSpawnAnim one more time so this last unit has a portal to walk through
				getAnimation(portalUnitSpawnAnim).setDuration(200);
				restartAnimation(portalUnitSpawnAnim);

				allSpawned = true;
			} else
				die();
		}
	}

	private void spawnUnit() {
		final FloatingCreep creep = new FloatingCreep();
		creep.offsetTo(getImageCenter());
		creep.setDestination(getImageCenter().x, getImageCenter().y + SPAWN_DISTANCE);
		creep.setEntering(true);
		queueAIUnit(creep);
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
	public SpawnPortal copy() {
		return new SpawnPortal(this);
	}
}