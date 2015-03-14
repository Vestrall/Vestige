package com.lescomber.vestige.units.sone;

import com.lescomber.vestige.aiabilities.AIAbility;
import com.lescomber.vestige.aiabilities.AIShooter;
import com.lescomber.vestige.aiabilities.DummyAbility;
import com.lescomber.vestige.aiabilities.sone.Charge;
import com.lescomber.vestige.aiabilities.sone.CreepingFireShooter;
import com.lescomber.vestige.aiabilities.sone.MovingBeamShooter;
import com.lescomber.vestige.aiabilities.sone.MultiShooter;
import com.lescomber.vestige.audio.AudioManager;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.graphics.Sprite;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.projectiles.HitGroup;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.projectiles.sone.Seeker;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.units.Boss;

import java.util.ArrayList;

public class OneSixBoss extends Boss
{
	private static final double BOSS_PHASE_SECONDS = 12;
	private static final double CASTER_PHASE_SECONDS = 16;
	
	private boolean isBossState;
	private final boolean isFiringShapeShift;
	
	// Charge related fields
	private final int preChargeLeft;
	private final int preChargeRight;
	private final int successfulChargeLeft;
	private final int successfulChargeRight;
	private final int unsuccessfulChargeLeft;
	private final int unsuccessfulChargeRight;
	private boolean chargeLeft;		// Remembers the direction we were facing when we started the charge
	private Charge charge;
	
	// Anim IDs for all the boss animations in the following order (with the left-facing version coming before right-facing):
	//walking, preFiring, preChanneling, channeling, postChanneling, postFiring
	private final int bossAnims[];
	private Sprite bossIdleLeft;
	private Sprite bossIdleRight;
	
	// Anim IDs for caster animations in the following order (with the left-facing version coming before right-facing):
	//walking, preFiring, channeling, postFiring
	private final int casterAnims[];
	private final Sprite casterIdleLeft;
	private final Sprite casterIdleRight;
	private final int casterDeathLeftAnim;
	private final int casterDeathRightAnim;
	private static final float CASTER_SCALE = 1.5f;
	
	private final HitGroup creepingFireHitGroup;
	private final ArrayList<AIAbility> bossAbilities;
	private final ArrayList<AIAbility> casterAbilities;
	
	private final DummyAbility trigger;
	
