package com.lescomber.vestige.projectiles;

import com.lescomber.vestige.statuseffects.HitBundle;
import com.lescomber.vestige.units.Unit;

import java.util.Iterator;
import java.util.LinkedList;

public class HitGroup {
	private static final int DEFAULT_IMMUNITY_DURATION = 440;    // In ms. Half a second minus max frame time

	private final LinkedList<UnitTime> units;

	public HitGroup() {
		units = new LinkedList<UnitTime>();
	}

	public void update(int deltaTime) {
		final Iterator<UnitTime> itr = units.iterator();
		while (itr.hasNext()) {
			final UnitTime ut = itr.next();
			if (ut.time > 0) {
				ut.time -= deltaTime;
				if (ut.time <= 0)
					itr.remove();
			}
		}
	}

	public boolean canHit(Unit unit) {
		for (final UnitTime ut : units) {
			if (ut.unit == unit)
				return false;
		}

		return true;
	}

	public void hit(Unit unit, HitBundle bundle, int immunityDuration/*, SoundEffect soundEffect*/) {
		// If unit is already present, do nothing
		for (final UnitTime ut : units) {
			if (ut.unit == unit)
				return;
		}

		unit.hit(bundle);

		if (immunityDuration > 0)
			units.add(new UnitTime(unit, immunityDuration));
	}

	public void hit(Unit unit, HitBundle bundle) {
		hit(unit, bundle, DEFAULT_IMMUNITY_DURATION);
	}

	public void clear() {
		units.clear();
	}

	private class UnitTime {
		private final Unit unit;
		private int time;

		/**
		 * Note: initializing time with a negative value means that this UnitTime will not be removed until clear() is called
		 */
		private UnitTime(Unit unit, int time) {
			this.unit = unit;
			this.time = time;
		}
	}
}