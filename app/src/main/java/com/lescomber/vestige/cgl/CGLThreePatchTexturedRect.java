package com.lescomber.vestige.cgl;

import android.graphics.Rect;

public class CGLThreePatchTexturedRect extends CGLTexturedRect {
	private CGLTexturedRect left;
	private CGLTexturedRect right;

	public CGLThreePatchTexturedRect(int textureHandle, float x, float y, int texWidth, int texHeight, Rect subTexRect) {
		super(textureHandle, x, y, texWidth, texHeight, subTexRect);

		left = null;
		right = null;
	}

	public void setLeft(int textureHandle, int texWidth, int texHeight, Rect subTexRect) {
		if (textureHandle < 0) {
			left = null;
			return;
		}

		left = new CGLTexturedRect(textureHandle, 0, 0, texWidth, texHeight, subTexRect);

		final float dx = vertexBuffer.get(0) - left.vertexBuffer.get(2);
		final float dy = vertexBuffer.get(1) - left.vertexBuffer.get(3);

		left.offset(dx, dy);
	}

	public void setRight(int textureHandle, int texWidth, int texHeight, Rect subTexRect) {
		if (textureHandle < 0) {
			right = null;
			return;
		}

		right = new CGLTexturedRect(textureHandle, 0, 0, texWidth, texHeight, subTexRect);

		final float dx = vertexBuffer.get(2) - right.vertexBuffer.get(0);
		final float dy = vertexBuffer.get(3) - right.vertexBuffer.get(1);

		right.offset(dx, dy);
	}

	@Override
	public void offset(float dx, float dy) {
		super.offset(dx, dy);

		if (left != null)
			left.offset(dx, dy);
		if (right != null)
			right.offset(dx, dy);
	}

	@Override
	public void rotate(float radians) {
		super.rotate(radians);

		// Rotate ends in-place
		if (left != null)
			left.rotate(radians);
		if (right != null)
			right.rotate(radians);

		// Reposition ends
		repositionEnds();
	}

	@Override
	public void scale(float widthRatio, float heightRatio) {
		float width = getWidth();
		if (left != null)
			width += left.getWidth();
		if (right != null)
			width += right.getWidth();

		width *= widthRatio;

		if (left != null)
			width -= left.getWidth();
		if (right != null)
			width -= right.getWidth();

		widthRatio = width / getWidth();

		super.scale(widthRatio, heightRatio);

		if (left != null)
			left.scale(1, heightRatio);
		if (right != null)
			right.scale(1, heightRatio);

		repositionEnds();
	}

	/**
	 * Re-attach shared vertices by offsetting left/right images (if they exist)
	 */
	private void repositionEnds() {
		// Calculate difference in x,y positions for a pair of vertices that should be attached. Then use those
		//differences to reposition all vertices for left/right images

		if (left != null) {
			final float dx = vertexBuffer.get(0) - left.vertexBuffer.get(2);
			final float dy = vertexBuffer.get(1) - left.vertexBuffer.get(3);

			left.offset(dx, dy);

			if (right != null)
				right.offset(-dx, -dy);
		} else if (right != null) {
			final float dx = vertexBuffer.get(2) - right.vertexBuffer.get(0);
			final float dy = vertexBuffer.get(3) - right.vertexBuffer.get(1);

			right.offset(dx, dy);
		}
	}

	@Override
	public void setAlpha(float alpha) {
		super.setAlpha(alpha);

		if (left != null)
			left.setAlpha(alpha);
		if (right != null)
			right.setAlpha(alpha);
	}

	@Override
	public void draw() {
		super.draw();

		if (left != null)
			left.draw();
		if (right != null)
			right.draw();
	}

	@Override
	public void bindDraw() {
		super.bindDraw();

		if (left != null)
			left.bindDraw();
		if (right != null)
			right.bindDraw();
	}
}