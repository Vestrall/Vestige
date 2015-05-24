package com.lescomber.vestige.widgets;

import com.lescomber.vestige.audio.AudioManager;
import com.lescomber.vestige.audio.AudioManager.SoundEffect;
import com.lescomber.vestige.crossover.SpriteManager;
import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;
import com.lescomber.vestige.framework.TouchHandler.TouchEvent;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.geometry.RotatedRect;
import com.lescomber.vestige.graphics.Swapper;
import com.lescomber.vestige.graphics.Text;
import com.lescomber.vestige.graphics.TextStyle;
import com.lescomber.vestige.graphics.UIThreePatchSprite;

public class Button extends Widget {
	public static final String BUTTON_PRESSED = "buttonPressed";
	public static final String ANIMATION_FINISHED = "animationFinished";

	static final double DEFAULT_CLICK_COUNTDOWN = 0.2;

	static final int WIDTH_PADDING = 15;    // Extra padding for buttons whose width is decided by text width

	private int listenedEvent;

	Text text;
	UIThreePatchSprite sprite;
	RotatedRect rect;

	// Click animation fields
	int maxClickCountdown;
	int clickCountdown;
	UIThreePatchSprite clickSprite;

	private SoundEffect clickSound;

	private ButtonGroup group;

	public Button(float x, float y, float width, float height, TextStyle style, String text, SpriteTemplate template) {
		super();

		SpriteTemplate[] templates = null;

		if (template != null) {
			templates = new SpriteTemplate[3];
			templates[0] = template;
			templates[1] = null;
			templates[2] = null;
		}

		init(x, y, width, height, style, text, templates);
	}

	public Button(float x, float y, float width, float height, TextStyle style, String text, SpriteTemplate[] templates) {
		super();

		init(x, y, width, height, style, text, templates);
	}

	public Button(float x, float y, float width, float height, TextStyle style, String text) {
		super();

		init(x, y, width, height, style, text, SpriteManager.menuButtonPieces);
		setClickAnimation(SpriteManager.menuButtonClickPieces);
	}

	/**
	 * Constructor that dynamically chooses the width/height based on the size of the text/image
	 */
	public Button(float x, float y, TextStyle style, String text, SpriteTemplate template) {
		super();

		SpriteTemplate[] templates = null;

		if (template != null) {
			templates = new SpriteTemplate[3];
			templates[0] = template;
			templates[1] = null;
			templates[2] = null;
		}

		init(x, y, style, text, templates);
	}

	/**
	 * ThreePatch Constructor that dynamically chooses the width/height based on the size of the text/images
	 */
	public Button(float x, float y, TextStyle style, String text, SpriteTemplate[] templates) {
		super();

		init(x, y, style, text, templates);
	}

	private void init(float x, float y, TextStyle style, String text, SpriteTemplate[] templates) {
		float width = 0;
		float height = 0;

		// Base width/height on sprites' native width/height
		if (templates != null) {
			for (final SpriteTemplate st : templates) {
				if (st != null) {
					width += st.getWidth();
					height += st.getHeight();
				}
			}
		}

		// ...or base width/height on size of text if no sprites
		else if (style != null && text != null) {
			width = style.measureText(text) + WIDTH_PADDING;
			height = 1.1f * style.getFontSize();
		}

		init(x, y, width, height, style, text, templates);
	}

	private void init(float x, float y, float width, float height, TextStyle style, String text, SpriteTemplate[] templates) {
		listenedEvent = TouchEvent.TOUCH_UP;

		clickSound = AudioManager.buttonClick;

		// Click countdown fields
		maxClickCountdown = 0;
		clickCountdown = 0;
		clickSprite = null;

		this.text = null;
		if (style != null && text != null)
			this.text = new Text(style, text, x, y, false);

		sprite = null;
		if (templates != null) {
			sprite = new UIThreePatchSprite(templates, x, y);
			sprite.setLayerHeight(SpriteManager.UI_LAYER_OVER_THREE);        // Default to UI layer over 3
			sprite.scaleTo(width, height);
		}

		rect = new RotatedRect(x, y, width, height);

		group = null;
	}

	public void update(int deltaTime) {
		// Animation is considered underway whenever clickCountdown is > 0
		if (clickCountdown > 0) {
			clickCountdown -= deltaTime;

			if (clickCountdown <= 0) {
				if (group != null)
					group.releaseLock();
				displayUnclick();
				notifyListeners(new WidgetEvent(this, ANIMATION_FINISHED));
			}
		}
	}

	@Override
	public void handleEvent(TouchEvent event) {
		if (event.type == listenedEvent) {
			if (rect.contains(event.x, event.y))
				click();
		}
	}

	public void click() {
		// If not visible, Button cannot be interacted with
		if (!isVisible())
			return;

		if (clickCountdown <= 0) {
			// Request lock from group (if a group has been registered)
			if (group != null) {
				if (!group.requestLock())
					return;
			}

			// Play click sound (if any)
			if (clickSound != null)
				clickSound.play();

			// Display click animation (if one exists)
			if (clickSprite != null) {
				displayClickAnimation();
				clickCountdown = maxClickCountdown;
			} else if (group != null)        // Immediately release the lock since there is no animation to wait for anyway
				group.releaseLock();

			notifyListeners(new WidgetEvent(this, BUTTON_PRESSED));
		}
	}

	void displayUnclick() {
		Swapper.swapImages(clickSprite, sprite);
	}

