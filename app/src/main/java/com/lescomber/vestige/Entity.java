package com.lescomber.vestige;

import java.util.ArrayList;

import com.lescomber.vestige.framework.Util;
import com.lescomber.vestige.geometry.Angle;
import com.lescomber.vestige.geometry.Circle;
import com.lescomber.vestige.geometry.Hitbox;
import com.lescomber.vestige.geometry.Point;
import com.lescomber.vestige.geometry.Rectangle;
import com.lescomber.vestige.geometry.RotatedRect;
import com.lescomber.vestige.geometry.Shape;
import com.lescomber.vestige.graphics.Image;
import com.lescomber.vestige.graphics.Sprite;
import com.lescomber.vestige.graphics.SpriteAnimation;
import com.lescomber.vestige.graphics.Swapper;

public class Entity
{
	private float imageOffsetX;		// X offset from center of hitbox to center of images/animations
	private float imageOffsetY;		// Y offset from center of hitbox to center of images/animations
	private ArrayList<SpriteAnimation> animations;	// Store of all animations associated with this Entity
	private int activeAnimIndex;	// Current animation representing this Entity's main image (e.g. character sprite)
	private Image image;			// Current main image (e.g. character sprite)
	private boolean isVisible;		// Entity visibility (whether or not it is present in SpriteManager list)
	
	protected Hitbox hitbox;		// Entity's hitbox
	
	public Entity()
	{
		imageOffsetX = 0;
		imageOffsetY = 0;
		animations = null;
		activeAnimIndex = -1;
		image = null;
		isVisible = false;
		
		hitbox = new Hitbox();
	}
	
	public Entity(Entity copyMe)
	{
		imageOffsetX = copyMe.imageOffsetX;
		imageOffsetY = copyMe.imageOffsetY;
		animations = null;
		if (copyMe.animations != null)
		{
			animations = new ArrayList<SpriteAnimation>(copyMe.animations.size());
			for (final SpriteAnimation gsa : copyMe.animations)
				animations.add(gsa.copy());
		}
		activeAnimIndex = copyMe.activeAnimIndex;
		image = null;
		if (activeAnimIndex >= 0)
			image = animations.get(activeAnimIndex);
		else if (copyMe.image != null)
			image = copyMe.image.copy();
		hitbox = new Hitbox(copyMe.hitbox);
		isVisible = copyMe.isVisible;
	}
	
	public void update(int deltaTime)
	{
		// Update active Animation (if any)
		if (activeAnimIndex >= 0 && animations.get(activeAnimIndex).update(deltaTime))
			animationFinished(activeAnimIndex);
	}
	
	public boolean overlaps(Entity other)
	{
		return hitbox.overlaps(other.hitbox);
	}
	
	public boolean overlaps(Hitbox hitbox)
	{
		return this.hitbox.overlaps(hitbox);
	}
	
	public boolean overlaps(Shape shape)
	{
		return hitbox.overlaps(shape);
	}
	
	public void offset(float dx, float dy)
	{
		// Offset current image. Note: non-current animations are not offset and must be updated when they are played
		if (image != null)
			image.offset(dx, dy);
		
		// Offset hitbox
		hitbox.offset(dx, dy);
		
		// Update layer height
		setLayerHeight(Math.round(getY()));
	}
	
	public void offsetTo(float x, float y)
	{
		final float dx = x - getX();
		final float dy = y - getY();
		offset(dx, dy);
	}
	
	public void offsetTo(Point p)
	{
		offsetTo(p.x, p.y);
	}
	
	public void rotate(float radians)
	{
		// Rotate hitbox
		hitbox.rotate(radians);
		
		// Rotate image
		if (image != null)
			image.rotate(radians);
	}
	
	public void rotateTo(float radians)
	{
		rotate(Angle.normalizeRadians(radians - getDirection()));
	}
	
	public void rotateAbout(float radians, float rotateX, float rotateY)
	{
		hitbox.rotateAbout(radians, rotateX, rotateY);
		
		if (image != null)
			image.rotateAbout(radians, rotateX, rotateY);
	}
	
	public void rotateAbout(float radians, Point rotationPoint)
	{
		rotateAbout(radians, rotationPoint.x, rotationPoint.y);
	}
	
	// Ratios are relative to current hitbox/image/animation sizes (not absolute or original size)
	public void scale(double widthRatio, double heightRatio)
	{
		// Scale hitbox
		hitbox.scale(widthRatio, heightRatio);
		
		// Scale animations
		if (animations != null)
		{
			for (final SpriteAnimation gsa : animations)
				gsa.scale(widthRatio, heightRatio);
		}
		
		// image has already been scaled above if it is an animation. Otherwise, scale it here
		if (activeAnimIndex < 0)
		{
			if (image != null)
				image.scale(widthRatio, heightRatio);
		}
		
		// Scale image offsets (if needed)
		if (imageOffsetX != 0 || imageOffsetY != 0)
			setImageOffsets((float)(imageOffsetX * widthRatio), (float)(imageOffsetY * heightRatio));
	}
	
	// Scale hitbox and image/animations based on ratio of width and height to current hitbox width and height
	public void scaleTo(float width, float height)
	{
		final double widthRatio = width / hitbox.getWidth();
		final double heightRatio = height / hitbox.getHeight();
		
		scale(widthRatio, heightRatio);
	}
	
	public void scaleWidthTo(float width)
	{
		final double widthRatio = width / hitbox.getWidth();
		
		scale(widthRatio, 1);
	}
	
	public void scaleHeightTo(float height)
	{
		final double heightRatio = height / hitbox.getHeight();
		
		scale(1, heightRatio);
	}
	
	public void createRectangleHitbox(float width, float height)
	{
		final float left = getX() - width / 2;
		final float top = getY() - height / 2;
		hitbox = new Hitbox(new Rectangle(left, top, left + width, top + height));
	}
	
