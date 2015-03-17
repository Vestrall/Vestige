package com.lescomber.vestige.graphics;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.SpriteManager.SpriteInfo;
import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;

public class UISprite extends Sprite
{
	public UISprite(SpriteTemplate template, float x, float y)
	{
		super(template, x, y, false);

		setLayerHeight(SpriteManager.UI_LAYER_OVER_THREE);    // Default to top layer
	}

	public UISprite(SpriteTemplate template)
	{
		this(template, 0, 0);
	}

	public UISprite(SpriteInfo info)
	{
		super(info);
	}

	public UISprite(UISprite copyMe)
	{
		super(copyMe);
	}

	// Note: we don't bother calling the SpriteManager's setLayerHeight here because it would have no effect for the UI layer
	//because the UI layer is not sorted by layerHeight. Any calls to UISprite's setLayerHeight must be made prior to setting its
	//visibility to true in order to have any effect
	@Override
	public void setLayerHeight(int layerHeight)
	{
		if (layerHeight == 1)
			info.layerHeight = SpriteManager.UI_LAYER_UNDER_ONE;
		else if (layerHeight == 2)
			info.layerHeight = SpriteManager.UI_LAYER_UNDER_TWO;
		else if (layerHeight == 3)
			info.layerHeight = SpriteManager.UI_LAYER_OVER_ONE;
		else if (layerHeight == 4)
			info.layerHeight = SpriteManager.UI_LAYER_OVER_TWO;
		else if (layerHeight < SpriteManager.UI_LAYER_UNDER_ONE)
			info.layerHeight = SpriteManager.UI_LAYER_OVER_THREE;
		else
			info.layerHeight = layerHeight;
	}
}