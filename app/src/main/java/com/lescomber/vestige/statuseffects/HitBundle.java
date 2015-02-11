package com.lescomber.vestige.statuseffects;

import java.util.ArrayList;
import java.util.List;

import com.lescomber.vestige.audio.AudioManager.SoundEffect;
import com.lescomber.vestige.graphics.SpriteAnimation;

public class HitBundle
{
	private float damage;
	
	private final ArrayList<StatusEffect> statusEffects;
	private DisplacementEffect displacement;
	
	private SpriteAnimation hitAnimation;	// animation that plays over the unit being "hit" by this HitBundle
	
	private boolean absorbSound;	// if false, absorb sounds (e.g. caused by shields absorbing this HitBundle's damage) should
									//not play
	private SoundEffect hitSound;	// SoundEffect that plays if this HitBundle damages its target
	
	public HitBundle(float damage)
	{
		statusEffects = new ArrayList<StatusEffect>(2);
		this.damage = damage;
		displacement = null;
		hitAnimation = null;
		absorbSound = true;
		hitSound = null;
	}
	
	public HitBundle(HitBundle copyMe)
	{
		damage = copyMe.damage;
		statusEffects = new ArrayList<StatusEffect>(2);
		for (final StatusEffect se : copyMe.statusEffects)
			statusEffects.add(se.copy());
		displacement = null;
		if (copyMe.displacement != null)
			displacement = new DisplacementEffect(copyMe.displacement);
		hitAnimation = null;
		if (copyMe.hitAnimation != null)
			hitAnimation = new SpriteAnimation(copyMe.hitAnimation);
		absorbSound = copyMe.absorbSound;
		hitSound = copyMe.hitSound;
	}
	
	public void addStatusEffect(StatusEffect effect) { statusEffects.add(effect); }
	public void setDisplacementEffect(DisplacementEffect effect) { displacement = effect; }
	public void setDamage(float damage) { this.damage = damage; }
	public void setHitAnimation(SpriteAnimation anim) { hitAnimation = anim; }
	public void setAbsorbSound(boolean absorbSound) { this.absorbSound = absorbSound; }
	public void setHitSound(SoundEffect hitSound) { this.hitSound = hitSound; }
	
	public List<StatusEffect> getStatusEffects() { return statusEffects; }
	public DisplacementEffect getDisplacementEffect() { return displacement; }
	public float getDamage() { return damage; }
	public SpriteAnimation getHitAnimation() { return hitAnimation; }
	public boolean getAbsorbSound() { return absorbSound; }
	public SoundEffect getHitSound() { return hitSound; }
}