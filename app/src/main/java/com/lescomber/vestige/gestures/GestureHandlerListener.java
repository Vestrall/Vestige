package com.lescomber.vestige.gestures;

import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;

public interface GestureHandlerListener {
	void handleTap(Point tapPoint);

	void swipeBuilding(Line swipe);

	void chargeSwipeBuilding(Line swipe);

	void swipeCancelled();

	void handleSwipe(Line swipe);

	void handleChargeSwipe(Line swipe);

	void handleDoubleTap(Point tapPoint);

	void handleMultiTap();
}