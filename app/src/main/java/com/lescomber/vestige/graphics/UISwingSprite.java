package com.lescomber.vestige.graphics;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.SpriteManager.SpriteInfo;
import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;

public class UISwingSprite extends SwingSprite
{
	public UISwingSprite(SpriteTemplate template, float x, float y, float offsetX, float offsetY)
	{
		super(template, x, y, offsetX, offsetY, false);
		
		setLayerHeight(SpriteManager.UI_LAYER_OVER_THREE);	// Default to top layer
	}
	
	public UISwingSprite(SpriteTemplate template, float offsetX, float offsetY)
	{
		this(template, 0, 0, offsetX, offsetY);
	}
	
	public UISwingSprite(SpriteInfo info, float offsetX, float offsetY)
	{
		super(info, offsetX, offsetY);
	}
	
	public UISwingSprite(SwingSprite copyMe)
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