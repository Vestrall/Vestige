package com.lescomber.vestige.statuseffects;

import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.graphics.Swapper;
import com.lescomber.vestige.units.Unit;

import java.util.Comparator;

public class StatusEffect {
	private static int ID_GENERATOR = 0;

	private final int id;

	private StatPack stats;
	private final int maxDuration;    // For effects that reset to maxDuration when one or more stacks fall off
	private int duration;
	private boolean removeOnNoEffect;    // If true, remove this StatusEffect when it no longer has an effect (e.g. shield that has been consumed)
	private int stacks;
	private int maxStacks;
	private int stacksLostOnDuration;    // # of stacks lost when duration reaches 0. Negative number means all stacks lost

	private SpriteAnimation anim;

	public StatusEffect(StatPack stats, double durationSeconds) {
		id = ++ID_GENERATOR;
		this.stats = new StatPack(stats);
		maxDuration = (int) (durationSeconds * 1000);
		duration = maxDuration;
		removeOnNoEffect = true;
		anim = null;
		stacks = 1;
		maxStacks = 1;
		stacksLostOnDuration = -1;
	}

	public StatusEffect(StatusEffect copyMe) {
		id = copyMe.id;
		stats = new StatPack(copyMe.stats);
		maxDuration = copyMe.maxDuration;
		duration = copyMe.duration;
		removeOnNoEffect = copyMe.removeOnNoEffect;
		stacks = copyMe.stacks;
		maxStacks = copyMe.maxStacks;
		stacksLostOnDuration = copyMe.stacksLostOnDuration;
		anim = null;
		if (copyMe.anim != null)
			anim = new SpriteAnimation(copyMe.anim);
	}

	/**
	 * @return true if the number of stacks (and therefore possibly the stats provided by this StatusEffect) has changed
	 */
	public boolean update(int deltaTime) {
		duration -= deltaTime;

		boolean stackCountChanged = false;
		while (duration <= 0) {
			stackCountChanged = true;

			duration += maxDuration;

			if (stacksLostOnDuration < 0)
				stacks = 0;
			else {
				final int stacksLost = Math.min(stacksLostOnDuration, stacks);
				final float portion = 1 - ((float) stacksLost / stacks);
				stats = stats.portion(portion);
				stacks -= stacksLost;
			}
		}

		if (anim != null)
			anim.update(deltaTime);

		return stackCountChanged || (removeOnNoEffect && noEffect());
	}

	public float absorbDamage(float damage) {
		if (stats.shields > 0) {
			if (damage > stats.shields) {
				damage -= stats.shields;
				stats.shields = 0;
			} else {
				stats.shields -= damage;
				damage = 0;
			}
		}

		return damage;
	}

	public void combine(StatusEffect other) {
		if (other.stacks + stacks > maxStacks) {
			final int newStacks = maxStacks - stacks;
			final float portion = (float) newStacks / other.stacks;
			stats.add(other.stats.portion(portion));
		} else
			stats.add(other.stats);

		stacks = Math.min(maxStacks, stacks + other.stacks);
		duration = Math.min(maxDuration, duration + other.duration);

		if (other.anim != null) {
			Swapper.swapImages(anim, other.anim);
			anim = other.anim;
		}
	}

	/**
	 * Called when this StatusEffect is applied to a unit
	 */
	public void attach(Unit unit) {
	}

	/**
	 * Called when this StatusEffect combines with an existing effect (i.e. stacks or refreshes existing effect)
	 */
	public void reattach(Unit unit) {
	}

	public void setStacks(int stacks, int maxStacks, int stacksLostOnDuration) {
		this.stacks = stacks;
		this.maxStacks = maxStacks;
		this.stacksLostOnDuration = stacksLostOnDuration;
	}

	public void offsetAnim(float dx, float dy) {
		if (anim != null)
			anim.offset(dx, dy);
	}

	public void offsetAnimTo(float x, float y) {
		if (anim != null)
			anim.offsetTo(x, y);
	}

	public void setAnimation(SpriteAnimation anim) {
		this.anim = anim;
		if (anim != null)
			anim.play();
	}

	public void setAnimVisible(boolean isVisible) {
		if (anim != null)
			anim.setVisible(isVisible);
	}

	public void close() {
		setAnimVisible(false);
	}

	public int getId() {
		return id;
	}

	public StatPack getStats() {
		return stats;
	}

	public int getDuration() {
		return duration;
	}

	public SpriteAnimation getAnim() {
		return anim;
	}

	public boolean isFinished() {
		return (stacks <= 0 || noEffect());
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setNoEffectRemoval(boolean removeOnNoEffect) {
		this.removeOnNoEffect = removeOnNoEffect;
	}

	public void setStats(StatPack stats) {
		this.stats = stats;
	}

	public boolean noEffect() {
		return removeOnNoEffect && stats.isEmpty();
	}

	public StatusEffect copy() {
		return new StatusEffect(this);
	}

	public static final Comparator<StatusEffect> STATUS_EFFECT_COMPARATOR = new Comparator<StatusEffect>() {
		@Override
		public int compare(StatusEffect one, StatusEffect two) {
			return one.getDuration() - two.getDuration();
		}
	};

	public void setLayerHeight(int layerHeight) {
		if (anim != null)
			anim.setLayerHeight(layerHeight);
	}
}