package com.lescomber.vestige.projectiles;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.SpriteManager.SpriteInfo;
import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.graphics.Sprite;
import com.lescomber.vestige.graphics.Swapper;

public class BeamSprite extends Sprite {
	private static final float HEIGHT_RATIO = SpriteManager.enemyLaserBody.getHeight() / SpriteManager.enemyLaserHead.getHeight();
	private static final float HEAD_WIDTH = 16;

	private float x;
	private float y;

	private Sprite bodySprite;
	private final SpriteInfo bodySpriteInfo;
	private Sprite nextBodySprite;    // For bodySprite transitions

	private Sprite headSprite;

	public BeamSprite() {
		super((SpriteInfo) null);

		bodySprite = new Sprite(SpriteManager.enemyLaserBody);
		headSprite = new Sprite(SpriteManager.enemyLaserHead);

		bodySprite.offset(bodySprite.getWidth() / 2, 0);
		headSprite.offset(bodySprite.getWidth() + (headSprite.getWidth() / 2), 0);

		bodySpriteInfo = bodySprite.getInfo();

		x = 0;
		y = 0;
	}

	public BeamSprite(BeamSprite copyMe) {
		super(copyMe);

		x = copyMe.x;
		y = copyMe.y;
		bodySprite = new Sprite(copyMe.bodySprite);
		bodySpriteInfo = headSprite.getInfo();
		if (copyMe.headSprite != null)
			headSprite = new Sprite(copyMe.headSprite);
	}

	public void loseHead() {
		if (headSprite == null)
			return;

		// Set up SpriteInfo changes for bodySprite (both scale and offset changes)
		final float direction = bodySprite.getDirection();
		bodySpriteInfo.widthScale += HEAD_WIDTH / bodySprite.getSubTexWidth();
		bodySpriteInfo.x += ((float) Math.cos(direction) * HEAD_WIDTH) / 2;
		bodySpriteInfo.y += ((float) Math.sin(direction) * HEAD_WIDTH) / 2;

		// Call the Swapper to make the bodySprite changes happen all at once
		nextBodySprite = new Sprite(bodySpriteInfo);
		Swapper.swapImages(bodySprite, nextBodySprite);
		bodySprite = nextBodySprite;

		headSprite.close();
		headSprite = null;
	}

	public void age(float width, float dx, float dy) {
		// Is the beam growing or shrinking? We will only move headSprite if it is growing
		final boolean isGrowing = width > getWidth();

		// Adjust width for space occupied by headSprite
		if (headSprite != null)
			width -= HEAD_WIDTH;

		// Set up SpriteInfo changes for bodySprite (both scale and offset changes)
		bodySpriteInfo.widthScale = width / bodySprite.getSubTexWidth();
		bodySpriteInfo.x += dx;
		bodySpriteInfo.y += dy;

		// Call the Swapper to make the bodySprite changes happen all at once
		nextBodySprite = new Sprite(bodySpriteInfo);
		Swapper.swapImages(bodySprite, nextBodySprite);
		bodySprite = nextBodySprite;

		// Update x, y
		x += dx;
		y += dy;

		// If the beam is still growing, offset headSprite by double dx and dy since headSprite moves by an amount equal to the
		//total change in width rather than just the movement of the center of the beam (which is represented by dx, dy)
		if (headSprite != null && isGrowing)
			headSprite.offset(dx * 2, dy * 2);
	}

	@Override
	public void offset(float dx, float dy) {
		x += dx;
		y += dy;
		bodySprite.offset(dx, dy);
		if (headSprite != null)
			headSprite.offset(dx, dy);
	}

	@Override
	public void offsetTo(float x, float y) {
		offset(x - this.x, y - this.y);
	}

	@Override
	public void offsetTo(Point p) {
		offsetTo(p.x, p.y);
	}

	@Override
	public void rotate(float radians) {
		// Rotate body
		bodySprite.rotateAbout(radians, x, y);

		// Rotate head
		if (headSprite != null)
			headSprite.rotateAbout(radians, x, y);
	}

	@Override
	public void rotateTo(float radians) {
		rotate(radians - getDirection());
	}

