package com.lescomber.vestige;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.SpriteManager.SpriteInfo;
import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.gestures.GestureHandler;
import com.lescomber.vestige.gestures.GestureHandlerListener;
import com.lescomber.vestige.graphics.Swapper;
import com.lescomber.vestige.graphics.UISwingSprite;
import com.lescomber.vestige.units.Player;

public class ChargeSwipeArrow implements GestureHandlerListener
{
	private final GestureHandler gestureHandler;
	private Player player;
	
	private final UISwingSprite cooldownTail;
	private final UISwingSprite cooldownHead;
	private final UISwingSprite tail;
	private final UISwingSprite head;
	
	private static final float SPARKS_SWING_OFFSET = 26;
	private UISwingSprite sparks;
	private final SpriteInfo sparksInfo;
	
	private static final int SPARKS_MAX_COUNTDOWN = 100;
	private int sparksCountdown;
	private int sparksIndex;
	
	private boolean isDisabled;		// For tutorial screen purposes
	
	public ChargeSwipeArrow(GestureHandler gestureHandler)
	{
		this.gestureHandler = gestureHandler;
		gestureHandler.addListener(this);
		player = null;
		
		float halfWidth = (float)Math.ceil(((float)SpriteManager.chargeArrowCooldownTail.getWidth() / 2));
		cooldownTail = new UISwingSprite(SpriteManager.chargeArrowCooldownTail, -halfWidth, 0);
		cooldownTail.setLayerHeight(SpriteManager.UI_LAYER_UNDER_TWO);
		
		halfWidth = (float)Math.ceil((float)SpriteManager.chargeArrowCooldownHead.getWidth() / 2);
		cooldownHead = new UISwingSprite(SpriteManager.chargeArrowCooldownHead, halfWidth, 0);
		cooldownHead.setLayerHeight(SpriteManager.UI_LAYER_UNDER_TWO);
		
		halfWidth = (float)Math.ceil(((float)SpriteManager.chargeArrowTail.getWidth() / 2));
		tail = new UISwingSprite(SpriteManager.chargeArrowTail, -halfWidth, 0);
		tail.setLayerHeight(SpriteManager.UI_LAYER_UNDER_TWO);
		
		halfWidth = (float)Math.ceil((float)SpriteManager.chargeArrowHead.getWidth() / 2);
		head = new UISwingSprite(SpriteManager.chargeArrowHead, halfWidth, 0);
		head.setLayerHeight(SpriteManager.UI_LAYER_UNDER_TWO);
		
		sparks = new UISwingSprite(SpriteManager.chargeArrowSparks[0], SPARKS_SWING_OFFSET, 0);
		sparks.setLayerHeight(SpriteManager.UI_LAYER_UNDER_ONE);
		sparksInfo = sparks.getInfo();
		
		sparksCountdown = SPARKS_MAX_COUNTDOWN;
		sparksIndex = 0;
		
		isDisabled = false;
	}
	
	public void setPlayer(Player player)
	{
		this.player = player;
	}
	
	public void update(int deltaTime)
	{
		if (!gestureHandler.isSwiping())
		{
			if (player.isSwipeQueued())
				setVisible(false);
			else if (player.isChargeSwipeQueued())
				setVisible(true);
			else
				setVisible(false);
		}
		else if (isVisible())
		{
			if (player.isChargeSwipeReady())
				displayArrow();
			else
				displayCooldownArrow();
		}
		
		if (sparks.isVisible())
		{
			sparksCountdown -= deltaTime;
			
			if (sparksCountdown <= 0)
			{
				sparksCountdown += SPARKS_MAX_COUNTDOWN;
				
				nextSparks();
			}
		}
	}
	
	private void displayCooldownArrow()
	{
		cooldownTail.setVisible(!isDisabled);
		cooldownHead.setVisible(!isDisabled);
		tail.setVisible(false);
		head.setVisible(false);
		sparks.setVisible(false);
	}
	
	private void displayArrow()
	{
		cooldownTail.setVisible(false);
		cooldownHead.setVisible(false);
		tail.setVisible(!isDisabled);
		head.setVisible(!isDisabled);
		sparks.setVisible(!isDisabled);
	}
	
	private void nextSparks()
	{
		int newSparksIndex = sparksIndex + 1;
		
		if (newSparksIndex >= SpriteManager.chargeArrowSparks.length)
			newSparksIndex = 0;
		
		sparksIndex = newSparksIndex;
		sparksInfo.template = SpriteManager.chargeArrowSparks[sparksIndex];
		final UISwingSprite curSprite = sparks;
		sparks = new UISwingSprite(sparksInfo, SPARKS_SWING_OFFSET, 0);
		Swapper.swapImages(curSprite, sparks);
	}
	
	public void update(Line swipe)
	{
		final float direction = swipe.getDirection();
		cooldownTail.offsetTo(swipe.getStart());
		cooldownTail.rotateTo(direction);
		cooldownTail.scaleWidthTo((float)swipe.getLength() - cooldownHead.getWidth());
		cooldownHead.offsetTo(swipe.getEnd());
		cooldownHead.rotateTo(direction);
		tail.offsetTo(swipe.getStart());
		tail.rotateTo(direction);
		tail.scaleWidthTo((float)swipe.getLength() - head.getWidth());
		head.offsetTo(swipe.getEnd());
		head.rotateTo(direction);
		sparks.offsetTo(swipe.getEnd());
		sparks.rotateTo(direction);
	}
	
	public void setVisible(boolean isVisible)
	{
		if (isVisible)
		{
			if (player.isChargeSwipeReady())
				displayArrow();
			else
				displayCooldownArrow();
		}
		else
		{
			cooldownTail.setVisible(false);
			cooldownHead.setVisible(false);
			tail.setVisible(false);
			head.setVisible(false);
			sparks.setVisible(false);
		}
	}
	
	@Override
	public void handleTap(Point tapPoint)
	{
		setVisible(false);
	}
	
	@Override public void swipeBuilding(Line swipe) { }
	
	@Override
	public void chargeSwipeBuilding(Line swipe)
	{
		update(swipe);
		setVisible(true);
	}
	
	@Override
	public void swipeCancelled()
	{
		setVisible(false);
	}
	
	@Override public void handleSwipe(Line swipe) { }
	@Override public void handleChargeSwipe(Line swipe) { }
	
	@Override
	public void handleDoubleTap(Point tapPoint)
	{
		setVisible(false);
	}
	
	@Override public void handleMultiTap() { }
	
	public void setDisabled(boolean isDisabled) { this.isDisabled = isDisabled; }
	
	public boolean isVisible() { return cooldownTail.isVisible() || tail.isVisible(); }
}