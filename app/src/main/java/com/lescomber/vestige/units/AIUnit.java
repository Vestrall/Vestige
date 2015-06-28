package com.lescomber.vestige.units;

import com.lescomber.vestige.Options;
import com.lescomber.vestige.aiabilities.AIAbility;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.statuseffects.StatPack;

import java.util.ArrayList;
import java.util.List;

public abstract class AIUnit extends Unit {
	private final ArrayList<AIAbility> abilities;

	private AIAbility queuedAbility;

	// true when this unit is first entering the map and should therefore not use abilities or move orders until arrival
	private boolean isEntering;

	public AIUnit(int faction, float hitboxWidth, float hitboxHeight, float imageOffsetY, float topGap) {
		super(faction, hitboxWidth, hitboxHeight, imageOffsetY);

		setTopGap(topGap);

		abilities = new ArrayList<AIAbility>(3);

		queuedAbility = null;

		isEntering = false;
	}

	public AIUnit(AIUnit copyMe) {
		super(copyMe);

		abilities = new ArrayList<AIAbility>(3);
		for (final AIAbility aib : copyMe.abilities) {
			final AIAbility aibCopy = aib.copy();
			aibCopy.setOwner(this);
			abilities.add(aibCopy);
		}
		isEntering = copyMe.isEntering;

		// Not copied
		queuedAbility = null;
	}

	protected void setDifficultyScaledBaseStats(StatPack baseStats) {
		baseStats.maxHp *= 1.0 + ((Options.difficulty - 1) * 0.20);
		baseStats.moveSpeed *= (0.9 + ((Options.difficulty - 1) * 0.1)) * (0.9 + Util.rand.nextDouble() * 0.15);
		setBaseStats(baseStats);
	}

	@Override
	public void update(int deltaTime) {
		// Update frame destination and any animations
		super.update(deltaTime);

		// Update abilities and fire one if applicable. Note: Will only happen after unit has finished entering GameScreen
		if (!isEntering) {
			for (final AIAbility aia : abilities)
				aia.update(deltaTime);

			// If we are not displacing or currently firing an ability, ask each ability if it's ready to fire until we find one
			//that is ready to go or we have checked all abilities once each
			if (!isFiring() && !isDisplacing()) {
				for (final AIAbility aia : abilities) {
					if (aia.isReadyToFire()) {
						startAbility(aia);
						break;
					}
				}
			}
		}
	}

	@Override    // Prevent move() from having an effect in the middle of firing an ability (unless that ability is displacing us)
	protected void move(float dx, float dy) {
		if (!isFiring() || isDisplacing())
			super.move(dx, dy);
	}

	public void startAbility(AIAbility ability) {
		if (ability.usesAnimation()) {
			if ((ability.isChanneled() && preChannelingAnim()) || preFiringAnim()) {
				setFiring(true);
				queuedAbility = ability;    // Queue up ability to fire once animation is complete
				return;
			}
		} else
			fire(ability);
	}

	protected void fire(AIAbility ability) {
		ability.fire();
	}

	protected void finishedFiring() {
		setFiring(false);
		if (getPathDestination() != null) {
			faceTowards(getDestination().x);
			walkingAnim();
		} else
			idleSprite();
	}

	@Override
	protected void animationFinished(int animIndex) {
		if (animIndex == getPreFiringLeftIndex() || animIndex == getPreFiringRightIndex() ||
				animIndex == getPreChannelingLeftIndex() || animIndex == getPreChannelingRightIndex()) {
			final boolean facingLeft = (animIndex == getPreFiringLeftIndex() || animIndex == getPreChannelingLeftIndex());

			fire(queuedAbility);

			if (!queuedAbility.isChanneled() || !channelingAnim(facingLeft, queuedAbility.getChannelDuration())) {
				if (!postFiringAnim(facingLeft))
					finishedFiring();
			}

			queuedAbility = null;
		} else if (animIndex == getChannelingLeftIndex()) {
			if (!postChannelingAnim(true) && !postFiringAnim(true))
				finishedFiring();
		} else if (animIndex == getChannelingRightIndex()) {
			if (!postChannelingAnim(false) && !postFiringAnim(false))
				finishedFiring();
		} else if (animIndex == getPostFiringLeftIndex() || animIndex == getPostFiringRightIndex() ||
				animIndex == getPostChannelingLeftIndex() || animIndex == getPostChannelingRightIndex())
			finishedFiring();
	}

	public void channelFinished() {
		if (getCurrentAnimID() == getChannelingLeftIndex() || getCurrentAnimID() == getChannelingRightIndex())
			getAnimation(getCurrentAnimID()).setDuration(0);
	}

	// Prevent unit from changing move destination if unit is still entering playing field from off-screen
	@Override
	public void setDestination(float destX, float destY) {
		if (!isEntering)
			super.setDestination(destX, destY);
	}

	@Override
	public void destinationReached() {
		if (isEntering) {
			isEntering = false;
			hasEntered();
		}
		super.destinationReached();
	}

	@Override
	protected void pathDestinationReached() {
		if (getPathDestination() == null)
			idleSprite();
	}

	public void clearAbilities() {
		abilities.clear();
	}

	public void setBossDefaults() {
		setSlowable(false);
		setDisplaceable(false);
	}

	// Override to insert any code that needs to run only after the unit has marched onto the playing field (if spawned
	//off-screen by GameScreen.map
	protected void hasEntered() {
		constrainHealthBar(true);
	}

	public void addAbility(AIAbility ability) {
		abilities.add(ability);
	}

	public void setEntering(boolean entering) {
		this.isEntering = entering;
	}

	public List<AIAbility> getAbilities() {
		return abilities;
	}

	public boolean isEntering() {
		return isEntering;
	}

	@Override
	public void close() {
		super.close();

		for (final AIAbility aia : abilities)
			aia.interrupted();
	}

	public abstract AIUnit copy();
}