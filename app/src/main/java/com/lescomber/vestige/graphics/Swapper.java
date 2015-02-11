package com.lescomber.vestige.graphics;

import com.lescomber.vestige.crossover.SpriteManager;

public class Swapper
{
	public static void swapImages(Image removeMe, Image addMe)
	{
		if (removeMe != null && removeMe.getSprite().isVisible())
		{
			if (addMe != null)
			{
				if (addMe.isVisible() && (addMe.getIndex() != removeMe.getIndex()))
					addMe.setVisible(false);
				
				if (addMe instanceof ThreePatchSprite)
				{
					SpriteManager.getInstance().replaceSprite(removeMe.getIndex(), addMe.getSprite().getInfo(),
							((ThreePatchSprite)addMe).getLeftTemplate(), ((ThreePatchSprite)addMe).getRightTemplate());
				}
				else
					SpriteManager.getInstance().replaceSprite(removeMe.getIndex(), addMe.getSprite().getInfo());
				
				addMe.wasAdded(removeMe.getIndex());
				if (removeMe != addMe)
					removeMe.wasReplaced();
				
				// If addMe and removeMe refer to the same image, do not addMe.setVisible(false) or else we'll set
				//removeMe.setVisible(false) right before we assume it is visible
				/*if (addMe.isVisible() && (addMe.getIndex() != removeMe.getIndex()))
					addMe.setVisible(false);
				
				SpriteManager.getInstance().replaceSprite(removeMe.getIndex(), addMe.getSprite().getInfo());
				addMe.wasAdded(removeMe.getIndex());
				if (removeMe != addMe)
					removeMe.wasReplaced();*/
			}
			else
			{
				SpriteManager.getInstance().removeSprite(removeMe.getIndex());
				removeMe.wasReplaced();
			}
		}
		else
		{
			if (addMe != null)
			{
				if (addMe.isVisible())
					addMe.setVisible(false);
				final int index;
				if (addMe instanceof ThreePatchSprite)
				{
					index = SpriteManager.getInstance().newThreePatchSprite(addMe.getSprite().getInfo(),
							((ThreePatchSprite)addMe).getLeftTemplate(), ((ThreePatchSprite)addMe).getRightTemplate());
				}
				else
					index = SpriteManager.getInstance().newSprite(addMe.getSprite().getInfo());
				addMe.wasAdded(index);
				
				/*if (addMe.isVisible())
					addMe.setVisible(false);
				final int index = SpriteManager.getInstance().newSprite(addMe.getSprite().getInfo());
				addMe.wasAdded(index);*/
			}
		}
	}
}