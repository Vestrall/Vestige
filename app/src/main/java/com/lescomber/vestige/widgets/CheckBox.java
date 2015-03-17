package com.lescomber.vestige.widgets;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.Input.TouchEvent;
import com.lescomber.vestige.geometry.Rectangle;
import com.lescomber.vestige.graphics.Swapper;
import com.lescomber.vestige.graphics.UISprite;

public class CheckBox extends Widget
{
	public static final String CHECKED = "checked";
	public static final String UNCHECKED = "unchecked";

	private final UISprite offSprite;
	private final UISprite onSprite;

	private final Rectangle rect;

	private boolean isChecked;

	public CheckBox(float x, float y)
	{
		rect = new Rectangle(0, 0, SpriteManager.checkBoxOff.getWidth(), SpriteManager.checkBoxOn.getHeight());
		rect.offsetTo(x, y);

		offSprite = new UISprite(SpriteManager.checkBoxOff, x, y);
		onSprite = new UISprite(SpriteManager.checkBoxOn, x, y);

		isChecked = false;
	}

	@Override
	public void handleEvent(TouchEvent event)
	{
		if (event.type == TouchEvent.TOUCH_UP && rect.contains(event.x, event.y))
		{
			toggle();
			final String command = isChecked ? CHECKED : UNCHECKED;
			notifyListeners(new WidgetEvent(this, command));
		}
	}

	public void setValue(boolean isChecked)
	{
		if (isVisible())
		{
			if (isChecked)
				Swapper.swapImages(offSprite, onSprite);
			else
				Swapper.swapImages(onSprite, offSprite);
		}

		this.isChecked = isChecked;
	}

	private void toggle()
	{
		if (isVisible())
		{
			if (isChecked)
				Swapper.swapImages(onSprite, offSprite);
			else
				Swapper.swapImages(offSprite, onSprite);
		}

		isChecked = !isChecked;
	}

	public boolean isChecked()
	{
		return isChecked;
	}

	@Override
	public void setVisible(boolean isVisible)
	{
		final UISprite curSprite = isChecked ? onSprite : offSprite;
		curSprite.setVisible(isVisible);
	}

	public boolean isVisible()
	{
		return (isChecked ? onSprite : offSprite).isVisible();
	}
}