package com.lescomber.vestige.graphics;

import com.lescomber.vestige.crossover.SpriteManager.SpriteInfo;
import com.lescomber.vestige.crossover.SpriteManager.SpriteTemplate;
import com.lescomber.vestige.geometry.Point;

import java.util.ArrayList;

public class SpriteAnimation implements Image {
	private static final int DEFAULT_FRAME_TIME = 67;

	private final SpriteInfo sharedInfo;

	private Sprite curSprite;
	private Sprite nextSprite;

	private float dAlphaPerMs;

	private boolean isVisible;

	private final ArrayList<SpriteTemplate> frames;
	private int curFrameIndex;
	private int curTime;
	private int frameTime;

	// Remaining duration (in ms) for the entire animation. Normally an animation finishes whenever it completes all its sequences but if duration is
	//set (to a value >= 0) then the animation will stop abruptly when duration drops below 0
	private int duration;

	private boolean isPlaying;

	private boolean holdLastFrame;    // If true, last frame remains as a static image when animation is finished playing

	// Sequence limit refers to the number of times this animation should go through a full sequence before before being stopped. Negative number
	//means no limit
	private int sequenceLimit;
	private int sequenceNum;    // Current sequence

	public SpriteAnimation() {
		sharedInfo = new SpriteInfo(null, 0, 0);

		curSprite = null;
		nextSprite = null;

		dAlphaPerMs = 0;

		isVisible = false;

		frames = new ArrayList<SpriteTemplate>();
		curFrameIndex = 0;
		curTime = 0;
		frameTime = DEFAULT_FRAME_TIME;

		duration = -1;

		isPlaying = false;
		holdLastFrame = false;

		sequenceLimit = 1;
		sequenceNum = 1;
	}

	public SpriteAnimation(SpriteTemplate[] templates, int startIndex, int endIndex) {
		this();

		for (int i = startIndex; i <= endIndex; i++)
			frames.add(templates[i]);

		sharedInfo.template = frames.get(0);
		curSprite = new Sprite(sharedInfo);
	}

	public SpriteAnimation(SpriteTemplate[] templates) {
		this();

		for (final SpriteTemplate st : templates)
			frames.add(st);

		sharedInfo.template = frames.get(0);
		curSprite = new Sprite(sharedInfo);
	}

	public SpriteAnimation(SpriteAnimation copyMe) {
		sharedInfo = new SpriteInfo(copyMe.sharedInfo);
		curSprite = new Sprite(sharedInfo);
		frames = new ArrayList<SpriteTemplate>();
		for (final SpriteTemplate st : copyMe.frames)
			frames.add(st);
		curFrameIndex = copyMe.curFrameIndex;
		curTime = copyMe.curTime;
		frameTime = copyMe.frameTime;
		duration = copyMe.duration;
		isPlaying = copyMe.isPlaying;
		holdLastFrame = copyMe.holdLastFrame;
		dAlphaPerMs = copyMe.dAlphaPerMs;
		sequenceLimit = copyMe.sequenceLimit;
		sequenceNum = copyMe.sequenceNum;
		isVisible = false;
		setVisible(copyMe.isVisible);
	}

	/**
	 * @return true if the animation finished its last sequence this frame
	 */
	public boolean update(int deltaTime) {
		boolean isFinished = false;

		if (isPlaying) {
			curTime += deltaTime;
			while (curTime >= frameTime) {
				curTime -= frameTime;
				isFinished = nextFrame();
			}

			// If duration has been set, countdown here and finish the anim if it reaches 0 this frame
			if (duration >= 0) {
				duration -= deltaTime;

				if (duration < 0) {
					if (holdLastFrame) {
						curFrameIndex = frames.size() - 1;
						isPlaying = false;
						isFinished = true;
					} else {
						stop();
						isFinished = true;
					}
				}
			}

			// Handle "fade" if one is in progress
			if (dAlphaPerMs != 0)
				curSprite.setAlpha(Math.min(sharedInfo.alpha + (dAlphaPerMs * deltaTime), 1));
		}

		return isFinished;
	}

	/**
	 * @return true if the animation finished its last sequence this frame
	 */
	private boolean nextFrame() {
		curFrameIndex++;

		// Case: we just finished the last frame of the current sequence
		if (curFrameIndex == frames.size()) {
			curFrameIndex = 0;

			// Case: we just finished the last sequence
			if (--sequenceNum == 0) {
				if (holdLastFrame) {
					curFrameIndex = frames.size() - 1;
					isPlaying = false;
					return true;
				} else {
					stop();
					return true;
				}
			}

			// Case: we just finished a sequence but there is at least one more sequence to go
			else if (isVisible) {
				sharedInfo.template = frames.get(curFrameIndex);
				nextSprite = new Sprite(sharedInfo);

				Swapper.swapImages(curSprite, nextSprite);
				curSprite = nextSprite;
			}

			// Case: isVisible = false
			else {
				sharedInfo.template = frames.get(curFrameIndex);
				curSprite = new Sprite(sharedInfo);
			}

		}

		// Case: there are one or more frames left in this sequence
		else if (isVisible) {
			sharedInfo.template = frames.get(curFrameIndex);
			nextSprite = new Sprite(sharedInfo);

			Swapper.swapImages(curSprite, nextSprite);
			curSprite = nextSprite;
		}

		// Case: isVisible = false
		else {
			sharedInfo.template = frames.get(curFrameIndex);
			curSprite = new Sprite(sharedInfo);
		}

		return false;
	}