	public void createRotatedRectHitbox(float width, float height)
	{
		hitbox = new Hitbox(new RotatedRect(getX(), getY(), width, height));
	}
	
	public void createCircleHitbox(float radius)
	{
		hitbox = new Hitbox(new Circle(getX(), getY(), radius));
	}
	
	// Clear main Entity image
	public void clearImage()
	{
		if (image != null)
			image.close();
		
		image = null;
		activeAnimIndex = -1;
	}
	
	// Set main Entity image. If image is an animation, play it and return its ID
	public int setImage(Image image)
	{
		if (image instanceof SpriteAnimation)
		{
			final int id = addAnimation((SpriteAnimation)image);
			playAnimation(id);
			return id;
		}
		else
		{
			activeAnimIndex = -1;
			
			updateImage(image);
			
			if (isVisible)
				Swapper.swapImages(this.image, image);
			this.image = image;
			
			return -1;
		}
	}
	
	// Simply updates the image reference for this entity without removing the current image or updating the incoming image's
	//position/orientation/etc. Those things must be handled manually when calling imageRef() instead of setImage()
	public void imageRef(Image image)
	{
		this.image = image;
	}
	
	// Add anim to animations list. Can be subsequently accessed using play/stop/restart Animation methods using the
	//returned animation index
	public int addAnimation(SpriteAnimation anim)
	{
		anim.pause();
		
		if (animations == null)
			animations = new ArrayList<SpriteAnimation>(6);
		
		animations.add(anim);
		return animations.size() - 1;
	}
	
	// Play a currently stored animation and set it as the current image. Returns true if the animationIndex exists
	public boolean playAnimation(int index)
	{
		if (index == activeAnimIndex)
			return true;
		else if (index >= 0 && index < animations.size())
		{
			updateImage(animations.get(index));
			if (isVisible)
				Swapper.swapImages(image, animations.get(index));
			else
				animations.get(index).play();
			image = animations.get(index);
			activeAnimIndex = index;
			
			return true;
		}
		
		return false;
	}
	
	// Stop a currently stored animation (not necessarily the currently visible animation)
	public boolean stopAnimation(int index)
	{
		if (index >= 0 && index < animations.size())
		{
			animations.get(index).stop();
			if (activeAnimIndex == index)
				activeAnimIndex = -1;
			
			return true;
		}
		
		return false;
	}
	
	// Restart a currently stored animation from its beginning (stop() then play() animation)
	public boolean restartAnimation(int index)
	{
		if (index >= 0 && index < animations.size())
		{
			activeAnimIndex = -1;
			animations.get(index).stop();
			return playAnimation(index);
		}
		
		return false;
	}
	
	// Override if there is any code to run when an animation has run its course
	protected void animationFinished(int animID) { }
	
	public void setImageOffsets(float imageOffsetX, float imageOffsetY)
	{
		this.imageOffsetX = imageOffsetX;
		this.imageOffsetY = imageOffsetY;
		updateImage(image);
	}
	
	public void setImageOffsetX(float imageOffsetX)
	{
		setImageOffsets(imageOffsetX, imageOffsetY);
	}
	
	public void setImageOffsetY(float imageOffsetY)
	{
		setImageOffsets(imageOffsetX, imageOffsetY);
	}
	
	private void updateImage(Image image)
	{
		if (image == null)
			return;
		
		// Update image position
		if (Util.equals(getDirection(), 0))
			image.offsetTo(hitbox.getX() + imageOffsetX, hitbox.getY() + imageOffsetY);
		else
		{
			final float iox = (float)(Math.cos(getDirection()) * imageOffsetX);
			image.offsetTo(hitbox.getX() + iox, hitbox.getY() + imageOffsetY);
		}
		
		// Update image direction
		if (!Util.equals(image.getDirection(), getDirection()))
			image.rotateTo(getDirection());
		
		// Update layer height
		image.setLayerHeight(Math.round(getY()));
	}
	
	protected void setLayerHeight(int layerHeight)
	{
		if (image != null)
			image.setLayerHeight(layerHeight);
	}
	
	public int getLayerHeight()
	{
		if (image != null)
			return image.getSprite().getLayerHeight();
		else
			return Math.round(getY());
	}
	
	// Set this Entity's visibility (presence or absence in SpriteManager list)
	public void setVisible(boolean isVisible)
	{
		this.isVisible = isVisible;
		
		if (image != null)
			image.setVisible(isVisible);
	}
	
	public boolean isVisible() { return isVisible; }
	
	public float getX() { return hitbox.getX(); }
	public float getY() { return hitbox.getY(); }
	public Hitbox getHitbox() { return hitbox; }
	public Point getCenter() { return hitbox.getCenter(); }
	public float getImageX() { return getX() + imageOffsetX; }
	public float getImageY() { return getY() + imageOffsetY; }
	public float getImageOffsetX() { return imageOffsetX; }
	public float getImageOffsetY() { return imageOffsetY; }
	public Point getImageCenter() { return new Point(getX() + imageOffsetX, getY() + imageOffsetY); }
	public float getDirection() { return hitbox.getDirection(); }
	
	public Image getImage() { return image; }
	
	public Sprite getSprite()
	{
		if (image != null)
			return image.getSprite();
		else
			return null;
	}
	
	public SpriteAnimation getAnimation(int index)
	{
		if (index >= 0 && index < animations.size())
			return animations.get(index);
		else
			return null;
	}
	
	public int getCurrentAnimID() { return activeAnimIndex; }
	
	// To be called when this Entity is removed from the game. Removes image from SpriteManager. Override to add any additional
	//cleanup code
	public void close()
	{
		if (image != null)
			image.close();
		
		setVisible(false);
	}
}