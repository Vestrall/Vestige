package com.lescomber.vestige.aiabilities;

import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.geometry.Hitbox;
import com.lescomber.vestige.units.AIUnit;

// Spawns units on cooldown at the standard firingLocation
public class AIUnitSpawner extends AIAbility
{
	AIUnit spawn;

	// If non-null, destBox defines where spawned unit destinations can be applied to
	private Hitbox destBox;

	// X and Y ranges for spawned unit destination. destBox takes priority if it is non-null
	private float destXRange;
	private float destYRange;

	// Number of spawn's that will be spawned on each activation. Default: 1
	private int copiesPerSpawn;

	public AIUnitSpawner(AIUnit owner, double cooldownSeconds, AIUnit spawn)
	{
		super(owner, cooldownSeconds/*, false*/);

		this.spawn = spawn.copy();

		destBox = null;
		destXRange = 0;
		destYRange = 0;

		copiesPerSpawn = 1;
	}

	public AIUnitSpawner(AIUnitSpawner copyMe)
	{
		super(copyMe);

		spawn = copyMe.spawn.copy();
		destBox = null;
		if (copyMe.destBox != null)
			destBox = new Hitbox(copyMe.destBox);
		destXRange = copyMe.destXRange;
		destYRange = copyMe.destYRange;

		copiesPerSpawn = copyMe.copiesPerSpawn;
	}

	// Spawns spawn.copy() at the owner's firing location and, if destBox or destXRange/destYRange have been set, gives
	//the newly spawned unit a destination within destBox or within the range specified by destXRange/destYRange
	@Override
	public void activate()
	{
		for (int i = 0; i < copiesPerSpawn; i++)
		{
			final AIUnit newSpawn = spawn.copy();
			newSpawn.offsetTo(owner.getX() + owner.getFiringOffsetX(), owner.getY() + owner.getFiringOffsetY());

			if (destBox != null)
			{
				final Hitbox testBox = new Hitbox(newSpawn.getHitbox());
				testBox.offsetTo(-100, -100);

				final float destBoxHalfWidth = destBox.getWidth() / 2;
				final float destBoxHalfHeight = destBox.getHeight() / 2;
				float newX = 0;
				float newY = 0;

				float loopCount = 0;
				while (!testBox.isCompletelyOnScreen())
				{
					final float randX = (Util.rand.nextFloat() * 2 * destBoxHalfWidth) - destBoxHalfWidth;
					final float randY = (Util.rand.nextFloat() * 2 * destBoxHalfHeight) - destBoxHalfHeight;
					newX = destBox.getX() + randX;
					newY = destBox.getY() + randY;
					testBox.offsetTo(newX, newY);

					// Prevent infinite loops.. but probably should throw an exception here
					if (++loopCount > 25)
						break;
				}
				newSpawn.setDestination(newX, newY);
			}
			else if (destXRange > 0 || destYRange > 0)
			{
				final Hitbox testBox = new Hitbox(newSpawn.getHitbox());
				testBox.offsetTo(-100, -100);

				float newX = 0;
				float newY = 0;

				float loopCount = 0;
				while (!testBox.isCompletelyOnScreen())
				{
					final float randX = (Util.rand.nextFloat() * 2 * destXRange) - destXRange;
					final float randY = (Util.rand.nextFloat() * 2 * destYRange) - destYRange;
					newX = newSpawn.getX() + randX;
					newY = newSpawn.getY() + randY;
					testBox.offsetTo(newX, newY);

					// Prevent infinite loops.. but probably should throw an exception here
					if (++loopCount > 25)
						break;
				}
				newSpawn.setDestination(newX, newY);
			}
			else
			{
				// Trigger newSpawn's destination choosing code (if any)
				newSpawn.setDestination(newSpawn.getX(), newSpawn.getY());
			}

			owner.queueAIUnit(newSpawn);
		}
	}

	public void setDestinationBox(Hitbox destBox)
	{
		if (destBox == null)
			this.destBox = null;
		else
			this.destBox = new Hitbox(destBox);
	}

	public void setDestinationRange(float rangeX, float rangeY)
	{
		destXRange = rangeX;
		destYRange = rangeY;
	}

	public void setCopiesPerSpawn(int copiesPerSpawn)
	{
		this.copiesPerSpawn = copiesPerSpawn;
	}

	@Override
	public AIUnitSpawner copy()
	{
		return new AIUnitSpawner(this);
	}
}