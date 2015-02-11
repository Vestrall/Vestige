package com.lescomber.vestige.playerabilities;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;
import com.lescomber.vestige.graphics.UISprite;
import com.lescomber.vestige.graphics.UISwingSprite;

public class CDIndicator
{
	private final UISwingSprite[] arcs;
	private final UISwingSprite grayCover;
	private final UISprite iconReady;
	private final UISprite iconCooldown;
	
	private int cooldown;			// In ms
	private int maxCooldown;		// In ms
	private float halfMaxCooldown;	// In ms
	
	private int state;	// 0 = Ready, 1 = First half of cooldown, 2 = 2nd half of cooldown
	private boolean isVisible;
	
	public CDIndicator(SpriteTemplate iconReady, SpriteTemplate iconCooldown, float x, float y, int maxCooldownMs)
	{
		this.iconReady = new UISprite(iconReady, x, y);
		this.iconCooldown = new UISprite(iconCooldown, x, y);
		grayCover = new UISwingSprite(SpriteManager.cdInnerGrayArc, x, y, 6.5f, 0);
		
		this.iconReady.setLayerHeight(SpriteManager.UI_LAYER_OVER_ONE);
		this.iconCooldown.setLayerHeight(SpriteManager.UI_LAYER_OVER_ONE);
		grayCover.setLayerHeight(SpriteManager.UI_LAYER_OVER_THREE);
		
		arcs = new UISwingSprite[2];
		for (int i=0; i<2; i++)
		{
			arcs[i] = new UISwingSprite(SpriteManager.cdInnerBlueArc, x, y, 6.5f, 0);
			arcs[i].rotateTo((float)(Math.PI) * i);
			arcs[i].setLayerHeight(SpriteManager.UI_LAYER_OVER_TWO);
		}
		
		cooldown = 0;
		setMaxCooldown(maxCooldownMs);
		
		state = 0;
		isVisible = false;
	}
	
	public CDIndicator(SpriteTemplate iconReady, SpriteTemplate iconCooldown, int maxCooldown)
	{
		this(iconReady, iconCooldown, 0, 0, maxCooldown);
	}
	
	public CDIndicator(CDIndicator copyMe)
	{
		arcs = new UISwingSprite[2];
		for (int i=0; i<2; i++)
			arcs[i] = new UISwingSprite(copyMe.arcs[i]);
		grayCover = new UISwingSprite(copyMe.grayCover);
		iconReady = new UISprite(copyMe.iconReady);
		iconCooldown = new UISprite(copyMe.iconCooldown);
		cooldown = copyMe.cooldown;
		maxCooldown = copyMe.maxCooldown;
		halfMaxCooldown = copyMe.halfMaxCooldown;
		state = copyMe.state;
		isVisible = copyMe.isVisible;
	}

	public void update(int deltaTime)
	{
		if (state == 1)
		{
			cooldown -= deltaTime;
			if (cooldown < halfMaxCooldown)
			{
				arcs[0].rotateTo((float)Math.PI);
				state = 2;
				arcs[1].setVisible(isVisible);
				grayCover.setVisible(false);
				arcs[1].rotate(((halfMaxCooldown - cooldown) / halfMaxCooldown) * (float)Math.PI);
			}
			else
				arcs[0].rotate((deltaTime / halfMaxCooldown) * (float)Math.PI);
		}
		else if (state == 2)
		{
			cooldown -= deltaTime;
			if (cooldown <= 0)
			{
				state = 0;
				iconReady.setVisible(isVisible);
				iconCooldown.setVisible(false);
				
				// Reset arcs for next cooldown
				arcs[0].setVisible(false);
				arcs[1].setVisible(false);
				arcs[0].rotateTo(0);
				arcs[1].rotateTo((float)Math.PI);
			}
			else
				arcs[1].rotate((deltaTime / halfMaxCooldown) * (float)Math.PI);
		}
	}
	
	public void setMaxCooldown(int cooldownMs)
	{
		maxCooldown = cooldownMs;
		halfMaxCooldown = (float)cooldownMs / 2;
	}
	
	public void triggerCooldown()
	{
		// Change icon
		iconCooldown.setVisible(isVisible);
		iconReady.setVisible(false);
		
		// Reset cooldown counter and state
		cooldown = maxCooldown;
		state = 1;
		
		// Set state 1 arc visibility
		grayCover.setVisible(isVisible);
		arcs[0].setVisible(isVisible);
	}
	
	public void setCooldown(double cooldownSeconds)
	{
		cooldown = (int)(cooldownSeconds * 1000);
		
		if (cooldown <= 0)
		{
			state = 0;
			iconReady.setVisible(isVisible);
			iconCooldown.setVisible(false);
			
			// Reset arcs for next cooldown
			arcs[0].setVisible(false);
			arcs[1].setVisible(false);
			arcs[0].rotateTo(0);
			arcs[1].rotateTo((float)Math.PI);
		}
		else if (cooldown < halfMaxCooldown)
		{
			// Change icon
			iconCooldown.setVisible(isVisible);
			iconReady.setVisible(false);
			
			state = 1;
			
			// Set state 1 arc visibility
			grayCover.setVisible(isVisible);
			arcs[0].setVisible(isVisible);
		}
		else
		{
			arcs[0].rotateTo((float)Math.PI);
			state = 2;
			arcs[1].setVisible(isVisible);
			grayCover.setVisible(false);
			arcs[1].rotate(((halfMaxCooldown - cooldown) / halfMaxCooldown) * (float)Math.PI);
		}
	}
	
	public void offsetTo(float x, float y)
	{
		for (int i=0; i<2; i++)
			arcs[i].offsetTo(x, y);
		grayCover.offsetTo(x, y);
		iconReady.offsetTo(x, y);
		iconCooldown.offsetTo(x, y);
	}
	
	public void setVisible(boolean isVisible)
	{
		if (state == 0)
			iconReady.setVisible(isVisible);
		else if (state == 1)
		{
			iconCooldown.setVisible(isVisible);
			grayCover.setVisible(isVisible);
			arcs[0].setVisible(isVisible);
		}
		else if (state == 2)
		{
			iconCooldown.setVisible(isVisible);
			arcs[0].setVisible(isVisible);
			arcs[1].setVisible(isVisible);
		}
		
		this.isVisible = isVisible;
	}
}