package com.lescomber.vestige.units.sone;

import com.lescomber.vestige.geometry.Point;

public class DanceCoordinator {
	private static final float HALF_SIDE_LENGTH = 85;
	private static final float HALF_HEIGHT = (float) ((Math.sqrt(3) / 2) * HALF_SIDE_LENGTH);

	private final float x;
	private final float y;

	private final DancingCreep[] creeps;
	private DancingCaster caster;

	private int phaseNum;
	private int countdown;

	public DanceCoordinator(float x, float y) {
		this.x = x;
		this.y = y;

		creeps = new DancingCreep[3];
		caster = null;

		phaseNum = 0;
		countdown = 0;
	}

	public void addCreep(DancingCreep creep) {
		for (int i = 0; i < 3; i++) {
			if (creeps[i] == null) {
				creeps[i] = creep;
				return;
			}
		}
	}

	public void setCaster(DancingCaster caster) {
		this.caster = caster;
	}

	public void update(int deltaTime) {
		// Do nothing (not even countdown) while units are moving into place for the next phase
		if (isMoving())
			return;

		countdown -= deltaTime;
		if (countdown <= 0)
			nextPhase();
	}

	protected void nextPhase() {
		phaseNum++;

		if (phaseNum == 1) {
			// Move into starting positions
			caster.dancePosition(getMiddle());
			creeps[0].dancePosition(getTopMiddle());
			creeps[1].dancePosition(getBottomLeft());
			creeps[2].dancePosition(getBottomRight());
		} else if (phaseNum == 2) {
			if (!caster.isMoonwalking())
				caster.dance();
			creeps[0].danceLeft();
			creeps[1].danceLeft();
			creeps[2].danceRight();
			countdown = 2 * creeps[0].getDanceSequenceDuration();
		} else if (phaseNum == 3) {
			creeps[1].dancePosition(getBottomRight());
			creeps[2].dancePosition(getBottomLeft());
		} else if (phaseNum == 4) {
			creeps[1].danceLeft();
			creeps[2].danceRight();
			countdown = 2 * creeps[0].getDanceSequenceDuration();
		} else if (phaseNum == 5) {
			creeps[0].dancePosition(getBottomMiddle());
			creeps[1].dancePosition(getTopLeft());
			creeps[2].dancePosition(getTopRight());
		} else if (phaseNum == 6) {
			creeps[0].danceLeft();
			creeps[1].danceLeft();
			creeps[2].danceRight();
			countdown = 2 * creeps[0].getDanceSequenceDuration();
		} else
			phaseNum = 0;
	}

	public void aggro() {
		for (final DancingCreep dc : creeps) {
			if (dc != null)
				dc.aggro();
		}

		if (caster != null)
			caster.aggro();
	}

	private Point getMiddle() {
		return new Point(x, y + 10);
	}

	private Point getTopLeft() {
		return new Point(x - HALF_SIDE_LENGTH, y - HALF_HEIGHT);
	}

	private Point getTopMiddle() {
		return new Point(x, y - HALF_HEIGHT);
	}

	private Point getTopRight() {
		return new Point(x + HALF_SIDE_LENGTH, y - HALF_HEIGHT);
	}

	private Point getBottomLeft() {
		return new Point(x - HALF_SIDE_LENGTH, y + HALF_HEIGHT);
	}

	private Point getBottomMiddle() {
		return new Point(x, y + HALF_HEIGHT);
	}

	private Point getBottomRight() {
		return new Point(x + HALF_SIDE_LENGTH, y + HALF_HEIGHT);
	}

	private boolean isMoving() {
		for (final DancingCreep dc : creeps) {
			if (dc != null && dc.getDestination() != null)
				return true;
		}
		if (caster != null && caster.getDestination() != null && !caster.isMoonwalking())
			return true;

		return false;
	}
}