	public OneSixBoss()
	{
		super(850 + (350 * OptionsScreen.difficulty), 160 + (10 * OptionsScreen.difficulty));
		
		isBossState = true;
		isFiringShapeShift = false;
		
		// Retrieve boss anim IDs / sprites
		bossAnims = new int[12];
		bossAnims[0] = getWalkingLeftIndex();
		bossAnims[1] = getWalkingRightIndex();
		bossAnims[2] = getPreFiringLeftIndex();
		bossAnims[3] = getPreFiringRightIndex();
		bossAnims[4] = getPreChannelingLeftIndex();
		bossAnims[5] = getPreChannelingRightIndex();
		bossAnims[6] = getChannelingLeftIndex();
		bossAnims[7] = getChannelingRightIndex();
		bossAnims[8] = getPostChannelingLeftIndex();
		bossAnims[9] = getPostChannelingRightIndex();
		bossAnims[10] = getPostFiringLeftIndex();
		bossAnims[11] = getPostFiringRightIndex();
		bossIdleLeft = new Sprite(SpriteManager.bossWalkLeft[0]);
		bossIdleRight = new Sprite(SpriteManager.bossWalkRight[0]);
		
		// Create/store caster anim IDs / sprites
		casterAnims = new int[8];
		SpriteAnimation anim = new SpriteAnimation(SpriteManager.casterWalkLeft);
		anim.setSequenceLimit(-1);
		casterAnims[0] = addAnimation(anim);
		anim = new SpriteAnimation(SpriteManager.casterWalkRight);
		anim.setSequenceLimit(-1);
		casterAnims[1] = addAnimation(anim);
		anim = new SpriteAnimation(SpriteManager.casterFiringLeft, 0, 3);
		anim.setFrameTime(55);
		casterAnims[2] = addAnimation(anim);
		anim = new SpriteAnimation(SpriteManager.casterFiringRight, 0, 3);
		anim.setFrameTime(55);
		casterAnims[3] = addAnimation(anim);
		anim = new SpriteAnimation(SpriteManager.casterFiringLeft, 3, 3);
		anim.setFrameTime(55);
		casterAnims[4] = addAnimation(anim);
		anim = new SpriteAnimation(SpriteManager.casterFiringRight, 3, 3);
		anim.setFrameTime(55);
		casterAnims[5] = addAnimation(anim);
		anim = new SpriteAnimation(SpriteManager.casterFiringLeft, 4, 6);
		anim.setFrameTime(55);
		casterAnims[6] = addAnimation(anim);
		anim = new SpriteAnimation(SpriteManager.casterFiringRight, 4, 6);
		anim.setFrameTime(55);
		casterAnims[7] = addAnimation(anim);
		
		casterIdleLeft = new Sprite(SpriteManager.casterWalkLeft[0]);
		casterIdleRight = new Sprite(SpriteManager.casterWalkRight[0]);
		
		// Caster death animations
		anim = new SpriteAnimation(SpriteManager.casterDeathLeft);
		for (int i=0; i<13; i++)
			anim.addFrame(SpriteManager.casterDeathLeft[11]);
		casterDeathLeftAnim = addAnimation(anim);
		anim = new SpriteAnimation(SpriteManager.casterDeathRight);
		for (int i=0; i<13; i++)
			anim.addFrame(SpriteManager.casterDeathRight[11]);
		casterDeathRightAnim = addAnimation(anim);
		
		// Create charge-specific animations
		anim = new SpriteAnimation(SpriteManager.bossFiringLeft, 0, 5);
		anim.setHoldLastFrame(true);
		preChargeLeft = addAnimation(anim);
		anim = new SpriteAnimation(SpriteManager.bossFiringRight, 0, 5);
		anim.setHoldLastFrame(true);
		preChargeRight = addAnimation(anim);
		anim = new SpriteAnimation(SpriteManager.bossFiringLeft, 6, 16);
		anim.setHoldLastFrame(true);
		successfulChargeLeft = addAnimation(anim);
		anim = new SpriteAnimation(SpriteManager.bossFiringRight, 6, 16);
		anim.setHoldLastFrame(true);
		successfulChargeRight = addAnimation(anim);
		
		anim = new SpriteAnimation();
		for (int i=4; i>=0; i--)
			anim.addFrame(SpriteManager.bossFiringLeft[i]);
		anim.setHoldLastFrame(true);
		unsuccessfulChargeLeft = addAnimation(anim);
		anim = new SpriteAnimation();
		for (int i=4; i>=0; i--)
			anim.addFrame(SpriteManager.bossFiringRight[i]);
		anim.setHoldLastFrame(true);
		unsuccessfulChargeRight = addAnimation(anim);
		
		chargeLeft = true;
		
		// Set firing offsets (for caster phase). They are scaled by the same amount as the caster animations
		setFiringOffsets(41 * CASTER_SCALE, 1.5f * CASTER_SCALE);
		
		// Scale caster images
		for (int i=0; i<8; i++)
			getAnimation(casterAnims[i]).scale(CASTER_SCALE, CASTER_SCALE);
		casterIdleLeft.scale(CASTER_SCALE, CASTER_SCALE);
		casterIdleRight.scale(CASTER_SCALE, CASTER_SCALE);
		getAnimation(casterDeathLeftAnim).scale(CASTER_SCALE, CASTER_SCALE);
		getAnimation(casterDeathRightAnim).scale(CASTER_SCALE, CASTER_SCALE);
		
		// Init shape shift ability
		trigger = new DummyAbility(this, 16);
		trigger.scaleForDifficulty();
		addAbility(trigger);
		
		// Init boss abilities
		bossAbilities = new ArrayList<AIAbility>(5);
		
		charge = new Charge(this, 4);
		charge.scaleForDifficulty();
		bossAbilities.add(charge);
		
		creepingFireHitGroup = new HitGroup();
		bossAbilities.add(new CreepingFireShooter(this, 3, creepingFireHitGroup));
		
		// Add boss abilities since we start in boss form
		for (final AIAbility aia : bossAbilities)
			addAbility(aia);
		
		// Init caster abilities
		casterAbilities = new ArrayList<AIAbility>(5);
		
		final MovingBeamShooter movingBeamShooter = new MovingBeamShooter(this, 9);
		movingBeamShooter.scaleForDifficulty();
		casterAbilities.add(movingBeamShooter);
		
		final AIShooter seekerShooter = new AIShooter(this, new Seeker(), 5);
		seekerShooter.scaleForDifficulty();
		casterAbilities.add(seekerShooter);
		
		final Projectile prototype = new Projectile(SpriteManager.enemyProjectile, 7,
				Projectile.ENEMY_PROJECTILE_WIDTH, Projectile.ENEMY_PROJECTILE_HEIGHT);
		prototype.setUnitHitSound(AudioManager.enemyProjectileHit);
		prototype.setGlow(SpriteManager.redGlow);
		prototype.setVelocityPerSecond(300);		// Slow prototype down just a little (default is 350)
		final MultiShooter multiShooter = new MultiShooter(this, prototype, 3);
		multiShooter.scaleForDifficulty();
		casterAbilities.add(multiShooter);
	}
	
