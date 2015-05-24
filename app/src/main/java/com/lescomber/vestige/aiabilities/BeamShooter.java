package com.lescomber.vestige.aiabilities;

import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.projectiles.Beam;
import com.lescomber.vestige.screens.OptionsScreen;
import com.lescomber.vestige.units.AIUnit;
import com.lescomber.vestige.units.Unit;

/**
 * Fires channeled beams towards the nearest enemy target. Beam stops growing if channel is interrupted
 */
public class BeamShooter extends AIChanneledAbility {
	private static final float BEAM_DAMAGE[] = new float[] { 6, 7, 8 };
	private static final double CHANNEL_DURATION = 1.25;        // Time (in seconds) this beam is channeled (and grows) for

	private Beam beam;        // Latest beam to have been fired. This reference used to tell beam when to stop growing

	private Unit target;            // Current target to aim for
	private Point firingLocation;    // Remember the firing location we decided on when starting our firing animation

	public BeamShooter(AIUnit owner, double cooldownSeconds) {
		super(owner, CHANNEL_DURATION, cooldownSeconds);
	}

	public BeamShooter(BeamShooter copyMe) {
		super(copyMe);

		beam = copyMe.beam;        // Not a copy of copyMe.beam. References the same beam
		target = copyMe.target;
	}

	@Override
	protected void channeling(int deltaTime) {
	}

	@Override
	protected void channelFinished() {
		if (beam != null)
			beam.stopGrowth();
	}

	@Override
	public boolean decideToFire() {
		target = Unit.getNearestMember(owner.getCenter(), getTargetFaction());

		if (target != null) {
			owner.faceTowards(target.getX());
			firingLocation = owner.getFiringLocation();
			return true;
		} else
			return false;
	}

	@Override
	public void activate() {
		super.activate();

		// Set path information for new beam
		final Line line = new Line(firingLocation, target.getCenter());
		beam = new Beam(firingLocation.x, firingLocation.y, line.getDirection(), BEAM_DAMAGE[OptionsScreen.difficulty]);

		// Fire
		owner.queueProjectile(beam);
	}

	@Override
	public BeamShooter copy() {
		return new BeamShooter(this);
	}
}