	public void addFrame(SpriteTemplate template) {
		frames.add(template);
		if (sharedInfo.template == null) {
			sharedInfo.template = frames.get(0);
			curSprite = new Sprite(sharedInfo);
		}
	}

	public void addFrames(SpriteTemplate[] templates) {
		for (final SpriteTemplate st : templates)
			addFrame(st);
	}

	public void addFrames(SpriteTemplate[] templates, int startIndex, int endIndex) {
		for (int i = startIndex; i <= endIndex; i++)
			addFrame(templates[i]);
	}

	public void play() {
		isPlaying = true;
		curSprite.setVisible(isVisible);
	}

	public void pause() {
		isPlaying = false;
		curSprite.setVisible(false);
	}

	public void stop() {
		isPlaying = false;
		curSprite.setVisible(false);
		curFrameIndex = 0;
		curTime = 0;
		sequenceNum = sequenceLimit;

		// Update curSprite to reference the correct (first) frame
		sharedInfo.template = frames.get(curFrameIndex);
		curSprite = new Sprite(sharedInfo);
	}

	@Override
	public void offset(float dx, float dy) {
		curSprite.offset(dx, dy);
	}

	@Override
	public void offsetTo(float x, float y) {
		curSprite.offsetTo(x, y);
	}

	@Override
	public void offsetTo(Point p) {
		offsetTo(p.x, p.y);
	}

	@Override
	public void rotate(float radians) {
		curSprite.rotate(radians);
	}

	@Override
	public void rotateTo(float radians) {
		curSprite.rotateTo(radians);
	}

	@Override
	public void rotateAbout(float radians, float rotateX, float rotateY) {
		curSprite.rotateAbout(radians, rotateX, rotateY);
	}

	@Override
	public void scale(double widthRatio, double heightRatio) {
		curSprite.scale(widthRatio, heightRatio);
	}

	@Override
	public void scaleTo(float width, float height) {
		curSprite.scaleTo(width, height);
	}

	@Override
	public void setAlpha(float alpha) {
		curSprite.setAlpha(alpha);
	}

	@Override
	public void setLayerHeight(int layerHeight) {
		if (curSprite != null)
			curSprite.setLayerHeight(layerHeight);
		else
			sharedInfo.layerHeight = layerHeight;
	}

	/**
	 * Sets current alpha to startAlpha, then fades in to alpha = 1.0f over fadeInSeconds
	 */
	public void setFadeIn(float startAlpha, double fadeInSeconds) {
		setAlpha(startAlpha);
		dAlphaPerMs = (float) ((1 - startAlpha) / fadeInSeconds / 1000);
	}

	/**
	 * Sets current alpha to startAlpha, then fades out to alpha = 0 over fadeOutSeconds
	 */
	public void setFadeOut(float startAlpha, double fadeOutSeconds) {
		setAlpha(startAlpha);
		dAlphaPerMs = (float) (-startAlpha / fadeOutSeconds / 1000);
	}

	/**
	 * Set the frame time by specifying the duration of one entire sequence
	 */
	public void setSequenceDuration(int duration) {
		setFrameTime(Math.round(duration / frames.size()));
	}

	public void setFrameTime(int frameTime) {
		this.frameTime = frameTime;
	}

	public void setHoldLastFrame(boolean holdLastFrame) {
		this.holdLastFrame = holdLastFrame;
	}

	public void setDuration(int duration) {
		setSequenceLimit(-1);
		this.duration = duration;
	}

	public void setSequenceLimit(int sequenceLimit) {
		this.sequenceLimit = sequenceLimit;
		sequenceNum = sequenceLimit;
	}

	public void setSequenceLimitByDuration(int duration) {
		setSequenceLimit((int) Math.ceil(duration / (frames.size() * frameTime)));
	}

	public void clearFrames() {
		frames.clear();
		isPlaying = false;
		sharedInfo.template = null;
		curFrameIndex = 0;
		curTime = 0;
		sequenceNum = sequenceLimit;
	}

	public int getTimeRemaining() {
		if (sequenceLimit < 0)
			return -1;
		else if (sequenceNum <= 0)
			return 0;
		else {
			final int curFrameTime = frameTime - curTime;
			final int curSequence = curFrameTime + (frameTime * (frames.size() - curFrameIndex - 1));
			final int allRemainingSequences = curSequence + ((sequenceNum - 1) * frames.size() * frameTime);
			return allRemainingSequences;
		}
	}

	@Override
	public Sprite getSprite() {
		return curSprite;
	}

	@Override
	public int getIndex() {
		return curSprite.getIndex();
	}

	@Override
	public float getX() {
		return sharedInfo.x;
	}

	@Override
	public float getY() {
		return sharedInfo.y;
	}

	@Override
	public float getDirection() {
		return sharedInfo.direction;
	}

	@Override
	public boolean isVisible() {
		return isVisible;
	}

	public int getFrameTime() {
		return frameTime;
	}

	public int getFrameCount() {
		return frames.size();
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	@Override
	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
		if (isPlaying || !isVisible)
			curSprite.setVisible(isVisible);
	}

	@Override
	public void close() {
		curSprite.close();
	}

	@Override
	public void wasReplaced() {
		isVisible = false;
		isPlaying = false;
		curSprite.wasReplaced();
	}

	@Override
	public void wasAdded(int newIndex) {
		isVisible = true;
		isPlaying = true;
		curSprite.wasAdded(newIndex);
	}

	@Override
	public SpriteAnimation copy() {
		return new SpriteAnimation(this);
	}
}