	public OneSixBoss(OneSixBoss copyMe)
	{
		super(copyMe);
		
		isBossState = copyMe.isBossState;
		isFiringShapeShift = copyMe.isFiringShapeShift;
		
		preChargeLeft = copyMe.preChargeLeft;
		preChargeRight = copyMe.preChargeRight;
		successfulChargeLeft = copyMe.successfulChargeLeft;
		successfulChargeRight = copyMe.successfulChargeRight;
		unsuccessfulChargeLeft = copyMe.unsuccessfulChargeLeft;
		unsuccessfulChargeRight = copyMe.unsuccessfulChargeRight;
		chargeLeft = copyMe.chargeLeft;
		
		bossAnims = new int[12];
		for (int i=0; i<12; i++)
			bossAnims[i] = copyMe.bossAnims[i];
		casterAnims = new int[8];
		for (int i=0; i<8; i++)
			casterAnims[i] = copyMe.casterAnims[i];
		casterIdleLeft = copyMe.casterIdleLeft.copy();
		casterIdleRight = copyMe.casterIdleRight.copy();
		casterDeathLeftAnim = copyMe.casterDeathLeftAnim;
		casterDeathRightAnim = copyMe.casterDeathRightAnim;
		
		bossAbilities = new ArrayList<AIAbility>(5);
		creepingFireHitGroup = new HitGroup();
		for (final AIAbility aia : copyMe.bossAbilities)
		{
			final AIAbility newAbility = aia.copy();
			newAbility.setOwner(this);
			if (newAbility instanceof Charge)
				charge = (Charge)newAbility;
			else if (newAbility instanceof CreepingFireShooter)
				((CreepingFireShooter)newAbility).setFireGroup(creepingFireHitGroup);
			bossAbilities.add(newAbility);
		}
		
		casterAbilities = new ArrayList<AIAbility>(5);
		for (final AIAbility aia : copyMe.casterAbilities)
		{
			final AIAbility newAbility = aia.copy();
			newAbility.setOwner(this);
			casterAbilities.add(newAbility);
		}
		
		clearAbilities();
		trigger = copyMe.trigger.copy();
		trigger.setOwner(this);
		addAbility(trigger);
		
		for (final AIAbility aia : isBossState ? bossAbilities : casterAbilities)
			addAbility(aia);
	}
	
	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);
		
		creepingFireHitGroup.update(deltaTime);
	}
	
	@Override
	public void startAbility(AIAbility ability)
	{
		if (ability instanceof Charge)
		{
			charge = (Charge)ability;
			
			if (charge.isTargetLeft())
			{
				setFiring(true);
				restartAnimation(preChargeLeft);
			}
			else
			{
				setFiring(true);
				restartAnimation(preChargeRight);
			}
		}
		else
			super.startAbility(ability);
	}
	
	@Override
	protected void fire(AIAbility ability)
	{
		super.fire(ability);
		
		if (ability == trigger)
		{
			if (isBossState)
			{
				shiftToCaster();
				trigger.setCooldown(CASTER_PHASE_SECONDS * (1.0 + (0.2 * Util.rand.nextDouble())));	// Reduce trigger cooldown for boss phase
			}
			else
			{
				shiftToBoss();
				trigger.setCooldown(BOSS_PHASE_SECONDS * (1.0 + (0.2 * Util.rand.nextDouble())));	// Reduce trigger cooldown for boss phase
			}
		}
	}
	
	@Override
	protected void animationFinished(int animIndex)
	{
		if (animIndex == preChargeLeft)
		{
			if (charge.isTargetLeft())
			{
				setIdleLeftSprite(SpriteManager.bossFiringLeft[5]);
				chargeLeft = true;
			}
			else
			{
				setIdleRightSprite(SpriteManager.bossFiringRight[5]);
				chargeLeft = false;
			}
			
			charge.fire();
		}
		else if (animIndex == preChargeRight)
		{
			if (charge.isTargetLeft())
			{
				chargeLeft = true;
				setIdleLeftSprite(SpriteManager.bossFiringLeft[5]);
			}
			else
			{
				chargeLeft = false;
				setIdleRightSprite(SpriteManager.bossFiringRight[5]);
			}
			
			charge.fire();
		}
		else if (animIndex == successfulChargeLeft || animIndex == successfulChargeRight)
		{
			charge.strike();
			finishedFiring();
		}
		else if (animIndex == unsuccessfulChargeLeft || animIndex == unsuccessfulChargeRight)
			finishedFiring();
		else
			super.animationFinished(animIndex);
	}
	
	@Override
	public void dashComplete()
	{
		setIdleLeftSprite(SpriteManager.bossWalkLeft[0]);
		setIdleRightSprite(SpriteManager.bossWalkRight[0]);
		
		if (chargeLeft)
		{
			if (charge.isInRange())
				restartAnimation(successfulChargeLeft);
			else
				restartAnimation(unsuccessfulChargeLeft);
		}
		else
		{
			if (charge.isInRange())
				restartAnimation(successfulChargeRight);
			else
				restartAnimation(unsuccessfulChargeRight);
		}
	}
	
	private void shiftToCaster()
	{
		// Setup caster images
		setIdleLeftSprite(casterIdleLeft);
		setIdleRightSprite(casterIdleRight);
		setWalkLeftAnimation(casterAnims[0]);
		setWalkRightAnimation(casterAnims[1]);
		setPreFiringLeftAnimation(casterAnims[2]);
		setPreFiringRightAnimation(casterAnims[3]);
		setPreChannelingLeftAnimation(-1);
		setPreChannelingRightAnimation(-1);
		setChannelingLeftAnimation(casterAnims[4]);
		setChannelingRightAnimation(casterAnims[5]);
		setPostChannelingLeftAnimation(-1);
		setPostChannelingRightAnimation(-1);
		setPostFiringLeftAnimation(casterAnims[6]);
		setPostFiringRightAnimation(casterAnims[7]);
		
		// Setup caster abilities
		clearAbilities();
		addAbility(trigger);
		for (final AIAbility aia : casterAbilities)
			addAbility(aia);
		
		isBossState = false;
	}
	
	private void shiftToBoss()
	{
		// Setup boss images
		setIdleLeftSprite(bossIdleLeft);
		setIdleRightSprite(bossIdleRight);
		setWalkLeftAnimation(bossAnims[0]);
		setWalkRightAnimation(bossAnims[1]);
		setPreFiringLeftAnimation(bossAnims[2]);
		setPreFiringRightAnimation(bossAnims[3]);
		setPreChannelingLeftAnimation(bossAnims[4]);
		setPreChannelingRightAnimation(bossAnims[5]);
		setChannelingLeftAnimation(bossAnims[6]);
		setChannelingRightAnimation(bossAnims[7]);
		setPostChannelingLeftAnimation(bossAnims[8]);
		setPostChannelingRightAnimation(bossAnims[9]);
		setPostFiringLeftAnimation(bossAnims[10]);
		setPostFiringRightAnimation(bossAnims[11]);
		
		// Setup boss abilities
		clearAbilities();
		addAbility(trigger);
		for (final AIAbility aia : bossAbilities)
			addAbility(aia);
		
		isBossState = true;
	}
	
	@Override
	public void deathAnim()
	{
		if (isBossState == false)
		{
			setDeathAnimationLeft(casterDeathLeftAnim);
			setDeathAnimationRight(casterDeathRightAnim);
			setDeathAnimXOffset(4 * CASTER_SCALE);
		}
		
		super.deathAnim();
	}
	
	@Override
	public OneSixBoss copy()
	{
		return new OneSixBoss(this);
	}
}