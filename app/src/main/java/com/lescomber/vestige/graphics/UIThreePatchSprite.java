package com.lescomber.vestige.graphics;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;

public class UIThreePatchSprite extends ThreePatchSprite
{
	public UIThreePatchSprite(SpriteTemplate[] templates, float x, float y)
	{
		super(templates, x, y, false);
		
		setLayerHeight(SpriteManager.UI_LAYER_OVER_THREE);	// Default to top layer
	}
	
	public UIThreePatchSprite(SpriteTemplate[] templates)
	{
		this(templates, 0, 0);
	}
	
	public UIThreePatchSprite(UIThreePatchSprite copyMe)
	{
		super(copyMe);
	}
	
	// Note: we don't bother calling the SpriteManager's setLayerHeight here because it would have no effect for the UI layer
	//because the UI layer is not sorted by layerHeight. Any calls to UIThreePatchSprite's setLayerHeight must be made prior to setting its
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