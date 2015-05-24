package com.lescomber.vestige.playerabilities;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.geometry.Line;
import com.lescomber.vestige.graphics.Sprite;

public class SOneDoubleTapLaser {
	private static final float INITIAL_LENGTH = 20;
	private static final float WIDTH_PER_MS = 3.25f;

	private final Sprite image;
	private final float direction;
	private final float cos;
	private final float sin;
	private final float maxWidth;
	private boolean isExpanding;

	public SOneDoubleTapLaser(Line path) {
		image = new Sprite(SpriteManager.sOneDoubleTapLaser);
		direction = path.getDirection();
		image.rotateTo(direction);
		cos = (float) Math.cos(direction);
		sin = (float) Math.sin(direction);
		image.scaleWidthTo(INITIAL_LENGTH);
		image.offsetTo(path.getStart().x + (cos * INITIAL_LENGTH), path.getStart().y + (sin * INITIAL_LENGTH));
		image.setVisible(true);
		maxWidth = (float) path.getLength();
		isExpanding = true;
	}

	public void update(int deltaTime) {
		float dWidth = deltaTime * WIDTH_PER_MS;

		if (isExpanding) {
			if (image.getWidth() + dWidth >= maxWidth) {
				dWidth = maxWidth - image.getWidth();
				isExpanding = false;
			}

			image.scaleWidthTo(image.getWidth() + dWidth);
		} else {
			if (image.getWidth() - dWidth <= 20)
				image.setVisible(false);
			else
				image.scaleWidthTo(image.getWidth() - dWidth);
		}

		image.offset(cos * (dWidth / 2), sin * (dWidth / 2));
	}

	public boolean isFinished() {
		return !image.isVisible();
	}
}