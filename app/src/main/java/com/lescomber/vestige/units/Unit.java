package com.lescomber.vestige.units;

import com.lescomber.vestige.MobileEntity;
import com.lescomber.vestige.aiabilities.Dash;
import com.lescomber.vestige.audio.AudioManager;
import com.lescomber.vestige.audio.AudioManager.SoundEffect;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.graphics.Sprite;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.graphics.UISprite;
import com.lescomber.vestige.projectiles.AreaEffect;
import com.lescomber.vestige.projectiles.Explosion;
import com.lescomber.vestige.projectiles.PickUp;
import com.lescomber.vestige.projectiles.Projectile;
import com.lescomber.vestige.screens.GameScreen;
import com.lescomber.vestige.statuseffects.DisplacementEffect;
import com.lescomber.vestige.statuseffects.HitBundle;
import com.lescomber.vestige.statuseffects.StatPack;
import com.lescomber.vestige.statuseffects.StatusEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class Unit extends MobileEntity implements Dash
{
	private float hp;
	private StatPack baseStats;
	private StatPack stats;
	private final ArrayList<StatusEffect> statusEffects;
	
	private PickUp pickUp;
	
	int teammates;
	int opponents;
	
	// Immunity flags
	private boolean displaceable;
	private boolean slowable;
	
	private boolean mainUnit;	// Does this unit need to be killed to progress to next wave/level? (Only for steve units)
	
	private DisplacementEffect displacementEffect;
	private Dash dashCallback;
	private final LinkedList<Point> path;
	
	private float firingOffsetX;	// X offset from center of Unit's image to firing location
	private float firingOffsetY;	// Y offset from center of Unit's image to firing location
	
	private float topGap;	// No fly zone at the top of the screen for this particular unit. Used to stop units from walking
							//largely off the top of the screen
	
	private final LinkedList<Projectile> projectilesBuffer;
	private final LinkedList<Projectile> projectilesReady;
	private final LinkedList<AreaEffect> areaEffectsBuffer;
	private final LinkedList<AreaEffect> areaEffectsReady;
	private final LinkedList<Explosion> explosionsBuffer;
	private final LinkedList<Explosion> explosionsReady;
	private final LinkedList<AIUnit> unitsBuffer;
	private final LinkedList<AIUnit> unitsReady;
	
	// Sprite/Animation fields
	private boolean facingLeft;
	private Sprite idleLeftSprite;
	private Sprite idleRightSprite;
	private int walkingLeft;
	private int walkingRight;
	private int preFiringLeft;
	private int preFiringRight;
	private int preChannelingLeft;
	private int preChannelingRight;
	private int channelingLeft;
	private int channelingRight;
	private int postChannelingLeft;
	private int postChannelingRight;
	private int postFiringLeft;
	private int postFiringRight;
	private boolean isFiring;	// Could become a state variable (eg. firing, idle, walking, etc. For now, only isFiring)
	
	private SpriteAnimation deathAnimationLeft;
	private SpriteAnimation deathAnimationRight;
	private float deathAnimXOffset;
	
	// Health bar fields
	private static final int HEALTH_BAR_GAP = 10; 	// Gap between top of image and centerY of health bar
	protected UISprite healthBarBackground;
	protected UISprite healthBar;
	private static final float HEALTH_BAR_MIN_Y = 6;
	
	private boolean constrainHealthBar;
	private float healthBarVirtualXGap;		// Min x distance between sides of the screen and center of health bar
	private float healthBarVirtualX;	// Tracks healthBar's virtual x position for use when it would be off the screen
	private float healthBarVirtualY;	// Tracks healthBar's virtual y position for use when it would be off the screen
	
	public Unit(float hitboxWidth, float hitboxHeight, float imageOffsetY)
	{
		super();
		
		createRectangleHitbox(hitboxWidth, hitboxHeight);
		setImageOffsetY(imageOffsetY);
		
		firingOffsetX = 0;
		firingOffsetY = 0;
		topGap = 0;
		
		statusEffects = new ArrayList<StatusEffect>();
		
		pickUp = null;
		
		mainUnit = true;
		
		displaceable = true;
		slowable = true;
		
		displacementEffect = null;
		dashCallback = null;
		path = new LinkedList<Point>();
		
		projectilesReady = new LinkedList<Projectile>();
		projectilesBuffer = new LinkedList<Projectile>();
		areaEffectsReady = new LinkedList<AreaEffect>();
		areaEffectsBuffer = new LinkedList<AreaEffect>();
		explosionsReady = new LinkedList<Explosion>();
		explosionsBuffer = new LinkedList<Explosion>();
		unitsBuffer = new LinkedList<AIUnit>();
		unitsReady = new LinkedList<AIUnit>();
		
		// Init stats
		baseStats = new StatPack();
		stats = new StatPack();
		updateStats();
		hp = stats.maxHp;
		
		facingLeft = true;
		
		idleLeftSprite = null;
		idleRightSprite = null;
		walkingLeft = -1;
		walkingRight = -1;
		preFiringLeft = -1;
		preFiringRight = -1;
		postFiringLeft = -1;
		postFiringRight = -1;
		preChannelingLeft = -1;
		preChannelingRight = -1;
		channelingLeft = -1;
		channelingRight = -1;
		postChannelingLeft = -1;
		postChannelingRight = -1;
		isFiring = false;
		
		deathAnimationLeft = null;
		deathAnimationRight = null;
		deathAnimXOffset = 0;
		
		constrainHealthBar = false;
		healthBarVirtualXGap = 0;
		healthBarVirtualX = 0;
		healthBarVirtualY = 0;
	}
	
	public Unit(Unit copyMe)
	{
		super(copyMe);
		
		hp = copyMe.hp;
		baseStats = new StatPack(copyMe.baseStats);
		stats = new StatPack(copyMe.stats);
		if (copyMe.pickUp != null)
			pickUp = copyMe.pickUp.copy();
		mainUnit = copyMe.mainUnit;
		displaceable = copyMe.displaceable;
		slowable = copyMe.slowable;
		firingOffsetX = copyMe.firingOffsetX;
		firingOffsetY = copyMe.firingOffsetY;
		topGap = copyMe.topGap;
		facingLeft = copyMe.facingLeft;
		idleLeftSprite = null;
		if (copyMe.idleLeftSprite != null)
			idleLeftSprite = new Sprite(copyMe.idleLeftSprite);
		idleRightSprite = null;
		if (copyMe.idleRightSprite != null)
			idleRightSprite = new Sprite(copyMe.idleRightSprite);
		walkingLeft = copyMe.walkingLeft;
		walkingRight = copyMe.walkingRight;
		preFiringLeft = copyMe.preFiringLeft;
		preFiringRight = copyMe.preFiringRight;
		postFiringLeft = copyMe.postFiringLeft;
		postFiringRight = copyMe.postFiringRight;
		preChannelingLeft = copyMe.preChannelingLeft;
		preChannelingRight = copyMe.preChannelingRight;
		channelingLeft = copyMe.channelingLeft;
		channelingRight = copyMe.channelingRight;
		postChannelingLeft = copyMe.postChannelingLeft;
		postChannelingRight = copyMe.postChannelingRight;
		if (copyMe.deathAnimationLeft != null)
			deathAnimationLeft = new SpriteAnimation(copyMe.deathAnimationLeft);
		if (copyMe.deathAnimationRight != null)
			deathAnimationRight = new SpriteAnimation(copyMe.deathAnimationRight);
		deathAnimXOffset = copyMe.deathAnimXOffset;
		if (copyMe.healthBarBackground != null)
			healthBarBackground = new UISprite(copyMe.healthBarBackground);
		if (copyMe.healthBar != null)
			healthBar = new UISprite(copyMe.healthBar);
		constrainHealthBar = copyMe.constrainHealthBar;
		healthBarVirtualXGap = copyMe.healthBarVirtualXGap;
		healthBarVirtualX = copyMe.healthBarVirtualX;
		healthBarVirtualY = copyMe.healthBarVirtualY;
		
		// The following fields are not copied and are instead initialized to their defaults
		displacementEffect = null;
		dashCallback = null;
		path = new LinkedList<Point>();
		statusEffects = new ArrayList<StatusEffect>();
		projectilesReady = new LinkedList<Projectile>();
		projectilesBuffer = new LinkedList<Projectile>();
		areaEffectsReady = new LinkedList<AreaEffect>();
		areaEffectsBuffer = new LinkedList<AreaEffect>();
		explosionsReady = new LinkedList<Explosion>();
		explosionsBuffer = new LinkedList<Explosion>();
		unitsBuffer = new LinkedList<AIUnit>();
		unitsReady = new LinkedList<AIUnit>();
	}
	
	public void setBaseStats(StatPack baseStats)
	{
		this.baseStats = baseStats;
		updateStats();
		hp = baseStats.maxHp;
	}
	
	protected void createHealthBar(SpriteTemplate healthBackground, SpriteTemplate health)
	{
		if (healthBarBackground != null)
			healthBarBackground.close();
		if (healthBar != null)
			healthBar.close();

		if (healthBackground == null || health == null)
		{
			healthBarBackground = null;
			healthBar = null;
			return;
		}
		
		healthBarVirtualX = getImageX();
		healthBarVirtualXGap = healthBackground.getWidth() / 2;
		float x = healthBarVirtualX;
		
		if (constrainHealthBar)
		{
			if (x < healthBarVirtualXGap)
				x = healthBarVirtualXGap;
			else if (x > Screen.WIDTH - healthBarVirtualXGap)
				x = Screen.WIDTH - healthBarVirtualXGap;
		}
		
		float y = hitbox.getBottom() - ((hitbox.getBottom() - getImageY()) * 2) - HEALTH_BAR_GAP;
		healthBarVirtualY = y;
		if (constrainHealthBar)
			y = Math.max(y, HEALTH_BAR_MIN_Y);
		
		healthBarBackground = new UISprite(healthBackground, x, y);
		healthBarBackground.setLayerHeight(SpriteManager.UI_LAYER_UNDER_ONE);
		healthBar = new UISprite(health, x, y);
		healthBar.setLayerHeight(SpriteManager.UI_LAYER_UNDER_TWO);
		
		// Set health bar visibility
		healthBarBackground.setVisible(isVisible());
		healthBar.setVisible(isVisible());
	}
	
	protected void updateHealthBar()
	{
		if (healthBar == null)
			return;

		final float healthFraction = hp / getMaxHp();
		healthBar.setTexWidth(healthFraction);
	}
	
	public void faceLeft()
	{
		facingLeft = true;
	}
	
	public void faceRight()
	{
		facingLeft = false;
	}
	
	public void faceTowards(float x)
	{
		if (x < getX())
			faceLeft();
		else
			faceRight();
	}
	
	public void setIdleLeftSprite(Sprite sprite)
	{
		final boolean wasNull = (idleLeftSprite == null);
		
		idleLeftSprite = sprite;
		
		if (wasNull && isFacingLeft() && getDestination() == null)
			idleSprite();
	}
	
	public void setIdleLeftSprite(SpriteTemplate template)
	{
		if (template != null)
			setIdleLeftSprite(new Sprite(template));
		else
			idleLeftSprite = null;
	}
	
	public void setIdleRightSprite(Sprite sprite)
	{
		final boolean wasNull = (idleRightSprite == null);
		
		idleRightSprite = sprite;
		
		if (wasNull && isFacingRight() && getDestination() == null)
			idleSprite();
	}
	
	public void setIdleRightSprite(SpriteTemplate template)
	{
		if (template != null)
			setIdleRightSprite(new Sprite(template));
		else
			idleRightSprite = null;
	}
	
	public void idleSprite()
	{
		if (isFiring && !isDisplacing())
			return;
		
		if (facingLeft)
		{
			if (idleLeftSprite != null)
				setImage(idleLeftSprite);
			else if (idleRightSprite != null)
				setImage(idleRightSprite);
		}
		else
		{
			if (idleRightSprite != null)
				setImage(idleRightSprite);
			else if (idleLeftSprite != null)
				setImage(idleLeftSprite);
		}
		
		stopAnimation(walkingLeft);
		stopAnimation(walkingRight);
	}
	
	public void setWalkLeftAnimation(SpriteAnimation anim)
	{
		if (anim == null)
			walkingLeft = -1;
		else
		{
			anim.setSequenceLimit(-1);
			walkingLeft = addAnimation(anim);
		}
	}
	
	public void setWalkLeftAnimation(int animID)
	{
		walkingLeft = animID;
	}
	
	public void setWalkRightAnimation(SpriteAnimation anim)
	{
		if (anim == null)
			walkingRight = -1;
		else
		{
			anim.setSequenceLimit(-1);
			walkingRight = addAnimation(anim);
		}
	}
	
	public void setWalkRightAnimation(int animID)
	{
		walkingRight = animID;
	}
	
	public void walkingAnim()
	{
		// If there is a firing animation going on, default behavior is to not interrupt it for a walking animation
		//Note: If firing animations are interruptible for a subclass of unit, then isFiring should not be set to true
		//during the firing animation. In which case, the subclass is responsible for starting the walking animation at
		//the appropriate time
		if (isFiring)
			return;
		
		if (facingLeft)
		{
			if (walkingLeft >= 0)
			{
				playAnimation(walkingLeft);
				stopAnimation(walkingRight);
			}
			else if (walkingRight >= 0)
			{
				playAnimation(walkingRight);
				stopAnimation(walkingLeft);
			}
			else
				idleSprite();
		}
		else
		{
			if (walkingRight >= 0)
			{
				playAnimation(walkingRight);
				stopAnimation(walkingLeft);
			}
			else if (walkingLeft >= 0)
			{
				playAnimation(walkingLeft);
				stopAnimation(walkingRight);
			}
			else
				idleSprite();
		}
	}
	
	public void setPreFiringLeftAnimation(SpriteAnimation anim)
	{
		if (anim == null)
			preFiringLeft = -1;
		else
		{
			anim.setHoldLastFrame(true);
			preFiringLeft = addAnimation(anim);
		}
	}
	
	public void setPreFiringLeftAnimation(int animID)
	{
		preFiringLeft = animID;
	}
	
	public void setPreFiringRightAnimation(SpriteAnimation anim)
	{
		if (anim == null)
			preFiringRight = -1;
		else
		{
			anim.setHoldLastFrame(true);
			preFiringRight = addAnimation(anim);
		}
	}
	
	public void setPreFiringRightAnimation(int animID)
	{
		preFiringRight = animID;
	}
	
	// Returns true if a pre-firing animation is present and was restarted, false if there is no pre-firing animation
	public boolean preFiringAnim()
	{
		if (facingLeft)
		{
			if (preFiringLeft >= 0)
				restartAnimation(preFiringLeft);
			else if (preFiringRight >= 0)
				restartAnimation(preFiringRight);
			else
				return false;
		}
		else
		{
			if (preFiringRight >= 0)
				restartAnimation(preFiringRight);
			else if (preFiringLeft >= 0)
				restartAnimation(preFiringLeft);
			else
				return false;
		}
		
		// If there is no pre-firing animation, false will have been returned above
		return true;
	}
	
	public void setPostFiringLeftAnimation(SpriteAnimation anim)
	{
		if (anim == null)
			postFiringLeft = -1;
		else
		{
			anim.setHoldLastFrame(true);
			postFiringLeft = addAnimation(anim);
		}
	}
	
	public void setPostFiringLeftAnimation(int animID)
	{
		postFiringLeft = animID;
	}
	
	public void setPostFiringRightAnimation(SpriteAnimation anim)
	{
		if (anim == null)
			postFiringRight = -1;
		else
		{
			anim.setHoldLastFrame(true);
			postFiringRight = addAnimation(anim);
		}
	}
	
	public void setPostFiringRightAnimation(int animID)
	{
		postFiringRight = animID;
	}
	
	// Returns true if a post-firing animation is present and was restarted, false if there is no post-firing animation
	public boolean postFiringAnim(boolean preferLeftAnim)
	{
		if (preferLeftAnim)
		{
			if (postFiringLeft >= 0)
				restartAnimation(postFiringLeft);
			else if (postFiringRight >= 0)
				restartAnimation(postFiringRight);
			else
				return false;
		}
		else
		{
			if (postFiringRight >= 0)
				restartAnimation(postFiringRight);
			else if (postFiringLeft >= 0)
				restartAnimation(postFiringLeft);
			else
				return false;
		}
		
		// If there is no post-firing animation, false will have been returned above
		return true;
	}
	
	public void setPreChannelingLeftAnimation(SpriteAnimation anim)
	{
		if (anim == null)
			preChannelingLeft = -1;
		else
		{
			anim.setHoldLastFrame(true);
			preChannelingLeft = addAnimation(anim);
		}
	}
	
	public void setPreChannelingLeftAnimation(int animID)
	{
		preChannelingLeft = animID;
	}
	
	public void setPreChannelingRightAnimation(SpriteAnimation anim)
	{
		if (anim == null)
			preChannelingRight = -1;
		else
		{
			anim.setHoldLastFrame(true);
			preChannelingRight = addAnimation(anim);
		}
	}
	
	public void setPreChannelingRightAnimation(int animID)
	{
		preChannelingRight = animID;
	}
	
	public boolean preChannelingAnim()
	{
		if (facingLeft)
		{
			if (preChannelingLeft >= 0)
				restartAnimation(preChannelingLeft);
			else if (preChannelingRight >= 0)
				restartAnimation(preChannelingRight);
			else
				return false;
		}
		else
		{
			if (preChannelingRight >= 0)
				restartAnimation(preChannelingRight);
			else if (preChannelingLeft >= 0)
				restartAnimation(preChannelingLeft);
			else
				return false;
		}
		
		// If there is no pre-channeling animation, false will have been returned above
		return true;
	}
	
	public void setChannelingLeftAnimation(SpriteAnimation anim)
	{
		if (anim == null)
			channelingLeft = -1;
		else
		{
			anim.setHoldLastFrame(true);
			channelingLeft = addAnimation(anim);
		}
	}
	
	public void setChannelingLeftAnimation(int animID)
	{
		channelingLeft = animID;
	}
	
	public void setChannelingRightAnimation(SpriteAnimation anim)
	{
		if (anim == null)
			channelingRight = -1;
		else
		{
			anim.setHoldLastFrame(true);
			channelingRight = addAnimation(anim);
		}
	}
	
	public void setChannelingRightAnimation(int animID)
	{
		channelingRight = animID;
	}
	
	// Returns true if a channeling animation is present and was restarted, false if there is no channeling animation
	public boolean channelingAnim(boolean preferLeftAnim, int duration)
	{
		if (preferLeftAnim)
		{
			if (channelingLeft >= 0)
			{
				getAnimation(channelingLeft).setDuration(duration);
				restartAnimation(channelingLeft);
			}
			else if (channelingRight >= 0)
			{
				getAnimation(channelingRight).setDuration(duration);
				restartAnimation(channelingRight);
			}
			else
				return false;
		}
		else
		{
			if (channelingRight >= 0)
			{
				getAnimation(channelingRight).setDuration(duration);
				restartAnimation(channelingRight);
			}
			else if (channelingLeft >= 0)
			{
				getAnimation(channelingLeft).setDuration(duration);
				restartAnimation(channelingLeft);
			}
			else
				return false;
		}
		
		// If there is no channeling animation, false will have been returned above
		return true;
	}
	
	public void setPostChannelingLeftAnimation(SpriteAnimation anim)
	{
		if (anim == null)
			postChannelingLeft = -1;
		else
		{
			anim.setHoldLastFrame(true);
			postChannelingLeft = addAnimation(anim);
		}
	}
	
	public void setPostChannelingLeftAnimation(int animID)
	{
		postChannelingLeft = animID;
	}
	
	public void setPostChannelingRightAnimation(SpriteAnimation anim)
	{
		if (anim == null)
			postChannelingRight = -1;
		else
		{
			anim.setHoldLastFrame(true);
			postChannelingRight = addAnimation(anim);
		}
	}
	
	public void setPostChannelingRightAnimation(int animID)
	{
		postChannelingRight = animID;
	}
	
	public boolean postChannelingAnim(boolean preferLeftAnim)
	{
		if (preferLeftAnim)
		{
			if (postChannelingLeft >= 0)
				restartAnimation(postChannelingLeft);
			else if (postChannelingRight >= 0)
				restartAnimation(postChannelingRight);
			else
				return false;
		}
		else
		{
			if (postChannelingRight >= 0)
				restartAnimation(postChannelingRight);
			else if (postChannelingLeft >= 0)
				restartAnimation(postChannelingLeft);
			else
				return false;
		}
		
		// If there is no post-channeling animation, false will have been returned above
		return true;
	}
	
	public void setDeathAnimationLeft(SpriteAnimation anim)
	{
		deathAnimationLeft = anim;
	}
	
	public void setDeathAnimationLeft(int animID)
	{
		deathAnimationLeft = getAnimation(animID);
	}
	
	public void setDeathAnimationRight(SpriteAnimation anim)
	{
		deathAnimationRight = anim;
	}
	
	public void setDeathAnimationRight(int animID)
	{
		deathAnimationRight = getAnimation(animID);
	}
	
	public void deathAnim()
	{
		if (facingLeft)
		{
			if (deathAnimationLeft != null)
			{
				final Point imageCenter = getImageCenter();
				deathAnimationLeft.offsetTo(imageCenter.x - deathAnimXOffset, imageCenter.y);
				deathAnimationLeft.setLayerHeight(getLayerHeight());
				GameScreen.playAnimation(deathAnimationLeft, getImage());
			}
			else if (deathAnimationRight != null)
			{
				final Point imageCenter = getImageCenter();
				deathAnimationRight.offsetTo(imageCenter.x + deathAnimXOffset, imageCenter.y);
				deathAnimationRight.setLayerHeight(getLayerHeight());
				GameScreen.playAnimation(deathAnimationRight, getImage());
			}
		}
		else
		{
			if (deathAnimationRight != null)
			{
				final Point imageCenter = getImageCenter();
				deathAnimationRight.offsetTo(imageCenter.x + deathAnimXOffset, imageCenter.y);
				deathAnimationRight.setLayerHeight(getLayerHeight());
				GameScreen.playAnimation(deathAnimationRight, getImage());
			}
			else if (deathAnimationLeft != null)
			{
				final Point imageCenter = getImageCenter();
				deathAnimationLeft.offsetTo(imageCenter.x - deathAnimXOffset, imageCenter.y);
				deathAnimationLeft.setLayerHeight(getLayerHeight());
				GameScreen.playAnimation(deathAnimationLeft, getImage());
			}
		}
	}
	
	public void setDeathAnimXOffset(float xOffset)
	{
		deathAnimXOffset = xOffset;
	}
	
	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);
		
		updateStatusEffects(deltaTime);
	}
	
	@Override
	protected void destinationReached()
	{
		if (isDisplacing())
		{
			if (displacementEffect.getFaction() == getFaction())
				dashComplete();
			if (dashCallback != null)
				dashCallback.dashComplete();
			
			displacementEffect = null;
			setVelocityPerSecond(getMoveSpeed());
			if (!path.isEmpty())
				pathTo(path.removeLast());
		}
		else if (!path.isEmpty())
			setDestination(path.remove());
		else
			pathDestinationReached();
	}
	
	protected void pathDestinationReached() { }		// Triggered when unit has reached the end of its path
	
	public void hit(HitBundle bundle)
	{
		// Apply status effects
		for (final StatusEffect se : bundle.getStatusEffects())
			addStatusEffect(se.copy());
		
		// Apply displacement effect (if any)
		final DisplacementEffect de = bundle.getDisplacementEffect();
		if (de != null)
			addStatusEffect(de.copy());
		
		if (bundle.getDamage() >= 0)
			takeDamage(bundle.getDamage(), bundle.getAbsorbSound(), bundle.getHitSound());
		else
			heal(-bundle.getDamage());
		
		updateStats();
		updateHealthBar();
	}
	
	public void addStatusEffect(StatusEffect statusEffect)
	{
		final StatusEffect adder = statusEffect.copy();
		
		// Init adder's animation (if any)
		adder.offsetAnimTo(getImageX(), getImageY());
		adder.setLayerHeight((int)(Math.ceil(getY() + 1)));
		adder.setAnimVisible(isVisible());
		
		// Handle slow immunity (if applicable)
		final StatPack sp = adder.getStats();
		if (!slowable && (sp.moveSpeed < 0 || sp.moveSpeedPercent < 1.0))
		{
			sp.moveSpeed = 0;
			sp.moveSpeedPercent = 1.0;
			
			if (adder.noEffect())
				return;
		}
		
		for (final StatusEffect se : statusEffects)
		{
			if (se.getId() == adder.getId())
			{
				se.combine(adder);
				se.reattach(this);
				Collections.sort(statusEffects, StatusEffect.statusEffectComparator);
				updateStats();
				updateHealthBar();
				return;
			}
		}
		
		statusEffects.add(adder);
		adder.attach(this);
		
		// TESTME
		// Sort statusEffects based on duration (e.g. so that buffs will use up their shields in the most efficient order)
		Collections.sort(statusEffects, StatusEffect.statusEffectComparator);
		
		updateStats();
		updateHealthBar();
	}
	
	public void addStatusEffect(DisplacementEffect displacementEffect)
	{
		// Handle displacement immunity
		if (!isDisplaceable() && displacementEffect.getFaction() != getFaction())
			return;
		
		// Case: unit was being displaced. Consider current displacement effect finished and inform it's dashCallback if
		//applicable
		if (isDisplacing())
		{
			if (displacementEffect.getFaction() == getFaction())
				dashComplete();
			if (dashCallback != null)
				dashCallback.dashComplete();
		}
		
		// Case: unit was walking somewhere. Shove that destination onto the front of the queue. At the end of the
		//displacement, the back of the queue will be used to generate a new path to that final destination. (i.e. the
		//current destination that we are pushing onto the front of the queue will only matter if it is the only Point
		//along the current path)
		else if (getDestination() != null)
			path.addFirst(getDestination());
		
		// Store displacementEffect reference and set our velocity to that of the displacement effect
		this.displacementEffect = new DisplacementEffect(displacementEffect);
		setVelocityPerSecond(displacementEffect.getVelocityPerSecond());
		
		// Set our destination to the displacement effect's end location
		final Point destination = displacementEffect.getDestination(getX(), getY());
		setDestination(destination);
		
		// Use idleSprite while displacing
		if (displacementEffect.getFaction() == getFaction())
			faceTowards(destination.x);
		idleSprite();
	}
	
	public void addStatusEffect(DisplacementEffect displacementEffect, Dash dashCallback)
	{
		// Handle displacement immunity
		if (!isDisplaceable() && displacementEffect.getFaction() != getFaction())
			return;
		
		addStatusEffect(displacementEffect);
		
		this.dashCallback = dashCallback;
	}
	
	@Override public void dashComplete() { }	// Called when a friendly dash is completed (but not on knockback)
	
	private void updateStatusEffects(int deltaTime)
	{
		boolean hasChanged = false;
		final Iterator<StatusEffect> itr = statusEffects.iterator();
		while (itr.hasNext())
		{
			final StatusEffect se = itr.next();
			if (se.update(deltaTime))
			{
				hasChanged = true;
				if (se.isFinished())
				{
					se.close();
					itr.remove();
				}
			}
		}
		
		if (hasChanged)
		{
			updateStats();
			updateHealthBar();
		}
	}
	
	protected void updateStats()
	{
		float oldMaxHp = 0;
		if (stats != null)
			oldMaxHp = stats.maxHp;
		
		stats = new StatPack();
		
		stats.add(baseStats);
		for (final StatusEffect se : statusEffects)
			stats.add(se.getStats());
		
		// Increase hp appropriately if maxHp went up during this update
		if (stats.maxHp > oldMaxHp)
			hp += stats.maxHp - oldMaxHp;
		hp = Math.min(hp, stats.maxHp);		// Make sure hp isn't over the maxHp cap
		
		if (!isDisplacing())
			setVelocityPerSecond(getMoveSpeed());
	}
	
	void takeDamage(float damage, boolean absorbSound, SoundEffect hitSound)
	{
		float thisDamage = damage;
		
		for (final StatusEffect se : statusEffects)
			thisDamage = se.absorbDamage(damage);
		
		// Play shield absorb sound effect if any damage was absorbed
		if (absorbSound && thisDamage < damage)
			AudioManager.shieldAbsorb.play();
		else if (thisDamage > 0 && hitSound != null)
			hitSound.play();
		
		hp -= thisDamage;
	}
	
	void heal(float amount)
	{
		hp += amount;
		if (hp > getMaxHp())
			hp = getMaxHp();
	}
	
	public static Unit getNearestMember(Point point, int faction)
	{
		double minDistanceSquared = Double.MAX_VALUE;
		Unit target = null;
		
		for (final Unit u : GameScreen.units[faction])
		{
			final double d2 = point.distanceToPointSquared(u.getCenter());
			if (d2 < minDistanceSquared)
			{
				minDistanceSquared = d2;
				target = u;
			}
		}
		
		return target;
	}
	
	public static Unit getNearestMember(Point point, int faction, int rangeSquared)
	{
		double minDistanceSquared = Double.MAX_VALUE;
		Unit target = null;
		
		for (final Unit u : GameScreen.units[faction])
		{
			final double d2 = point.distanceToPointSquared(u.getCenter());
			if (d2 < minDistanceSquared && d2 < rangeSquared)
			{
				minDistanceSquared = d2;
				target = u;
			}
		}
		
		return target;
	}
	
	@Override
	public void offset(float dx, float dy)
	{
		super.offset(dx, dy);
		
		for (final StatusEffect se : statusEffects)
			se.offsetAnim(dx, dy);
		
		offsetHealthBar(dx, dy);
	}
	
	@Override
	protected void move(float dx, float dy)
	{
		super.move(dx, dy);
		
		for (final StatusEffect se : statusEffects)
			se.offsetAnim(dx, dy);
		
		offsetHealthBar(dx, dy);
	}
	
	protected void offsetHealthBar(float dx, float dy)
	{
		if (healthBar == null)
			return;

		float barDx;
		float barDy;
		
		healthBarVirtualX += dx;
		if (!constrainHealthBar)
			barDx = dx;
		else if (healthBarVirtualX <= healthBarVirtualXGap)
			barDx = healthBarVirtualXGap - healthBarBackground.getX();
		else if (healthBarVirtualX >= Screen.WIDTH - healthBarVirtualXGap)
			barDx = Screen.WIDTH - healthBarVirtualXGap - healthBarBackground.getX();
		else
			barDx = healthBarVirtualX - healthBarBackground.getX();
		
		healthBarVirtualY += dy;
		if (!constrainHealthBar)
			barDy = dy;
		else if (healthBarVirtualY <= HEALTH_BAR_MIN_Y)
			barDy = HEALTH_BAR_MIN_Y - healthBarBackground.getY();
		else
			barDy = healthBarVirtualY - healthBarBackground.getY();
		
		healthBarBackground.offset(barDx, barDy);
		healthBar.offset(barDx, barDy);
	}
	
	public void constrainHealthBar(boolean constrainHealthBar)
	{
		if (this.constrainHealthBar == constrainHealthBar)
			return;
		else if (healthBar == null)
		{
			this.constrainHealthBar = constrainHealthBar;
			return;
		}
		
		float barDx;
		float barDy;
		
		if (constrainHealthBar)
		{
			if (healthBarVirtualX <= healthBarVirtualXGap)
				barDx = healthBarVirtualXGap - healthBarBackground.getX();
			else if (healthBarVirtualX >= Screen.WIDTH - healthBarVirtualXGap)
				barDx = Screen.WIDTH - healthBarVirtualXGap - healthBarBackground.getX();
			else
				barDx = healthBarVirtualX - healthBarBackground.getX();
			
			if (healthBarVirtualY <= HEALTH_BAR_MIN_Y)
				barDy = HEALTH_BAR_MIN_Y - healthBarBackground.getY();
			else
				barDy = healthBarVirtualY - healthBarBackground.getY();
			
			//healthBarBackground.offset(barDx, barDy);
			//healthBar.offset(barDx, barDy);
		}
		else
		{
			barDx = getImageX() - healthBarBackground.getX();
			barDy = hitbox.getBottom() - ((hitbox.getBottom() - getImageY()) * 2) - HEALTH_BAR_GAP - healthBarBackground.getY();
		}

		// TESTME: these offsets were moved from the if section above (previously the else section set barDx and barDy but they were
		//never used)
		healthBarBackground.offset(barDx, barDy);
		healthBar.offset(barDx, barDy);
		
		this.constrainHealthBar = constrainHealthBar;
	}
	
	@Override
	public void scale(double widthRatio, double heightRatio)
	{
		// Remember some values for moving the health bar after the scale has happened
		final float curYGap = healthBarVirtualY - getImageY();
		final float oldImageY = getImageY();	// Used for health bar positioning later in scale()
		
		super.scale(widthRatio, heightRatio);
		
		// Scale ability offsets
		firingOffsetX *= widthRatio;
		firingOffsetY *= heightRatio;
		
		// Entity scale method will scale all animations and the current image. This means that one or both of the idle
		//Sprites will be left unscaled so scale it/them here
		if (getSprite() == idleLeftSprite)
		{
			if (idleRightSprite != null)
				idleRightSprite.scale(widthRatio, heightRatio);
		}
		else if (getSprite() == idleRightSprite)
		{
			if (idleLeftSprite != null)
				idleLeftSprite.scale(widthRatio, heightRatio);
		}
		else
		{
			if (idleLeftSprite != null)
				idleLeftSprite.scale(widthRatio, heightRatio);
			if (idleRightSprite != null)
				idleRightSprite.scale(widthRatio, heightRatio);
		}
		
		// Scale death animations (if any)
		if (deathAnimationLeft != null)
			deathAnimationLeft.scale(widthRatio, heightRatio);
		if (deathAnimationRight != null)
			deathAnimationRight.scale(widthRatio, heightRatio);
		
		// Scale death animation x offset
		deathAnimXOffset *= widthRatio;
		
		// Move hpBar to compensate for heightChange
		if (!Util.equals(heightRatio, 1.0))
		{
			final float newYGap = (float)(curYGap * heightRatio);
			final float dImageY = getImageY() - oldImageY;	// Represents the change in imageY position
			offsetHealthBar(0, newYGap - curYGap + dImageY);
		}
	}
	
	public void setPath(List<Point> path)
	{
		if (path == null || path.isEmpty())
		{
			this.path.clear();
			if (!isDisplacing())
				setDestination(null);
			return;
		}
		
		this.path.clear();
		this.path.addAll(path);
		if (!isDisplacing())
			setDestination(this.path.remove());
	}
	
	public void pathTo(Point end)
	{
		setPath(GameScreen.map.getPath(getCenter(), end));
	}
	
	// Kills this Unit. Also, this is called by GameScreen when the unit has died via other means so if there is any special
	//death related code to run, over-write this method and do it there
	public void die()
	{
		hp = 0;
		if (pickUp != null)
		{
			pickUp.offsetTo(getX(), getY());
			queueAreaEffect(pickUp);
		}
	}
	
	@Override
	public void setDestination(Point p)
	{
		super.setDestination(p);
		
		if (p == null)
			idleSprite();
	}
	
	@Override
	public void setDestination(float destX, float destY)
	{
		faceTowards(destX);
		
		super.setDestination(destX, destY);
		
		if (getDestination() != null)
			walkingAnim();
	}
	
	public Point getPathDestination()
	{
		if (!path.isEmpty())
			return path.get(path.size() - 1);
		else
			return getDestination();
	}
	
	// Returns the appropriate firing location (e.g. gun / hand position). Abilities may ask for this location when firing
	//a projectile
	public Point getFiringLocation(Point destination)
	{
		faceTowards(destination.x);
		
		return getFiringLocation();
	}
	
	// Returns the firing location based on the direction this Unit is currently facing (left or right)
	public Point getFiringLocation()
	{
		if (facingLeft)
			return new Point(getImageX() - firingOffsetX, getImageY() + firingOffsetY);
		else
			return new Point(getImageX() + firingOffsetX, getImageY() + firingOffsetY);
	}
	
	@Override
	public void setLayerHeight(int layerHeight)
	{
		super.setLayerHeight(layerHeight);
		
		for (final StatusEffect se : statusEffects)
			se.setLayerHeight(layerHeight + 1);		// + 1 added to make buffs draw on top of unit
	}
	
	public float getShields()
	{
		float ret = 0;
		for (final StatusEffect se : statusEffects)
		{
			if (se.getStats().bonusShields > 0)
				ret += se.getStats().bonusShields;
		}
		return ret;
	}
	
	public float getFiringOffsetX() { return firingOffsetX; }
	public float getFiringOffsetY() { return firingOffsetY; }
	public float getTopGap() { return topGap; }
	
	public float getMoveSpeed() { return ((float)(stats.moveSpeedPercent * stats.moveSpeed));	}
	public float getHp() { return hp; }
	public float getMaxHp() { return stats.maxHp; }
	public int getFaction() { return teammates; }
	public int getOpponents() { return opponents; }
	public int getTeammates() { return teammates; }
	public boolean isSlowable() { return slowable; }
	public boolean isDisplaceable() { return displaceable; }
	public boolean isDisplacing() { return displacementEffect != null; }
	public boolean isFacingLeft() { return facingLeft; }
	public boolean isFacingRight() { return !facingLeft; }
	public int getWalkingLeftIndex() { return walkingLeft; }
	public int getWalkingRightIndex() { return walkingRight; }
	public int getPreFiringLeftIndex() { return preFiringLeft; }
	public int getPreFiringRightIndex() { return preFiringRight; }
	public int getPreChannelingLeftIndex() { return preChannelingLeft; }
	public int getPreChannelingRightIndex() { return preChannelingRight; }
	public int getChannelingLeftIndex() { return channelingLeft; }
	public int getChannelingRightIndex() { return channelingRight; }
	public int getPostChannelingLeftIndex() { return postChannelingLeft; }
	public int getPostChannelingRightIndex() { return postChannelingRight; }
	public int getPostFiringLeftIndex() { return postFiringLeft; }
	public int getPostFiringRightIndex() { return postFiringRight; }
	public boolean isFiring() { return isFiring; }
	public boolean isMainUnit() { return mainUnit; }
	
	public void setFaction(int faction)
	{
		teammates = faction;
		opponents = (teammates + 1) % 2;
		
		if (teammates == GameScreen.steves)
			createHealthBar(SpriteManager.hpBarBackground, SpriteManager.hpSteveHealth);
		else
			createHealthBar(SpriteManager.hpBarBackground, SpriteManager.hpGregHealth);
	}
	public void setPickUp(PickUp pickUp) { this.pickUp = pickUp; }
	public void setMainUnit(boolean mainUnit) { this.mainUnit = mainUnit; }
	public void setFiring(boolean isFiring) { this.isFiring = isFiring; }
	public void setBaseMoveSpeed(float moveSpeed) { baseStats.moveSpeed = moveSpeed; updateStats(); }
	public void setSlowable(boolean slowable) { this.slowable = slowable; }
	public void setDisplaceable(boolean displaceable) { this.displaceable = displaceable; }
	public void setTopGap(float topGap) { this.topGap = topGap; }
	protected void setFiringOffsets(float offsetX, float offsetY)
	{
		firingOffsetX = offsetX;
		firingOffsetY = offsetY - Projectile.DEFAULT_IMAGE_OFFSET_Y;
	}
	
	@Override
	public void setVisible(boolean isVisible)
	{
		super.setVisible(isVisible);
		
		// Set statusEffect animation visibility
		for (final StatusEffect se : statusEffects)
			se.setAnimVisible(isVisible);
		
		// Set health bar visibility
		if (healthBarBackground != null)
			healthBarBackground.setVisible(isVisible);
		if (healthBar != null)
			healthBar.setVisible(isVisible);
	}
	
	public void queueProjectile(Projectile p)
	{
		projectilesBuffer.add(p);
	}
	
	public List<Projectile> getProjectileQueue()
	{
		projectilesReady.clear();
		projectilesReady.addAll(projectilesBuffer);
		projectilesBuffer.clear();
		return projectilesReady;
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
	
	public void queueExplosion(Explosion e)
	{
		explosionsBuffer.add(e);
	}
	
	public List<Explosion> getExplosionQueue()
	{
		explosionsReady.clear();
		explosionsReady.addAll(explosionsBuffer);
		explosionsBuffer.clear();
		return explosionsReady;
	}
	
	public void queueAIUnit(AIUnit unit)
	{
		unitsBuffer.add(unit);
	}
	
	public List<AIUnit> getAIUnitQueue()
	{
		unitsReady.clear();
		unitsReady.addAll(unitsBuffer);
		unitsBuffer.clear();
		return unitsReady;
	}
	
	@Override
	public void close()
	{
		super.close();
		
		deathAnim();
	}
}