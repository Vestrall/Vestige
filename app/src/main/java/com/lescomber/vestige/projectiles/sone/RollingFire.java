package com.lescomber.vestige.projectiles.sone;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.Screen;
import com.lescomber.vestige.geometry.Hitbox;
import com.lescomber.vestige.geometry.Rectangle;
import com.lescomber.vestige.projectiles.AreaEffect;
import com.lescomber.vestige.projectiles.FireAnimation;
import com.lescomber.vestige.screens.OptionsScreen;

import java.util.ArrayList;
import java.util.List;

public class RollingFire extends AreaEffect
{
	public static final float HEIGHT = 120;
	private static final float DPS[] = new float[] { 10, 15, 20 };
	private static final double MAX_DURATION = 3;
	private static final float X_GAP = SpriteManager.groundFire[0].getWidth() * 0.7f;
	private static final float Y_GAP = SpriteManager.groundFire[0].getHeight() * 0.8f;
	private static final float X_VARIANCE = 15;
	private static final float Y_VARIANCE = 12;

	private final float topY;

	private final ArrayList<FireAnimation> anims;

	private static final int MAX_COUNTDOWN[] = new int[] { 60, 50, 40 };
	private int countdown;

	private final Rectangle hitboxRect;

	public RollingFire(float y)
	{
		super(DPS[OptionsScreen.difficulty], MAX_DURATION);

		topY = y - (HEIGHT / 2);

		anims = new ArrayList<FireAnimation>(50);

		countdown = 0;
		hitboxRect = new Rectangle(Screen.WIDTH, topY, Screen.WIDTH, topY + HEIGHT);
		hitbox = new Hitbox(hitboxRect);
	}

	public RollingFire(RollingFire copyMe)
	{
		super(copyMe);

		this.topY = copyMe.topY;

		anims = new ArrayList<FireAnimation>(50);
		for (final FireAnimation fa : copyMe.anims)
			anims.add(new FireAnimation(fa));

		countdown = copyMe.countdown;
		hitboxRect = new Rectangle(copyMe.hitboxRect);
		hitbox = new Hitbox(hitboxRect);
	}

	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);

		for (final FireAnimation fa : anims)
			fa.update(deltaTime);

		countdown -= deltaTime;
		if (countdown <= 0)
		{
			countdown += MAX_COUNTDOWN[OptionsScreen.difficulty];

			createNextColumn();

			// Check if we've reached the left edge of the screen
			if (hitboxRect.left < 0)
				countdown = Integer.MAX_VALUE;
		}
	}

	private void createNextColumn()
	{
		final List<FireAnimation> newAnims = new ArrayList<FireAnimation>(5);

		final float animX = hitboxRect.left - (X_GAP / 2);

		// Expand hitbox
		hitboxRect.left -= X_GAP;

		final float imageOffsetY = FireAnimation.IMAGE_OFFSET_Y / 2;

		// Add FireAnimations
		for (float y = topY + imageOffsetY; y < topY + HEIGHT + imageOffsetY; y += Y_GAP)
		{
			final FireAnimation anim = new FireAnimation();
			anim.offsetTo(animX, y);
			anim.setLayerHeight(Math.round(y));
			anim.setVisible(true);
			anim.play();
			newAnims.add(anim);
		}

		FireAnimation.varyLocations(newAnims, X_VARIANCE, Y_VARIANCE);
		anims.addAll(newAnims);
	}

	@Override
	public void close()
	{
		for (final FireAnimation fa : anims)
			fa.close();

		super.close();
	}

	@Override
	public RollingFire copy()
	{
		return new RollingFire(this);
	}
}