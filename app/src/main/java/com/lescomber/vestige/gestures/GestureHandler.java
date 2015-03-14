package com.lescomber.vestige.gestures;

import com.lescomber.vestige.framework.Input.TouchEvent;
import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;

import java.util.ArrayList;

public class GestureHandler
{
	private static final int MIN_SWIPE_DISTANCE_SQUARED = 35 * 35;
	private static final int MAX_DOUBLE_TAP_DISTANCE_SQUARED = 30 * 30;
	private static final int MAX_DOUBLE_TAP_TIME = 400;
	private static final int MIN_CHARGE_TIME = 400;
	
	private int clock;
	
	private final ArrayList<GestureHandlerListener> listeners;
	
	private Point downPoint;
	private int previousDownTime;
	private Point previousDownPoint;		// For recognizing the 2nd tap of a double tap
	private int previousPreviousDownTime;	// For recognizing the 2nd tap of a double tap
	private boolean isCharged;				// For recognizing charge swipes
	private boolean isSwiping;
	private boolean isDown;		// For recognizing the 2nd tap of a multi tap
	
	// FIXME: Game crash caused by dragging a few fingers around the screen with a shirt in between fingers and screen
	
	public GestureHandler()
	{
		listeners = new ArrayList<GestureHandlerListener>(3);
		
		clear();
	}
	
	public void update(int deltaTime)
	{
		clock += deltaTime;
	}
	
	public void addListener(GestureHandlerListener listener)
	{
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	
	public boolean removeListener(GestureHandlerListener listener)
	{
		return listeners.remove(listener);
	}
	
	public void addTouch(TouchEvent event)
	{
		final Point eventPoint = new Point(event.x, event.y);
		
		if (event.type == TouchEvent.TOUCH_DOWN)
		{
			if (isDown)
			{
				previousDownTime = 0;
				previousPreviousDownTime = 0;
				isCharged = false;
				isSwiping = false;
				downPoint = null;
				previousDownPoint = null;
				for (final GestureHandlerListener l : listeners)
					l.handleMultiTap();
				isDown = false;
			}
			else
			{
				previousDownPoint = downPoint;
				downPoint = eventPoint;
				previousPreviousDownTime = previousDownTime;
				previousDownTime = clock;
				isDown = true;
				for (final GestureHandlerListener l : listeners)
					l.handleTap(downPoint);
			}
		}
		else if (event.type == TouchEvent.TOUCH_DRAGGED)
		{
			if (downPoint != null && downPoint.distanceToPointSquared(eventPoint) >= MIN_SWIPE_DISTANCE_SQUARED)
			{
				isSwiping = true;
				
				if (!isCharged && (clock - previousDownTime) >= MIN_CHARGE_TIME)
					isCharged = true;
				
				final Line inProgress = new Line(downPoint, eventPoint);
				
				if (isCharged)
					for (final GestureHandlerListener l : listeners)
						l.chargeSwipeBuilding(inProgress);
				else
					for (final GestureHandlerListener l : listeners)
						l.swipeBuilding(inProgress);
			}
			else if (isSwiping)
			{
				isSwiping = false;
				
				for (final GestureHandlerListener l : listeners)
					l.swipeCancelled();
			}
		}
		else if (event.type == TouchEvent.TOUCH_UP)
		{
			if (isSwiping)
			{
				final Line newSwipe = new Line(downPoint, eventPoint);
				
				if (isCharged)
					for (final GestureHandlerListener l : listeners)
						l.handleChargeSwipe(newSwipe);
				else
					for (final GestureHandlerListener l : listeners)
						l.handleSwipe(newSwipe);
			}
			else if (clock - previousPreviousDownTime <= MAX_DOUBLE_TAP_TIME && previousDownPoint != null &&
					previousDownPoint.distanceToPointSquared(eventPoint) <= MAX_DOUBLE_TAP_DISTANCE_SQUARED)
			{
				for (final GestureHandlerListener l : listeners)
					l.handleDoubleTap(downPoint);
				previousDownTime = 0;
				previousPreviousDownTime = 0;
				downPoint = null;
				previousDownPoint = null;
			}
			
			isSwiping = false;
			isCharged = false;
			isDown = false;
		}
		else if (event.type == TouchEvent.TOUCH_HOLD)
		{
			
		}
	}
	
	public void clear()
	{
		clock = 0;
		previousDownTime = -10000;
		previousPreviousDownTime = -10000;
		
		downPoint = null;
		previousDownPoint = null;
		isCharged = false;
		isSwiping = false;
		isDown = false;
	}
	
	public boolean isSwiping() { return isSwiping; }
}