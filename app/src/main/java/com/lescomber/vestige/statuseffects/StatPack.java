package com.lescomber.vestige.statuseffects;

import com.lescomber.vestige.framework.Util;

public class StatPack
{
	public float maxHp;		// Also behaves as bonusHp when used in StatusEffects
	public float bonusShields;
	public float moveSpeed;
	public double moveSpeedPercent;		// 1.0 = normal. adds multiplicatively
	
	public StatPack()
	{
		maxHp = 0;
		bonusShields = 0;
		moveSpeed = 0;
		moveSpeedPercent = 1.0;
	}
	
	public StatPack(StatPack copyMe)
	{
		maxHp = copyMe.maxHp;
		bonusShields = copyMe.bonusShields;
		moveSpeed = copyMe.moveSpeed;
		moveSpeedPercent = copyMe.moveSpeedPercent;
	}
	
	public void add(StatPack other)
	{
		maxHp += other.maxHp;
		bonusShields += other.bonusShields;
		moveSpeed += other.moveSpeed;
		moveSpeedPercent *= other.moveSpeedPercent;
	}
	
	public StatPack portion(float percentage)
	{
		final StatPack sp = new StatPack(this);
		
		sp.maxHp *= percentage;
		sp.bonusShields *= percentage;
		sp.moveSpeed *= percentage;
		double slowPercent = 1 - sp.moveSpeedPercent;
		slowPercent *= percentage;
		sp.moveSpeedPercent = 1 - slowPercent;
		
		return sp;
	}
	
	public boolean isEmpty()
	{
		return (Util.equals(maxHp, 0) && bonusShields <= 0 && Util.equals(moveSpeed, 0) && Util.equals(moveSpeedPercent, 1));
	}
}