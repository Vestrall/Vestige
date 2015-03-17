package com.lescomber.vestige.map;

import com.lescomber.vestige.Entity;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.graphics.Sprite;
import com.lescomber.vestige.graphics.SpriteAnimation;

public class Portal extends Entity
{
	private int delay;    // Short delay (in ms) before portal is active (to avoid confusion caused by standing on its spawn
	//point when the level ends)

	public Portal(float x, float y)
	{
		super();

		offsetTo(x, y);
		setImage(new Sprite(SpriteManager.portal[0]));

		// Init animation
		final SpriteAnimation portalAnim = new SpriteAnimation(SpriteManager.portal);
		portalAnim.setSequenceLimit(-1);
		portalAnim.setFadeIn(0, 2.5);
		setImage(portalAnim);

		createRectangleHitbox(48, 36);
		setImageOffsetY(-20);

		delay = 2500;
	}

	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);

		delay -= deltaTime;
	}

	public boolean isReady()
	{
		return (isVisible() && delay <= 0);
	}
}