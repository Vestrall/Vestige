package com.lescomber.vestige.projectiles;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.Util;

import java.util.ArrayList;

public class WildFire extends AreaEffect {
	private static final float STANDARD_X_GAP = SpriteManager.groundFire[0].getWidth() * 0.7f;
	private static final float STANDARD_Y_GAP = SpriteManager.groundFire[0].getHeight() * 0.8f;
	private static final float X_VARIANCE = 25;
	private static final float Y_VARIANCE = 10;

	private final ArrayList<FireAnimation>[] anims;

	@SuppressWarnings("unchecked")
	public WildFire(float x, float y, float width, float height, float dps, double durationSeconds) {
		super(width, height, dps, durationSeconds);

		offsetTo(x, y);

		// TODO: Consider better row/col calculation
		final int rows = (int) Math.ceil(height / STANDARD_X_GAP) + 1;
		final int cols = (int) Math.ceil(width / STANDARD_Y_GAP) + 1;

		final float xGap = width / cols;
		final float yGap = height / rows;

		final float startX = x - (width / 2) + (xGap / 2) - (X_VARIANCE / 2);
		float curX = startX;
		float curY = y - (height / 2) + (yGap / 2) - (Y_VARIANCE / 2);

		FireAnimation fa;
		anims = new ArrayList[rows];
		for (int i = 0; i < rows; i++) {
			anims[i] = new ArrayList<FireAnimation>();

			for (int j = 0; j < cols; j++) {
				fa = new FireAnimation();
				fa.setDuration((int) (durationSeconds * 1000));
				fa.offsetTo(curX + (Util.rand.nextFloat() * X_VARIANCE), curY + (Util.rand
						.nextFloat() * Y_VARIANCE) + (FireAnimation.IMAGE_OFFSET_Y / 2));
				fa.setLayerHeight(Math.round(fa.getY()));
				anims[i].add(fa);

				curX += xGap;
			}

			curX = startX;
			curY += yGap;
		}
	}

	public WildFire(float width, float height, float dps, double durationSeconds) {
		this(0, 0, width, height, dps, durationSeconds);
	}

	@SuppressWarnings("unchecked")
	public WildFire(WildFire copyMe) {
		super(copyMe);

		anims = new ArrayList[copyMe.anims.length];
		for (int i = 0; i < copyMe.anims.length; i++) {
			anims[i] = new ArrayList<FireAnimation>();
			for (final FireAnimation fa : copyMe.anims[i])
				anims[i].add(new FireAnimation(fa));
		}
	}

	@Override
	public void update(int deltaTime) {
		super.update(deltaTime);

		for (int i = 0; i < anims.length; i++) {
			for (final FireAnimation fa : anims[i])
				fa.update(deltaTime);
		}
	}

	@Override
	public void offset(float dx, float dy) {
		super.offset(dx, dy);

		if (anims != null) {
			for (final ArrayList<FireAnimation> row : anims) {
				for (final FireAnimation fa : row)
					fa.offset(dx, dy);
			}
		}
	}

	@Override
	public void setVisible(boolean isVisible) {
		for (int i = 0; i < anims.length; i++) {
			for (final FireAnimation fa : anims[i]) {
				fa.setVisible(isVisible);

				if (isVisible)
					fa.play();
				else
					fa.stop();
			}
		}
	}

	@Override
	public void close() {
		for (int i = 0; i < anims.length; i++) {
			for (final FireAnimation fa : anims[i])
				fa.close();
		}

		super.close();
	}

	@Override
	public WildFire copy() {
		return new WildFire(this);
	}
}