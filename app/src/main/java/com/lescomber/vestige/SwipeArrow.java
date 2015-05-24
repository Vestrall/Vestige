package com.lescomber.vestige;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.gestures.GestureHandler;
import com.lescomber.vestige.gestures.GestureHandlerListener;
import com.lescomber.vestige.graphics.UISwingSprite;
import com.lescomber.vestige.units.Player;

public class SwipeArrow implements GestureHandlerListener {
	private final GestureHandler gestureHandler;
	private Player player;

	private final UISwingSprite swipeArrow;

	// For tutorial screen purposes
	private boolean isDisabled;
	private boolean isBothSwipes;

	public SwipeArrow(GestureHandler gestureHandler) {
		this.gestureHandler = gestureHandler;
		gestureHandler.addListener(this);

		swipeArrow = new UISwingSprite(SpriteManager.swipeArrow, 0, 0, -(SpriteManager.swipeArrow.getWidth() / 2), 0);
		swipeArrow.scale(0.65, 1);
		swipeArrow.setLayerHeight(SpriteManager.UI_LAYER_UNDER_TWO);

		isDisabled = false;
		isBothSwipes = false;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void update(int deltaTime) {
		if (!gestureHandler.isSwiping()) {
			if (player.isSwipeQueued())
				setVisible(true);
			else if (player.isChargeSwipeQueued())
				setVisible(isBothSwipes);
			else
				setVisible(false);
		}
	}

	@Override
	public void handleTap(Point tapPoint) {
		setVisible(false);
	}

	@Override
	public void swipeBuilding(Line swipe) {
		swipeArrow.offsetTo(swipe.getStart());
		swipeArrow.rotateTo(swipe.getDirection());
		setVisible(true);
	}

	@Override
	public void chargeSwipeBuilding(Line swipe) {
		if (isBothSwipes) {
			swipeArrow.offsetTo(swipe.getStart());
			swipeArrow.rotateTo(swipe.getDirection());
		}
		setVisible(isBothSwipes);
	}

	@Override
	public void swipeCancelled() {
		setVisible(false);
	}

	@Override
	public void handleSwipe(Line swipe) {
	}

	@Override
	public void handleChargeSwipe(Line swipe) {
	}

	@Override
	public void handleDoubleTap(Point tapPoint) {
		setVisible(false);
	}

	@Override
	public void handleMultiTap() {
	}

	public void setDisabled(boolean isDisabled) {
		this.isDisabled = isDisabled;
	}

	public void setBothSwipes(boolean isBothSwipes) {
		this.isBothSwipes = isBothSwipes;
	}

	public void setVisible(boolean isVisible) {
		if (!isDisabled || !isVisible)
			swipeArrow.setVisible(isVisible);
	}
}