	@Override
	public void rotateAbout(float radians, float rotateX, float rotateY) {
		// Update coords
		final Point xy = new Point(x, y);
		Point.rotate(xy, radians, rotateX, rotateY);
		x = xy.x;
		y = xy.y;

		// Update image(s)
		bodySprite.rotateAbout(radians, rotateX, rotateY);
		if (headSprite != null)
			headSprite.rotateAbout(radians, rotateX, rotateY);
	}

	@Override
	public void scale(double widthRatio, double heightRatio) {
		if (heightRatio == 1)
			scaleWidthTo((float) (widthRatio * getWidth()));
		else if (widthRatio == 1)
			scaleHeightTo((float) (heightRatio * getHeight()));
		else
			scaleTo((float) (widthRatio * getWidth()), (float) (heightRatio * getHeight()));
	}

	@Override
	public void scaleTo(float width, float height) {
		scaleWidthTo(width);
		scaleHeightTo(height);
	}

	@Override
	public void scaleWidthTo(float width) {
		final float dWidth = width - getWidth();
		bodySprite.scaleWidthTo(bodySprite.getWidth() + dWidth);

		if (headSprite != null) {
			Point headSpriteLocation = new Point(headSprite.getX(), headSprite.getY());
			headSpriteLocation = headSpriteLocation.getPointFromDirection(getDirection(), dWidth / 2);
			headSprite.offsetTo(headSpriteLocation);
		}
	}

	@Override
	public void scaleHeightTo(float height) {
		bodySprite.scaleHeightTo(HEIGHT_RATIO * height);
		if (headSprite != null)
			headSprite.scaleHeightTo(height);
	}

	@Override
	public void setAlpha(float alpha) {
		bodySprite.setAlpha(alpha);
		if (headSprite != null)
			headSprite.setAlpha(alpha);
	}

	@Override    // Layer height stays at 0 for BeamSprites
	public void setLayerHeight(int layerHeight) {
	}

	@Override
	public int getLayerHeight() {
		return bodySprite.getLayerHeight();
	}

	/**
	 * Note: doesn't work for BeamSprite
	 */
	@Override
	public void setTexWidth(float percentage) {
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public float getY() {
		return y;
	}

	@Override
	public float getDirection() {
		return bodySprite.getDirection();
	}

	@Override
	public int getIndex() {
		return bodySprite.getIndex();
	}

	@Override
	public SpriteInfo getInfo() {
		return bodySprite.getInfo();
	}

	@Override
	public Sprite getSprite() {
		return bodySprite;
	}

	@Override
	public SpriteTemplate getTemplate() {
		return bodySprite.getTemplate();
	}

	@Override
	public float getWidth() {
		if (headSprite != null)
			return bodySprite.getWidth() + HEAD_WIDTH;
		else
			return bodySprite.getWidth();
	}

	@Override
	public float getHeight() {
		if (headSprite != null)
			return headSprite.getHeight();
		else
			return bodySprite.getHeight() * (1 / HEIGHT_RATIO);
	}

	@Override
	public float getWidthScale() {
		return getWidth() / (SpriteManager.enemyLaserBody.getWidth() + SpriteManager.enemyLaserHead.getWidth());
	}

	@Override
	public float getHeightScale() {
		return getHeight() / (SpriteManager.enemyLaserHead.getHeight());
	}

	@Override
	public float getSubTexWidth() {
		return 0;
	}        // Doesn't work

	@Override
	public float getSubTexHeight() {
		return 0;
	}        // Doesn't work

	@Override
	public void setVisible(boolean isVisible) {
		if (bodySprite != null)
			bodySprite.setVisible(isVisible);

		if (headSprite != null)
			headSprite.setVisible(isVisible);
	}

	@Override
	public boolean isVisible() {
		return bodySprite.isVisible();
	}

	@Override
	public void close() {
		bodySprite.close();
		if (headSprite != null)
			headSprite.close();
	}

	@Override
	public void wasReplaced() {
		bodySprite.wasReplaced();
		if (headSprite != null)
			headSprite.setVisible(false);
	}

	@Override
	public void wasAdded(int newIndex) {
		bodySprite.wasAdded(newIndex);
		if (headSprite != null)
			headSprite.setVisible(true);
	}

	@Override
	public BeamSprite copy() {
		return new BeamSprite(this);
	}
}