package com.lescomber.vestige.widgets;

import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.framework.Input.TouchEvent;
import com.lescomber.vestige.geometry.Rectangle;
import com.lescomber.vestige.graphics.UISprite;

public class Slider extends Widget
{
	private static final float BAR_KNOB_HEIGHT_RATIO =
			(float) SpriteManager.sliderEmpty.getHeight() / SpriteManager.sliderKnob.getHeight();
	private static final float KNOB_INDENT_RATIO = 6f / 30;
	private static final float RECT_SCALE = 1.4f;    // Enlarge hitboxes by this scaling factor to make it easier to hit

	private final UISprite barEmpty;
	private final UISprite barFull;

	private final UISprite knob;
	private final Rectangle knobRect;
	private boolean isDragging;

	private final Rectangle boundingBox;

	private final float knobMinX;
	private final float knobMaxX;

	private int value;
	private final int minValue;
	private final int maxValue;

	public Slider(float x, float y, float width, float height, int minValue, int maxValue)
	{
		super();

		this.minValue = minValue;
		this.maxValue = maxValue;

		boundingBox = new Rectangle();
		boundingBox.offsetTo(x, y);
		boundingBox.scaleTo(width, height * RECT_SCALE);

		// Init Sprites
		barEmpty = new UISprite(SpriteManager.sliderEmpty, x, y);
		barEmpty.setLayerHeight(SpriteManager.UI_LAYER_OVER_ONE);
		barEmpty.scaleTo(width, height * BAR_KNOB_HEIGHT_RATIO);
		barFull = new UISprite(SpriteManager.sliderFull, x, y);
		barFull.setLayerHeight(SpriteManager.UI_LAYER_OVER_TWO);
		barFull.scaleTo(width, height * BAR_KNOB_HEIGHT_RATIO);
		knob = new UISprite(SpriteManager.sliderKnob, x, y);
		knob.setLayerHeight(SpriteManager.UI_LAYER_OVER_THREE);
		final double knobScale = height / SpriteManager.sliderKnob.getHeight();
		knob.scale(knobScale, knobScale);
		knobMinX = x - (width / 2) + (knob.getWidth() * KNOB_INDENT_RATIO);
		knobMaxX = x + (width / 2) - (knob.getWidth() * KNOB_INDENT_RATIO);

		// Init knob Rectangle
		knobRect = new Rectangle();
		knobRect.offsetTo(x, y);
		knobRect.scaleTo(knob.getWidth() * RECT_SCALE, knob.getHeight() * RECT_SCALE);

		setValue((minValue + maxValue) / 2);
	}

	@Override
	public void handleEvent(TouchEvent e)
	{
		// If not visible, Slider cannot be interacted with
		if (!knob.isVisible())
			return;

		if (e.type == TouchEvent.TOUCH_DOWN)
		{
			if (knobRect.contains(e.x, e.y))
				isDragging = true;

				// e is not in the knob so if it's inside the boundingBox it must be on the line
			else if (boundingBox.contains(e.x, e.y))
				setKnobX(e.x);
		}
		else if (e.type == TouchEvent.TOUCH_UP)
			isDragging = false;
		else if (e.type == TouchEvent.TOUCH_DRAGGED)
		{
			if (isDragging)
				setKnobX(e.x);
		}
	}

	private void setKnobX(float x)
	{
		final int newValue = Math.round(((x - knobMinX) / (knobMaxX - knobMinX)) * (maxValue - minValue));
		setValue(newValue);
	}

	public void setValue(int value)
	{
		if (value > maxValue)
			value = maxValue;
		else if (value < minValue)
			value = minValue;

		final float percent = (float) (value - minValue) / (maxValue - minValue);

		final float x = knobMinX + (percent * (knobMaxX - knobMinX));
		knob.offsetTo(x, knob.getY());
		knobRect.offsetTo(x, knob.getY());
		this.value = value;

		barFull.setTexWidth(percent);

		notifyListeners(new WidgetEvent(this, "valueChanged", value));
	}

	public int getValue()
	{
		return value;
	}

	@Override
	public void setVisible(boolean isVisible)
	{
		barEmpty.setVisible(isVisible);
		barFull.setVisible(isVisible);
		knob.setVisible(isVisible);
	}
}