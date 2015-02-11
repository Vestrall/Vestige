package com.lescomber.vestige.playerabilities;

import com.lescomber.vestige.Ability;
import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;
import com.lescomber.vestige.units.Player;

public abstract class PlayerAbility extends Ability
{
	Player player;
	
	private CDIndicator cdIndicator;
	
	PlayerAbility(Player player)
	{
		super();
		
		this.player = player;
		cdIndicator = null;
	}
	
	public PlayerAbility(PlayerAbility copyMe)
	{
		super(copyMe);
		
		player = copyMe.player;
		cdIndicator = new CDIndicator(copyMe.cdIndicator);
	}
	
	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);
		
		if (cdIndicator != null)
			cdIndicator.update(deltaTime);
	}
	
	@Override
	public void setCooldown(double cooldownSeconds)
	{
		super.setCooldown(cooldownSeconds);
		
		if (cdIndicator != null)
			cdIndicator.setCooldown(cooldownSeconds);
	}
	
	@Override
	public void setMaxCooldown(double cooldownSeconds)
	{
		super.setMaxCooldown(cooldownSeconds);
		
		if (cdIndicator != null)
			cdIndicator.setMaxCooldown(getMaxCooldown());
	}
	
	@Override
	public void triggerCooldown()
	{
		super.triggerCooldown();
		
		if (cdIndicator != null)
			cdIndicator.triggerCooldown();
	}
	
	public void setPlayer(Player player) { this.player = player; }
	
	public void setCDIndicator(SpriteTemplate iconReady, SpriteTemplate iconCooldown)
	{
		cdIndicator = new CDIndicator(iconReady, iconCooldown, getMaxCooldown());
	}
	
	public void offsetCDIndicatorTo(float x, float y)
	{
		if (cdIndicator != null)
			cdIndicator.offsetTo(x, y);
	}
	
	public void setCDIndicatorVisible(boolean isVisible)
	{
		if (cdIndicator != null)
			cdIndicator.setVisible(isVisible);
	}
}