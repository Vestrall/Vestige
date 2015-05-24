package com.lescomber.vestige.units;

import com.lescomber.vestige.aiabilities.AIAbility;
import com.lescomber.vestige.aiabilities.AIShooter;
import com.lescomber.vestige.aiabilities.BeamShooter;
import com.lescomber.vestige.audio.AudioManager;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.projectiles.Explosion;
import com.lescomber.vestige.projectiles.HealPickUp;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.statuseffects.StatPack;

import java.util.ArrayList;

public class Caster extends AIRailUnit {
	private static final ArrayList<AIAbility> ABILITY_POOL = new ArrayList<AIAbility>(5);

	public Caster() {
		super(60, 40, -23, 30);

		// Init stats
		final StatPack baseStats = new StatPack();
		baseStats.maxHp = 50;
		baseStats.moveSpeed = 150;
		setDifficultyScaledBaseStats(baseStats);

		// Init idle sprites
		setIdleLeftSprite(SpriteManager.casterWalkLeft[0]);
		setIdleRightSprite(SpriteManager.casterWalkRight[0]);

		// Init walking animations
		final SpriteAnimation walkLeftAnim = new SpriteAnimation(SpriteManager.casterWalkLeft);
		setWalkLeftAnimation(walkLeftAnim);
		final SpriteAnimation walkRightAnim = new SpriteAnimation(SpriteManager.casterWalkRight);
		setWalkRightAnimation(walkRightAnim);

		// Init firingLeft animations
		final SpriteAnimation preFiringLeftAnim = new SpriteAnimation(SpriteManager.casterAttackLeft, 0, 3);
		setPreFiringLeftAnimation(preFiringLeftAnim);
		final SpriteAnimation postFiringLeftAnim = new SpriteAnimation(SpriteManager.casterAttackLeft, 4, 6);
		setPostFiringLeftAnimation(postFiringLeftAnim);

		// Init firingRight animations
		final SpriteAnimation preFiringRightAnim = new SpriteAnimation(SpriteManager.casterAttackRight, 0, 3);
		setPreFiringRightAnimation(preFiringRightAnim);
		final SpriteAnimation postFiringRightAnim = new SpriteAnimation(SpriteManager.casterAttackRight, 4, 6);
		setPostFiringRightAnimation(postFiringRightAnim);

		// Init channel animations
		final SpriteAnimation channelLeftAnim = new SpriteAnimation(SpriteManager.casterAttackLeft, 3, 3);
		setChannelingLeftAnimation(channelLeftAnim);
		final SpriteAnimation channelRightAnim = new SpriteAnimation(SpriteManager.casterAttackRight, 3, 3);
		setChannelingRightAnimation(channelRightAnim);

		// Init death animations
		final SpriteAnimation deathLeftAnim = new SpriteAnimation(SpriteManager.casterDeathLeft);
		for (int i = 0; i < 13; i++)
			deathLeftAnim.addFrame(SpriteManager.casterDeathLeft[11]);
		setDeathAnimationLeft(deathLeftAnim);
		final SpriteAnimation deathRightAnim = new SpriteAnimation(SpriteManager.casterDeathRight);
		for (int i = 0; i < 13; i++)
			deathRightAnim.addFrame(SpriteManager.casterDeathRight[11]);
		setDeathAnimationRight(deathRightAnim);
		setDeathAnimXOffset(4);

		// Set firing offsets
		setFiringOffsets(41, 1.5f);

		// Note: ABILITY_POOL is rebuilt every time a new Caster is created in order to make sure the abilities are appropriately scaled for the
		//current difficulty. ABILITY_POOL is not rebuilt when copy() is used to create a new Caster
		ABILITY_POOL.clear();

		final Projectile enemyShot = new Projectile(SpriteManager.enemyProjectile, 8, Projectile.ENEMY_PROJECTILE_WIDTH, Projectile
				.ENEMY_PROJECTILE_HEIGHT);
		enemyShot.setUnitHitSound(AudioManager.enemyProjectileHit);
		enemyShot.setGlow(SpriteManager.redGlow);
		final AIShooter enemyShotShooter = new AIShooter(this, enemyShot, 3);
		enemyShotShooter.scaleForDifficulty();
		ABILITY_POOL.add(enemyShotShooter);

		final Projectile explosiveShot = new Projectile(null, 0, 8);
		final SpriteAnimation anim = new SpriteAnimation(SpriteManager.plasmaBall);
		anim.setSequenceLimit(-1);
		explosiveShot.setImage(anim);
		explosiveShot.setGlow(SpriteManager.purpleGlow);
		explosiveShot.setExplosion(new Explosion(50, 10));
		explosiveShot.setExplosionSound(AudioManager.purpleExplosion);
		final AIShooter explosiveShotShooter = new AIShooter(this, explosiveShot, 5);
		explosiveShotShooter.scaleForDifficulty();
		explosiveShotShooter.setIsExtended(false);
		ABILITY_POOL.add(explosiveShotShooter);

		final BeamShooter beamShooter = new BeamShooter(this, 7);
		beamShooter.scaleForDifficulty();
		ABILITY_POOL.add(beamShooter);

		// Randomly select abilities from ABILITY_POOL
		selectRandomAbilities(2);
	}

	public Caster(float healAmount) {
		this();

		setPickUp(new HealPickUp(healAmount));
	}

	public Caster(Caster copyMe) {
		super(copyMe);

		selectRandomAbilities(2);
	}

	/**
	 * Randomly selects abilityCount abilities to keep from its current list of abilities and discards any other abilities
	 */
	public void selectRandomAbilities(int abilityCount) {
		if (abilityCount <= 0 || abilityCount > ABILITY_POOL.size())
			return;

		// Clear any existing abilities (in case we just got copied)
		clearAbilities();

		// Randomly choose abilityCount indices
		final ArrayList<Integer> indices = new ArrayList<Integer>(abilityCount);
		while (indices.size() < abilityCount) {
			final int newIndex = Util.rand.nextInt(ABILITY_POOL.size());
			if (!indices.contains(newIndex))
				indices.add(newIndex);
		}

		// Retrieve ABILITY_POOL abilities based on the randomly chosen indices
		for (final int i : indices) {
			final AIAbility aia = ABILITY_POOL.get(i).copy();
			aia.setOwner(this);
			aia.triggerCooldown();    // Used to trigger cooldown randomness so Casters don't sync up on their first attacks
			addAbility(aia);
		}
	}

	@Override
	public Caster copy() {
		return new Caster(this);
	}
}