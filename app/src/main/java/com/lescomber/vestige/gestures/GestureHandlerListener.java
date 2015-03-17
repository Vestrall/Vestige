package com.lescomber.vestige.gestures;

import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;

public interface GestureHandlerListener
{
	public void handleTap(Point tapPoint);

	public void swipeBuilding(Line swipe);

	public void chargeSwipeBuilding(Line swipe);

	public void swipeCancelled();

	public void handleSwipe(Line swipe);

	public void handleChargeSwipe(Line swipe);

	public void handleDoubleTap(Point tapPoint);

	public void handleMultiTap();
}