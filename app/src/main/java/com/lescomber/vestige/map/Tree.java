package com.lescomber.vestige.map;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.graphics.Sprite;

public class Tree extends RectangleObstacle {
	private static final float HALF_WIDTH = 15;
	private static final float HALF_HEIGHT = 8;

	public Tree(float x, float y) {
		super(x - HALF_WIDTH, y - HALF_HEIGHT, x + HALF_WIDTH, y + HALF_HEIGHT);

		// Select random tree image
		final int index = Util.rand.nextInt(6);
		setImage(new Sprite(SpriteManager.trees[index]));

		// Adjust tree image location based on tree selected
		if (index < 2)
			setImageOffsetY(-59);
		else if (index < 4)
			setImageOffsetY(-63);
		else
			setImageOffsetY(-55);
	}

	public Tree(Tree copyMe) {
		super(copyMe);
	}

	@Override
	public void becomeVisible() {
		setVisible(true);
	}

	@Override
	public Obstacle copy() {
		return new Tree(this);
	}
}