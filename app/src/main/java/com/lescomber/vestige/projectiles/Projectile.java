package com.lescomber.vestige.projectiles;

import com.lescomber.vestige.MobileEntity;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;
import com.lescomber.vestige.framework.AudioManager.SoundEffect;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.geometry.Hitbox;
import com.lescomber.vestige.graphics.Sprite;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.map.Obstacle;
import com.lescomber.vestige.projectiles.glows.Glow;
import com.lescomber.vestige.projectiles.glows.OvalGlow;
import com.lescomber.vestige.projectiles.glows.RotatedRectGlow;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.statuseffects.DisplacementEffect;
import com.lescomber.vestige.statuseffects.HitBundle;
import com.lescomber.vestige.statuseffects.StatusEffect;
import com.lescomber.vestige.units.Unit;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Projectile extends MobileEntity {
	// Standard hitbox sizes for certain common projectile images
	public static final float ENEMY_PROJECTILE_WIDTH = 30;
	public static final float ENEMY_PROJECTILE_HEIGHT = 18;

	// Standard imageOffsetY for all projectiles
	public static final float DEFAULT_IMAGE_OFFSET_Y = -15;

	private Glow glow;
	private float imageDropPerMs;    // This causes the projectile to descend to the ground at its destination
	private boolean imageDrop;
	private float glowPercentage;        // Represents the ratio of glow size to hitbox size
	private float glowPercentagePerMs;    // This causes the glow to grow as the projectile descends to its destination

	protected final HitBundle hitBundle;
	private Explosion explosion;
	private AreaEffect areaEffect;

	private SoundEffect explosionSound;

	private final ArrayList<Integer> targets;

	protected boolean isFinished;

	private final LinkedList<Projectile> projectilesBuffer;
	private final LinkedList<Projectile> projectilesReady;
	private final LinkedList<AreaEffect> areaEffectsBuffer;
	private final LinkedList<AreaEffect> areaEffectsReady;
	private final LinkedList<Explosion> explosionsBuffer;
	private final LinkedList<Explosion> explosionsReady;

	// Uses hitGroup if non-null, otherwise tracks its own unitsHit to avoid hitting the same unit every frame
	private HitGroup hitGroup;
	private final ArrayList<Unit> unitsHit;

	// Basic behavior fields
	private boolean offScreenRemoval;
	private boolean wallPassThrough;
	private boolean unitPassThrough;
	private boolean arrivalRemoval;
	private float damageReductionOnHit;
	private float minDamageFromReductions;

	Object lastHit;        // Used for PewBall only

	/**
	 * No hitbox constructor (still creates a 0 radius circle hitbox for offScreen detection)
	 */
	public Projectile(SpriteTemplate template, float damage) {
		super();

		createCircleHitbox(0);

		glow = null;
		if (template != null)
			setImage(new Sprite(template));
		setImageOffsetY(DEFAULT_IMAGE_OFFSET_Y);

		imageDropPerMs = 0;
		imageDrop = true;
		glowPercentage = 0.9f;
		glowPercentagePerMs = 0;

		hitBundle = new HitBundle(damage);

		targets = new ArrayList<Integer>(2);
		targets.add(GameScreen.gregs);
		explosion = null;
		areaEffect = null;

		explosionSound = null;

		setVelocityPerSecond(350);    // Default speed

		projectilesReady = new LinkedList<Projectile>();
		projectilesBuffer = new LinkedList<Projectile>();
		explosionsReady = new LinkedList<Explosion>();
		explosionsBuffer = new LinkedList<Explosion>();
		areaEffectsReady = new LinkedList<AreaEffect>();
		areaEffectsBuffer = new LinkedList<AreaEffect>();

		hitGroup = null;
		unitsHit = new ArrayList<Unit>(5);

		// Init default behavior flags
		offScreenRemoval = true;
		wallPassThrough = false;
		unitPassThrough = false;
		arrivalRemoval = true;
		damageReductionOnHit = 0;
		minDamageFromReductions = 1;

		isFinished = false;

		lastHit = null;        // PewBall only
	}

	/**
	 * RotatedRect hitbox constructor
	 */
	public Projectile(SpriteTemplate template, float damage, float width, float height) {
		this(template, damage);

		createRotatedRectHitbox(width, height);
	}

	/**
	 * Circle hitbox constructor
	 */
	public Projectile(SpriteTemplate template, float damage, float radius) {
		this(template, damage);

		createCircleHitbox(radius);
	}

	public Projectile(Projectile copyMe) {
		super(copyMe);

		glow = null;
		if (copyMe.glow != null)
			glow = copyMe.glow.copy();

		imageDropPerMs = copyMe.imageDropPerMs;
		imageDrop = copyMe.imageDrop;
		glowPercentage = copyMe.glowPercentage;
		glowPercentagePerMs = copyMe.glowPercentagePerMs;

		// Copy Projectile fields
		hitBundle = new HitBundle(copyMe.hitBundle);
		isFinished = copyMe.isFinished;
		hitGroup = copyMe.hitGroup;
		explosionSound = copyMe.explosionSound;

		// Behavior flags
		offScreenRemoval = copyMe.offScreenRemoval;
		wallPassThrough = copyMe.wallPassThrough;
		unitPassThrough = copyMe.unitPassThrough;
		arrivalRemoval = copyMe.arrivalRemoval;
		damageReductionOnHit = copyMe.damageReductionOnHit;
		minDamageFromReductions = copyMe.minDamageFromReductions;

		targets = copyMe.targets;
		if (copyMe.explosion == null)
			explosion = null;
		else
			explosion = copyMe.explosion.copy();
		if (copyMe.areaEffect == null)
			areaEffect = null;
		else
			areaEffect = copyMe.areaEffect.copy();

		// Note: projectile/areaEffect/explosion queues and unitsHit are not copied
		projectilesReady = new LinkedList<Projectile>();
		projectilesBuffer = new LinkedList<Projectile>();
		explosionsReady = new LinkedList<Explosion>();
		explosionsBuffer = new LinkedList<Explosion>();
		areaEffectsReady = new LinkedList<AreaEffect>();
		areaEffectsBuffer = new LinkedList<AreaEffect>();
		unitsHit = new ArrayList<Unit>(5);
	}

	@Override
	public void update(int deltaTime) {
		super.update(deltaTime);

		// Update image y offset if we're still in flight
		if (imageDrop && getDestination() != null)
			setImageOffsetY(getImageOffsetY() + (imageDropPerMs * deltaTime));

		// Update glow size if we're enlarging it as it descends to the ground
		if (glowPercentagePerMs > 0) {
			glowPercentage += glowPercentagePerMs * deltaTime;
			updateGlow(hitbox);
		}

		// Check if projectile is completely off screen, remove if so
		if (offScreenRemoval && !Screen.overlaps(hitbox, true)) {
			isFinished = true;
			return;
		}

		// Wall collision detection
		for (final Obstacle o : GameScreen.map.getObstacles()) {
			if (o.overlaps(this)) {
				obstacleHit(o);
				if (isFinished)
					return;
			}
		}

		// Unit collision detection
		for (final Integer i : targets) {
			for (final Unit u : GameScreen.units[i]) {
				if (overlaps(u)) {
					if (hitGroup != null) {
						if (hitGroup.canHit(u)) {
							unitHit(u);
							if (isFinished)
								return;
						}
					} else {
						if (!unitsHit.contains(u)) {
							unitHit(u);
							if (isFinished)
								return;
						}
					}
				}
			}
		}
	}

	@Override
	public void createRectangleHitbox(float width, float height) {
		super.createRectangleHitbox(width, height);
		if (glow != null)
			updateGlow(hitbox);
	}

	@Override
	public void createRotatedRectHitbox(float width, float height) {
		super.createRotatedRectHitbox(width, height);
		if (glow != null)
			updateGlow(hitbox);
	}

	@Override
	public void createCircleHitbox(float radius) {
		super.createCircleHitbox(radius);
		if (glow != null)
			updateGlow(hitbox);
	}

	@Override
	public void setDestination(float x, float y) {
		super.setDestination(x, y);

		calculateImageDrop();
	}

	@Override
	public void setImageOffsets(float imageOffsetX, float imageOffsetY) {
		super.setImageOffsets(imageOffsetX, imageOffsetY);

		//if (getDestination() != null)
		calculateImageDrop();
	}

	@Override
	public void offset(float dx, float dy) {
		super.offset(dx, dy);
		if (glow != null)
			glow.offset(dx, dy);

		// Recalculate image drop since we have been moved in some fashion outside of a normal "move()"
		calculateImageDrop();
	}

	@Override
	protected void move(float dx, float dy) {
		super.move(dx, dy);

		if (glow != null)
			glow.offset(dx, dy);
	}

	private void calculateImageDrop() {
		if (getDestination() == null)
			return;

		// Calculate image offset reduction per MS
		final float distance = (float) (getCenter().distanceToPoint(getDestination()));
		final float msToDestination = distance / getVelocity();
		imageDropPerMs = -(getImageOffsetY() / msToDestination);

		// Calculate glow growth as projectile descends to the ground
		glowPercentagePerMs = (1 - glowPercentage) / msToDestination;
	}

	@Override
	public void rotate(float radians) {
		super.rotate(radians);
		if (glow != null)
			updateGlow(hitbox);
	}

	@Override
	public void rotateAbout(float radians, float rotateX, float rotateY) {
		super.rotateAbout(radians, rotateX, rotateY);
		if (glow != null)
			updateGlow(hitbox);
	}

	@Override
	public void scale(double widthRatio, double heightRatio) {
		super.scale(widthRatio, heightRatio);
		if (glow != null)
			updateGlow(hitbox);
	}

	public void setGlow(SpriteTemplate template) {
		if (template == null) {
			if (glow != null)
				glow.close();
			glow = null;
			return;
		}

		// Kind of ugly way of recognizing rectangular glows
		if (template == SpriteManager.rectangleRedGlow)
			glow = new RotatedRectGlow(template);
		else
			glow = new OvalGlow(template);

		updateGlow(hitbox);

		glow.setVisible(isVisible());
	}

	public void setGlow(Glow glow) {
		if (glow == null) {
			if (this.glow != null)
				this.glow.close();
			this.glow = null;
			return;
		}

		this.glow = glow;
		updateGlow(hitbox);

		this.glow.setVisible(isVisible());
	}

	@Override
	protected void destinationReached() {
		if (arrivalRemoval)
			explode();
	}

	/**
	 * Handle obstacle collision
	 */
	protected void obstacleHit(Obstacle o) {
		if (!wallPassThrough)
			explode();
	}

	/**
	 * Handle unit collision
	 */
	protected void unitHit(Unit unit) {
		if (hitGroup != null)
			hitGroup.hit(unit, hitBundle);
		else {
			unit.hit(hitBundle);
			unitsHit.add(unit);
		}

		if (!unitPassThrough)
			explode();
		else if (damageReductionOnHit > 0 && getHitBundle().getDamage() > minDamageFromReductions)
			setDamage(Math.max(getHitBundle().getDamage() - damageReductionOnHit, minDamageFromReductions));
	}

	protected void explode() {
		if (explosionSound != null)
			explosionSound.play();

		if (explosion != null) {
			explosion.offsetTo(getX(), getY());
			queueExplosion(explosion.copy());
		}

		if (areaEffect != null) {
			areaEffect.offsetTo(getX(), getY());
			queueAreaEffect(areaEffect.copy());
		}

		isFinished = true;
	}

	protected void updateGlow(Hitbox hitbox) {
		if (glow != null)
			glow.updateShape(hitbox, glowPercentage);
	}

	public void queueProjectile(Projectile p) {
		projectilesBuffer.add(p);
	}

	public List<Projectile> getProjectileQueue() {
		projectilesReady.clear();
		projectilesReady.addAll(projectilesBuffer);
		projectilesBuffer.clear();
		return projectilesReady;
	}

	public void queueExplosion(Explosion e) {
		explosionsBuffer.add(e);
	}

	public List<Explosion> getExplosionQueue() {
		explosionsReady.clear();
		explosionsReady.addAll(explosionsBuffer);
		explosionsBuffer.clear();
		return explosionsReady;
	}

	public void queueAreaEffect(AreaEffect ae) {
		areaEffectsBuffer.add(ae);
	}

	public List<AreaEffect> getAreaEffectQueue() {
		areaEffectsReady.clear();
		areaEffectsReady.addAll(areaEffectsBuffer);
		areaEffectsBuffer.clear();
		return areaEffectsReady;
	}

	/**
	 * Behavior flag getters
	 */
	public boolean wallPassThrough() {
		return wallPassThrough;
	}

	public boolean unitPassThrough() {
		return unitPassThrough;
	}

	public boolean arrivalRemoval() {
		return arrivalRemoval;
	}

	public boolean getOffScreenRemoval() {
		return offScreenRemoval;
	}

	public AIProjectileBehavior getBehavior() {
		return new AIProjectileBehavior();
	}

	public void disableGlow() {
		if (glow != null)
			glow.close();
		glow = null;
	}

	public void setAreaEffect(AreaEffect areaEffect) {
		this.areaEffect = areaEffect.copy();
	}

	public void setExplosion(Explosion explosion) {
		this.explosion = explosion.copy();
	}

	public void setExplosionSound(SoundEffect explosionSound) {
		this.explosionSound = explosionSound;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public HitBundle getHitBundle() {
		return hitBundle;
	}

	public List<Integer> getTargets() {
		return targets;
	}

	public float getDamage() {
		return hitBundle.getDamage();
	}

	public boolean getUnitPassThrough() {
		return unitPassThrough;
	}

	/**
	 * Behavior flag setters
	 */
	public void setWallPassThrough(boolean wallPassThrough) {
		this.wallPassThrough = wallPassThrough;
	}

	public void setUnitPassThrough(boolean unitPassThrough) {
		this.unitPassThrough = unitPassThrough;
	}

	public void setArrivalRemoval(boolean arrivalRemoval) {
		this.arrivalRemoval = arrivalRemoval;
	}

	public void setOffScreenRemoval(boolean offScreenRemoval) {
		this.offScreenRemoval = offScreenRemoval;
	}

	public void setDamageReductionOnHit(double reductionPercent, double minDamagePercent) {
		damageReductionOnHit = (float) reductionPercent * getHitBundle().getDamage();
		minDamageFromReductions = (float) minDamagePercent * getHitBundle().getDamage();
	}

	public void setDamage(float damage) {
		hitBundle.setDamage(damage);
	}

	public void setTargets(int targetFaction) {
		targets.clear();
		targets.add(targetFaction);
	}

	public void addTargets(int targetFaction) {
		if (!targets.contains(targetFaction))
			targets.add(targetFaction);
	}

	public void setHitGroup(HitGroup hitGroup) {
		this.hitGroup = hitGroup;
	}

	public void setHitAnimation(SpriteAnimation anim) {
		hitBundle.setHitAnimation(anim);
	}

	public void setUnitHitSound(SoundEffect unitHitSound) {
		hitBundle.setHitSound(unitHitSound);
	}

	public void clearHitList() {
		unitsHit.clear();
	}

	public void addStatusEffect(StatusEffect effect) {
		hitBundle.addStatusEffect(effect);
	}

	public void setDisplacementEffect(DisplacementEffect de) {
		hitBundle.setDisplacementEffect(de);
	}

	public void setImageDrop(double initialGlowPercentage) {
		imageDrop = true;
		glowPercentage = (float) initialGlowPercentage;
	}

	public void disableImageDrop() {
		imageDrop = false;
		glowPercentagePerMs = 0;
	}

	@Override
	public void setVisible(boolean isVisible) {
		super.setVisible(isVisible);
		if (glow != null)
			glow.setVisible(isVisible);
	}

	@Override
	public void close() {
		super.close();
		if (glow != null)
			glow.close();
	}

	public Projectile copy() {
		return new Projectile(this);
	}
}