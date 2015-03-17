package com.lescomber.vestige.map;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.graphics.Sprite;

public class Wall extends RectangleObstacle
{
	private static final int WALL_SECTION_WIDTH = 30;    // For all wall pieces (top, bottom, mid)
	private static final int WALL_TOP_TRANSPARENCY = 4;
	private static final int WALL_TOP_HEIGHT = 14;    // Bottom pieces will be considered to have 2x the height of tops
	private static final int WALL_BOTTOM_TRANSPARENCY = 5;

	private Sprite[] sprites;

	public Wall(float left, float top, float right, float bottom)
	{
		super(left, top, right, bottom);

		createSprites(left, top, right, bottom);
	}

	public Wall(Wall copyMe)
	{
		super(copyMe);

		sprites = new Sprite[copyMe.sprites.length];
		for (int i = 0; i < copyMe.sprites.length; i++)
			sprites[i] = new Sprite(copyMe.sprites[i]);
	}

	private void createSprites(float left, float top, float right, float bottom)
	{
		final float width = right - left;
		final float height = bottom - top;

		final double exactCols = width / WALL_SECTION_WIDTH;
		final double exactTopRows = height / WALL_TOP_HEIGHT;    // Total possible top rows without leaving space for bottoms

		final int actualCols = exactCols < 0.5 ? 1 : (int) Math.round(exactCols);
		final int actualRows = exactTopRows < 2.5 ? 2 : (int) Math.round(exactTopRows);    // min 2 rows for bottoms-only walls

		final double scaleX = exactCols / actualCols;
		final double scaleY = exactTopRows / actualRows;

		final double scaledWidth = scaleX * WALL_SECTION_WIDTH;
		final double scaledTopHeight = scaleY * WALL_TOP_HEIGHT;
		final double scaledTopTransparency = scaleY * WALL_TOP_TRANSPARENCY;
		final double scaledBottomTransparency = scaleY * WALL_BOTTOM_TRANSPARENCY;

		// Calculate actual coordinates for sprite starting positions
		// startY is one top row higher than we'll start with
		final double startY = getHitbox().getTop() - (0.5 * scaledTopHeight) - (0.5 * scaledTopTransparency);
		final double startX = getHitbox().getLeft() + (0.5 * scaledWidth);

		// Convert actual coordinates to offsets relative to wall center
		double curYOffset = startY - getY();
		final double startXOffset = startX - getX();

		//===============
		// Create sprites
		//===============

		// Note: bottom row counts as 2 actualRows because it is roughly twice the height of other rows
		sprites = new Sprite[(actualRows - 1) * actualCols];
		int arrayIndex = 0;

		// Create top row (if applicable)
		if (actualRows > 2)
		{
			curYOffset += scaledTopHeight;

			if (actualCols <= 1)    // Mid only
			{
				final Sprite topMid = new Sprite(SpriteManager.wallTops[1], getX(), getY() + (float) curYOffset);
				topMid.scale(scaleX, scaleY);
				sprites[arrayIndex++] = topMid;
			}
			else
			{
				double curXOffset = startXOffset;

				// Top left sprite
				final Sprite topLeft = new Sprite(SpriteManager.wallTops[0], getX() + (float) curXOffset, getY() + (float) curYOffset);
				topLeft.scale(scaleX, scaleY);
				sprites[arrayIndex++] = topLeft;
				curXOffset += scaledWidth;

				// Top mid sprites
				for (int j = 1; j < actualCols - 1; j++)
				{
					final Sprite topMid = new Sprite(SpriteManager.wallTops[1], getX() + (float) curXOffset, getY() + (float) curYOffset);
					topMid.scale(scaleX, scaleY);
					sprites[arrayIndex++] = topMid;
					curXOffset += scaledWidth;
				}

				// Top right sprite
				final Sprite topRight = new Sprite(SpriteManager.wallTops[2], getX() + (float) curXOffset, getY() + (float) curYOffset);
				topRight.scale(scaleX, scaleY);
				sprites[arrayIndex++] = topRight;
			}
		}

		// Create middle rows (if applicable)
		if (actualRows > 3)
		{
			for (int i = 1; i < actualRows - 2; i++)
			{
				curYOffset += scaledTopHeight;
				double curXOffset = startXOffset;

				for (int j = 0; j < actualCols; j++)
				{
					final Sprite mid = new Sprite(SpriteManager.wallMid, getX() + (float) curXOffset, getY() + (float) curYOffset);
					mid.scale(scaleX, scaleY);
					sprites[arrayIndex++] = mid;
					curXOffset += scaledWidth;
				}
			}
		}

		// Create bottom row (always applicable)
		curYOffset += (0.5 * scaledTopHeight) + scaledTopHeight - (0.5 * scaledBottomTransparency);

		if (actualCols <= 1)    // Mid only
		{
			final Sprite bottomMid = new Sprite(SpriteManager.wallBottoms[1], getX(), getY() + (float) curYOffset);
			bottomMid.scale(scaleX, scaleY);
			sprites[arrayIndex++] = bottomMid;
		}
		else
		{
			double curXOffset = startXOffset;

			// Bottom left sprite
			final Sprite bottomLeft = new Sprite(SpriteManager.wallBottoms[0], getX() + (float) curXOffset, getY() + (float) curYOffset);
			bottomLeft.scale(scaleX, scaleY);
			sprites[arrayIndex++] = bottomLeft;
			curXOffset += scaledWidth;

			// Bottom mid sprites
			for (int j = 1; j < actualCols - 1; j++)
			{
				final Sprite bottomMid = new Sprite(SpriteManager.wallBottoms[1], getX() + (float) curXOffset, getY() + (float) curYOffset);
				bottomMid.scale(scaleX, scaleY);
				sprites[arrayIndex++] = bottomMid;
				curXOffset += scaledWidth;
			}

			// Bottom right sprite
			final Sprite bottomRight = new Sprite(SpriteManager.wallBottoms[2], getX() + (float) curXOffset, getY() + (float) curYOffset);
			bottomRight.scale(scaleX, scaleY);
			sprites[arrayIndex++] = bottomRight;
		}
	}

	@Override
	public void becomeVisible()
	{
		for (final Sprite s : sprites)
			SpriteManager.getInstance().addBackgroundSprite(s.getTemplate(), s.getX(), s.getY());
	}

	@Override
	public void setVisible(boolean isVisible)
	{
		for (final Sprite gs : sprites)
			gs.setVisible(isVisible);
	}

	@Override
	public Wall copy()
	{
		return new Wall(this);
	}
}