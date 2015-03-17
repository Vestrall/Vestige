package com.lescomber.vestige.projectiles;

import com.lescomber.vestige.Entity;
import com.lescomber.vestige.geometry.Cone;
import com.lescomber.vestige.geometry.Hitbox;
import com.lescomber.vestige.geometry.RotatedRect;
import com.lescomber.vestige.graphics.Image;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.statuseffects.DisplacementEffect;
import com.lescomber.vestige.statuseffects.HitBundle;
import com.lescomber.vestige.statuseffects.StatusEffect;
import com.lescomber.vestige.units.Unit;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AreaEffect extends Entity
{
	private static final int DEFAULT_TICK_FREQUENCY = 500;

	private HitGroup group;

	private int ticksRemaining;
	private int tickFrequency;        // Time (in ms) between each tick
	private int tickCountdown;        // Time (in ms) remaining until next tick
	private final HitBundle hitBundle;
	private final ArrayList<Integer> targets;

	int animID;
	int ignitionAnimID;

	private final LinkedList<AreaEffect> areaEffectsBuffer;
	private final LinkedList<AreaEffect> areaEffectsReady;

	private boolean isFinished;

	public AreaEffect(float dps, double durationSeconds)
	{
		super();

		group = null;

		tickFrequency = DEFAULT_TICK_FREQUENCY;
		ticksRemaining = (int) ((durationSeconds * 1000) / tickFrequency) + 1;    // + 1 due to initial tick of damage
		tickCountdown = 0;
		final float damage = dps / (1000f / tickFrequency);
		hitBundle = new HitBundle(damage);
		hitBundle.setAbsorbSound(false);    // By default, area effects make no noise when damaging (or being absorbed)
		targets = new ArrayList<Integer>(2);
		targets.add(GameScreen.gregs);
		animID = -1;
		ignitionAnimID = -1;
		isFinished = false;

		areaEffectsReady = new LinkedList<AreaEffect>();
		areaEffectsBuffer = new LinkedList<AreaEffect>();
	}

	// Creates a rectangular AreaEffect. Must specify animation separately
	public AreaEffect(float width, float height, float dps, double durationSeconds)
	{
		this(dps, durationSeconds);

		createRectangleHitbox(width, height);
	}

	// Creates a circular AreaEffect. Must specify animation separately
	public AreaEffect(float radius, float dps, double durationSeconds)
	{
		this(dps, durationSeconds);

		createCircleHitbox(radius);
	}

	public AreaEffect(AreaEffect copyMe)
	{
		super(copyMe);

		group = copyMe.group;

		ticksRemaining = copyMe.ticksRemaining;
		tickFrequency = copyMe.tickFrequency;
		tickCountdown = copyMe.tickCountdown;
		hitBundle = new HitBundle(copyMe.hitBundle);
		targets = new ArrayList<Integer>(2);
		for (final Integer i : copyMe.targets)
			targets.add(i);
		animID = copyMe.animID;
		ignitionAnimID = copyMe.ignitionAnimID;
		isFinished = copyMe.isFinished;

		// Not copied
		areaEffectsReady = new LinkedList<AreaEffect>();
		areaEffectsBuffer = new LinkedList<AreaEffect>();
	}

	public void createRotatedRectHitbox(float width, float height, float direction)
	{
		hitbox = new Hitbox(new RotatedRect(getX(), getY(), width, height, direction));
	}

	public void createConeHitbox(float radius, float direction, float width)
	{
		hitbox = new Hitbox(new Cone(getX(), getY(), radius, direction, width));
	}

	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);

		tickCountdown -= deltaTime;
		while (tickCountdown <= 0 && ticksRemaining > 0)
		{
			tickCountdown += tickFrequency;

			// Check hitboxes and hit things
			for (final Integer i : targets)
			{
				for (final Unit u : GameScreen.units[i])
				{
					if (overlaps(u))
					{
						if (group != null)
							groupHit(u, hitBundle, tickFrequency - deltaTime);
						else
							unitHit(u, hitBundle);
					}
				}
			}

			ticksRemaining--;

			if (ticksRemaining <= 0)
				die();
		}
	}

	protected void groupHit(Unit unit, HitBundle bundle, int suggestedCooldown)
	{
		group.hit(unit, bundle, suggestedCooldown);
	}

	protected void unitHit(Unit unit, HitBundle bundle)
	{
		unit.hit(bundle);
	}

	@Override
	protected void animationFinished(int animID)
	{
		if (animID == ignitionAnimID)
			playAnimation(this.animID);

			// this.animID should only "finish" if we have completed our last tick
		else if (animID == this.animID)
			isFinished = true;
	}

	// Adds anim, stores its ID in ignitionAnimID, and sets it as the current image
	public void setIgnitionAnim(SpriteAnimation anim)
	{
		ignitionAnimID = setImage(anim);
	}

	// Adds anim, stores its ID in animID and, if ignitionAnimID does not exist, sets anim as the current animation (OTOH, if
	//there is an ignition animation already set, we let animID play once ignitionAnimID completes)
	public void setAreaAnimation(SpriteAnimation anim)
	{
		anim.setSequenceLimit(-1);

		if (ignitionAnimID >= 0)
			animID = addAnimation(anim);
		else
			animID = setImage(anim);
	}

	@Override
	public int setImage(Image image)
	{
		animID = super.setImage(image);

		return animID;
	}

	@Override
	public boolean playAnimation(int index)
	{
		final boolean ret = super.playAnimation(index);

		if (ret)
			animID = index;

		return ret;
	}

	public void setHitGroup(HitGroup group)
	{
		this.group = group;
	}

	public HitGroup getHitGroup()
	{
		return group;
	}

	public void setDamagePerSecond(float dps)
	{
		hitBundle.setDamage(dps / (1000f / tickFrequency));
	}

	public void setDamagePerTick(float damage)
	{
		hitBundle.setDamage(damage);
	}

	public void setTickFrequency(int tickFrequency)
	{
		final float dps = hitBundle.getDamage() * (1000f / this.tickFrequency);
		final int timeRemaining = tickCountdown + ((ticksRemaining - 1) * this.tickFrequency);
		this.tickFrequency = tickFrequency;
		ticksRemaining = timeRemaining / tickFrequency;
		setDamagePerSecond(dps);
	}

	public void queueAreaEffect(AreaEffect ae)
	{
		areaEffectsBuffer.add(ae);
	}

	public List<AreaEffect> getAreaEffectQueue()
	{
		areaEffectsReady.clear();
		areaEffectsReady.addAll(areaEffectsBuffer);
		areaEffectsBuffer.clear();
		return areaEffectsReady;
	}

	protected void die()
	{
		ticksRemaining = 0;
		close();
	}

	public void addStatusEffect(StatusEffect effect)
	{
		hitBundle.addStatusEffect(effect);
	}

	public void setDisplacementEffect(DisplacementEffect de)
	{
		hitBundle.setDisplacementEffect(de);
	}

	public void setHitAnimation(SpriteAnimation anim)
	{
		hitBundle.setHitAnimation(anim);
	}

	public void setTargets(int targetFaction)
	{
		targets.clear();
		targets.add(targetFaction);
	}

	public void addTargets(int targetFaction)
	{
		if (!targets.contains(targetFaction)) targets.add(targetFaction);
	}

	public void setAbsorbSound(boolean absorbSound)
	{
		hitBundle.setAbsorbSound(absorbSound);
	}

	public boolean isFinished()
	{
		return isFinished;
	}

	public float getDPS()
	{
		return (1000f / tickFrequency) * hitBundle.getDamage();
	}

	public List<Integer> getTargets()
	{
		return targets;
	}

	public AreaEffect copy()
	{
		return new AreaEffect(this);
	}
}