package com.lescomber.vestige.graphics;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;

public class ThreePatchSprite extends Sprite {
	private final SpriteTemplate leftTemplate;
	private final SpriteTemplate rightTemplate;

	public ThreePatchSprite(SpriteTemplate[] templates, float x, float y, boolean isVisible) {
		super(templates[0], x, y, false);

		leftTemplate = templates[1];
		rightTemplate = templates[2];

		setVisible(isVisible);
	}

	public ThreePatchSprite(SpriteTemplate[] templates, float x, float y) {
		this(templates, x, y, false);
	}

	public ThreePatchSprite(SpriteTemplate[] templates) {
		this(templates, 0, 0, false);
	}

	public ThreePatchSprite(ThreePatchSprite copyMe) {
		super(copyMe);

		leftTemplate = copyMe.leftTemplate;
		rightTemplate = copyMe.rightTemplate;

		isVisible = false;
		setVisible(copyMe.isVisible);
	}

	@Override
	public void scale(double widthRatio, double heightRatio) {
		float width = getWidth();
		width *= widthRatio;
		if (leftTemplate != null)
			width -= leftTemplate.getWidth();
		if (rightTemplate != null)
			width -= rightTemplate.getWidth();

		final double middleWidthRatio = (double) width / super.getWidth();

		info.widthScale *= middleWidthRatio;
		info.heightScale *= heightRatio;
		if (isVisible)
			SpriteManager.getInstance().scale(index, (float) widthRatio, (float) heightRatio);
	}

	@Override
	public void scaleTo(float width, float height) {
		if (info.widthScale <= 0 || info.heightScale <= 0) {
			float middleWidth = width;
			if (leftTemplate != null)
				middleWidth -= leftTemplate.getWidth();
			if (rightTemplate != null)
				middleWidth -= rightTemplate.getWidth();

			info.widthScale = middleWidth / getSubTexWidth();
			info.heightScale = height / getSubTexHeight();

			if (isVisible)
				Swapper.swapImages(this, this);
		} else
			super.scaleTo(width, height);
	}

	@Override
	public void scaleWidthTo(float width) {
		if (info.widthScale <= 0) {
			float middleWidth = width;
			if (leftTemplate != null)
				middleWidth -= leftTemplate.getWidth();
			if (rightTemplate != null)
				middleWidth -= rightTemplate.getWidth();

			info.widthScale = middleWidth / getSubTexWidth();

			if (isVisible)
				Swapper.swapImages(this, this);
		} else
			super.scaleWidthTo(width);
	}

	@Override
	public void setTexWidth(float percentage) {
		// Does not work for ThreePatchSprites
	}

	@Override
	public float getWidth() {
		float width = super.getWidth();

		if (leftTemplate != null)
			width += leftTemplate.getWidth();
		if (rightTemplate != null)
			width += rightTemplate.getWidth();

		return width;
	}

	@Override
	public float getHeight() {
		float height = super.getHeight();

		if (leftTemplate != null && leftTemplate.getHeight() > height)
			height = leftTemplate.getHeight();
		if (rightTemplate != null && rightTemplate.getHeight() > height)
			height = rightTemplate.getHeight();

		return height;
	}

	@Override
	public Sprite getSprite() {
		return this;
	}

	public SpriteTemplate getLeftTemplate() {
		return leftTemplate;
	}

	public SpriteTemplate getRightTemplate() {
		return rightTemplate;
	}

	@Override
	public void setVisible(boolean isVisible) {
		if (this.isVisible && !isVisible) {
			SpriteManager.getInstance().removeSprite(index);
			index = -1;
		} else if (!this.isVisible && isVisible) {
			index = SpriteManager.getInstance().newThreePatchSprite(info, leftTemplate, rightTemplate);
		}

		this.isVisible = isVisible;
	}
}