package com.lescomber.vestige.units;

import com.lescomber.vestige.screens.GameScreen;

public abstract class AIMeleeUnit extends AIUnit {
	// Destination decision making constants
	private static final int CLOSE_RANGE_SQUARED = 250 * 250;
	private static final int TOO_CLOSE_SQUARED = 48 * 48;
	private static final int LONG_RANGE_CD = 500;
	private static final int SHORT_RANGE_CD = 150;

	private int destinationCooldown;    // Cooldown before re-deciding on a new destination

	public AIMeleeUnit(float hitboxWidth, float hitboxHeight, float imageOffsetY, float topGap) {
		super(hitboxWidth, hitboxHeight, imageOffsetY, topGap);

		destinationCooldown = 0;
	}

	public AIMeleeUnit(AIMeleeUnit copyMe) {
		super(copyMe);

		// Not copied
		destinationCooldown = 0;
	}

	@Override
	public void update(int deltaTime) {
		// Destination decision making
		if (!isEntering()) {
			destinationCooldown -= deltaTime;
			if (destinationCooldown <= 0)
				chooseDestination();
		}

		// Update frame destination, abilities, and any animations
		super.update(deltaTime);
	}

	@Override
	protected void pathDestinationReached() {
		super.pathDestinationReached();

		chooseDestination();
	}

	@Override
	protected void finishedFiring() {
		chooseDestination();

		super.finishedFiring();
	}

	public void chooseDestination() {
		// Select target based on proximity
		final Unit target = Unit.getNearestMember(getCenter(), opponents);
		if (target == null) {
			triggerDestinationCD(0);
			return;
		}

		// Set destination
		final double distanceSquared = getCenter().distanceToPointSquared(target.getCenter());
		if (distanceSquared < TOO_CLOSE_SQUARED)
			setPath(null);
		else
			setPath(GameScreen.map.getPath(getCenter(), target.getCenter()));

		triggerDestinationCD(distanceSquared);
	}

	void triggerDestinationCD(double distanceSquared) {
		if (distanceSquared <= CLOSE_RANGE_SQUARED)
			destinationCooldown += SHORT_RANGE_CD;
		else
			destinationCooldown += LONG_RANGE_CD;
	}

	@Override
	public abstract AIMeleeUnit copy();
}