package com.lescomber.vestige.aiabilities.sone;

import com.lescomber.vestige.aiabilities.AIAbility;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.projectiles.PickUp;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.statuseffects.HitBundle;
import com.lescomber.vestige.statuseffects.StatPack;
import com.lescomber.vestige.units.AIUnit;

import java.util.ArrayList;

public class MirrorImage extends AIAbility
{
	private static final float SPAWN_DISTANCE = 200;

	private final float startingHealth;

	private final ArrayList<AIAbility> imageAbilities;
	private PickUp imagePowerUp;

	public MirrorImage(AIUnit owner, float startingHealth, double cooldownSeconds)
	{
		super(owner, cooldownSeconds);

		this.startingHealth = startingHealth;

		imageAbilities = new ArrayList<AIAbility>(3);
		imagePowerUp = null;
	}

	public MirrorImage(MirrorImage copyMe)
	{
		super(copyMe);

		startingHealth = copyMe.startingHealth;
		imageAbilities = new ArrayList<AIAbility>(3);
		for (final AIAbility aia : copyMe.imageAbilities)
			imageAbilities.add(aia.copy());
		if (copyMe.imagePowerUp != null)
			imagePowerUp = copyMe.imagePowerUp.copy();
	}

	public void addImageAbility(AIAbility ability)
	{
		imageAbilities.add(ability.copy());
	}

	public void setImagePickUp(PickUp powerUp)
	{
		this.imagePowerUp = powerUp;
	}

	@Override
	public void activate()
	{
		final AIUnit image = owner.copy();

		// Set image stats
		final StatPack baseStats = new StatPack();
		baseStats.maxHp = (startingHealth * owner.getMaxHp()) / owner.getHp();
		baseStats.moveSpeed = owner.getMoveSpeed();
		image.setBaseStats(baseStats);
		image.hit(new HitBundle(baseStats.maxHp - startingHealth));    // "Hit" image to bring its hp down to startingHealth

		// Give the image its power up (if one exists)
		if (imagePowerUp != null)
			image.setPickUp(imagePowerUp.copy());

		// Set image abilities based on imageAbilities
		image.clearAbilities();
		for (final AIAbility aia : imageAbilities)
		{
			final AIAbility ability = aia.copy();
			ability.setOwner(image);
			image.addAbility(ability);
		}

		// Set image starting location and destination
		image.offsetTo(owner.getX(), owner.getY());
		final float imageDirection = Util.rand.nextFloat() * (2 * (float) Math.PI);
		final float imageDx = (float) Math.cos(imageDirection) * SPAWN_DISTANCE;
		final float imageDy = (float) Math.sin(imageDirection) * SPAWN_DISTANCE;
		image.setDestination(GameScreen.map.adjustDestination(imageDx, imageDy, image.getTopGap()));
		image.setEntering(true);

		// Set owner destination (in opposite direction from image's destination)
		owner.setDestination(GameScreen.map.adjustDestination(-imageDx, -imageDy, owner.getTopGap()));
		owner.setEntering(true);

		// Queue image
		owner.queueAIUnit(image);
	}

	@Override
	public MirrorImage copy()
	{
		return new MirrorImage(this);
	}
}