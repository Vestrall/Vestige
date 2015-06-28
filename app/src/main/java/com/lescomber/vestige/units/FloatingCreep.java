package com.lescomber.vestige.units;

import com.lescomber.vestige.aiabilities.AIMeleeAttack;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.AudioManager;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.projectiles.HealPickUp;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.statuseffects.StatPack;

public class FloatingCreep extends AIMeleeUnit {
	public FloatingCreep() {
		super(GameScreen.steves, 32, 28, -16, 17);

		final StatPack baseStats = new StatPack();
		baseStats.maxHp = 30;
		baseStats.moveSpeed = 170;
		setDifficultyScaledBaseStats(baseStats);

		// Init idle sprites
		setIdleLeftSprite(SpriteManager.floatingCreepWalkLeft[0]);
		setIdleRightSprite(SpriteManager.floatingCreepWalkRight[0]);

		// Init walking animations
		final SpriteAnimation walkLeftAnim = new SpriteAnimation(SpriteManager.floatingCreepWalkLeft);
		setWalkLeftAnimation(walkLeftAnim);
		final SpriteAnimation walkRightAnim = new SpriteAnimation(SpriteManager.floatingCreepWalkRight);
		setWalkRightAnimation(walkRightAnim);

		// Init attacking animations
		final SpriteAnimation preFiringLeftAnim = new SpriteAnimation(SpriteManager.floatingCreepAttackLeft);
		setPreFiringLeftAnimation(preFiringLeftAnim);
		final SpriteAnimation preFiringRightAnim = new SpriteAnimation(SpriteManager.floatingCreepAttackRight);
		setPreFiringRightAnimation(preFiringRightAnim);

		// Init death animations
		setDeathAnimationLeft(new SpriteAnimation(SpriteManager.floatingCreepDeathLeft));
		setDeathAnimationRight(new SpriteAnimation(SpriteManager.floatingCreepDeathRight));
		setDeathAnimXOffset(8);

		// Init abilities
		final AIMeleeAttack melee = new AIMeleeAttack(this, 10, 2);
		melee.scaleForDifficulty();
		melee.setHitSound(AudioManager.floatingCreepHit);
		addAbility(melee);
	}

	public FloatingCreep(float healAmount) {
		this();

		setPickUp(new HealPickUp(healAmount));
	}

	public FloatingCreep(FloatingCreep copyMe) {
		super(copyMe);
	}

	@Override
	public FloatingCreep copy() {
		return new FloatingCreep(this);
	}
}