	void displayClickAnimation() {
		Swapper.swapImages(sprite, clickSprite);
	}

	public void setClickAnimation(SpriteTemplate template, double countdownSeconds) {
		SpriteTemplate[] templates = null;

		if (template != null) {
			templates = new SpriteTemplate[3];
			templates[0] = template;
			templates[1] = null;
			templates[2] = null;
		}

		setClickAnimation(templates, countdownSeconds);
	}

	public void setClickAnimation(SpriteTemplate[] templates, double countdownSeconds) {
		if (templates == null) {
			if (clickSprite != null)
				clickSprite.close();
			clickSprite = null;
			return;
		}

		// Retrieve current layerHeight from clickSprite or sprite
		final int layerHeight;
		if (clickSprite != null) {
			layerHeight = clickSprite.getLayerHeight();
			clickSprite.close();
		} else if (sprite != null)
			layerHeight = sprite.getLayerHeight();
		else
			layerHeight = SpriteManager.UI_LAYER_OVER_THREE;    // Default to UI Layer over 3

		final Point center = rect.getCenter();
		clickSprite = new UIThreePatchSprite(templates, center.x, center.y);
		clickSprite.setLayerHeight(layerHeight);
		if (sprite != null)
			clickSprite.scaleTo(sprite.getWidth(), sprite.getHeight());
		maxClickCountdown = (int) (countdownSeconds * 1000);
		clickCountdown = -1;
	}

	public void setClickAnimation(SpriteTemplate template) {
		setClickAnimation(template, DEFAULT_CLICK_COUNTDOWN);
	}

	public void setClickAnimation(SpriteTemplate[] templates) {
		setClickAnimation(templates, DEFAULT_CLICK_COUNTDOWN);
	}

	public void setClickSound(SoundEffect clickSound) {
		this.clickSound = clickSound;
	}

	public SoundEffect getClickSound() {
		return clickSound;
	}

	public void rotate(float radians) {
		if (text != null)
			text.rotate(radians);
		if (sprite != null)
			sprite.rotate(radians);
		if (clickSprite != null)
			clickSprite.rotate(radians);

		rect.rotate(radians);
	}

	public void rotateTo(float radians) {
		rotate(radians - rect.getDirection());
	}

	public Text getText() {
		return text;
	}

	public float getX() {
		return rect.getCenterX();
	}

	public float getY() {
		return rect.getCenterY();
	}

	public void setText(TextStyle style, String text) {
		final boolean isVisible = isVisible();
		if (this.text != null)
			this.text.setVisible(false);

		this.text = new Text(style, text, getX(), getY());
		this.text.setVisible(isVisible);

		// Note: Consider scaling button size if button is too small
	}

	public void setText(String text) {
		final TextStyle style = this.text != null ? this.text.getStyle() : TextStyle.bodyStyleWhite(25);
		setText(style, text);
	}

	public void setImage(UIThreePatchSprite sprite) {
		final boolean isVisible = isVisible();
		final int layerHeight;
		if (this.sprite != null) {
			layerHeight = this.sprite.getLayerHeight();
			this.sprite.close();
		} else
			layerHeight = SpriteManager.UI_LAYER_OVER_THREE;    // Default to UI Layer 3

		if (sprite != null) {
			sprite.setLayerHeight(layerHeight);
			sprite.offsetTo(getX(), getY());
			sprite.setVisible(isVisible);
		}
		this.sprite = sprite;
	}

	public void setImage(SpriteTemplate template) {
		if (template == null)
			setImage((SpriteTemplate[]) null);
		else {
			final SpriteTemplate[] templates = new SpriteTemplate[3];
			templates[0] = template;
			templates[1] = null;
			templates[2] = null;

			setImage(new UIThreePatchSprite(templates));
		}
	}

	public void setImage(SpriteTemplate[] templates) {
		if (templates == null) {
			if (sprite != null)
				sprite.close();
			sprite = null;
		} else
			setImage(new UIThreePatchSprite(templates));
	}

	public void offsetText(float dx, float dy) {
		if (text != null)
			text.offset(dx, dy);
	}

	public void scaleRect(double widthRatio, double heightRatio) {
		rect.scale(widthRatio, heightRatio);
	}

	public void registerGroup(ButtonGroup group) {
		this.group = group;
	}

	public RotatedRect getRect() {
		return rect;
	}

	public void setListenedEvent(int eventType) {
		listenedEvent = eventType;
	}

	public void setLayerHeight(int layerHeight) {
		sprite.setLayerHeight(layerHeight);
		if (clickSprite != null)
			clickSprite.setLayerHeight(layerHeight);
	}

	@Override
	public void setVisible(boolean isVisible) {
		if (text != null)
			text.setVisible(isVisible);
		if (clickCountdown <= 0) {
			if (sprite != null)
				sprite.setVisible(isVisible);
		} else {
			if (clickSprite != null)
				clickSprite.setVisible(isVisible);
		}
	}

	public boolean isVisible() {
		if (clickCountdown <= 0 && sprite != null)
			return sprite.isVisible();
		else if (clickCountdown > 0 && clickSprite != null)
			return clickSprite.isVisible();
		else if (text != null)
			return text.isVisible();
		else
			return false;
	}

	public void wasReplaced() {
		if (text != null)
			text.wasRemoved();
		if (sprite != null)
			sprite.wasReplaced();
		if (clickSprite != null)
			clickSprite.wasReplaced();
	}
}