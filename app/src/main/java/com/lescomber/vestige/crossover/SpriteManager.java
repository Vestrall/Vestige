package com.lescomber.vestige.crossover;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.graphics.Rect;
import android.opengl.GLES20;

import com.lescomber.vestige.Assets;
import com.lescomber.vestige.cgl.CGLTexture;
import com.lescomber.vestige.cgl.CGLTexturedRect;
import com.lescomber.vestige.cgl.CGLThreePatchTexturedRect;
import com.lescomber.vestige.framework.Screen;

public class SpriteManager
{
	private static SpriteManager instance;
	
	private final Object bufferWaitLock;
	
	private static final int INITIAL_THE_LIST_SIZE = 250;
	
	private int backgroundTextureHandle;
	private List<CGLTexturedRect> backgroundList;
	
	private int uiTextureHandle;
	
	private int actualListSize;		// Refers to actual size of theList in terms of the number of images (not SpriteBundles)
	private List<SpriteBundle> theList;
	private final LinkedList<Integer> nulls;
	
	private boolean isBuffering;
	private boolean bufferSwap;
	private int bufferListSize;
	private List<SpriteBundle> bufferList;
	private int bufferBackgroundTextureHandle;
	private List<CGLTexturedRect> bufferBackgroundList;
	private int bufferUITextureHandle;
	private List<Integer>[] bufferUILayers;
	private int bufferLayerCount;
	private int[] bufferLayerInsertionSort;
	
	private static final int INITIAL_LAYER_SIZE = 250;
	private int layerCount;
	private int[] layerInsertionSort;
	
	public static final int UI_LAYER_OVER_THREE = Integer.MAX_VALUE;
	public static final int UI_LAYER_OVER_TWO = UI_LAYER_OVER_THREE - 1;
	public static final int UI_LAYER_OVER_ONE = UI_LAYER_OVER_TWO - 1;
	public static final int UI_LAYER_UNDER_TWO = UI_LAYER_OVER_ONE - 1;
	public static final int UI_LAYER_UNDER_ONE = UI_LAYER_UNDER_TWO - 1;
	
	private final List<Integer>[] uiLayers;
	
	private final LinkedList<ListChange> changes;
	
	//===================================================================
	// Template list to be used by the rest of the game to create sprites
	//===================================================================
	
	// Map objects
	public static SpriteTemplate tiles[], wallTops[], wallBottoms[], wallMid, portal[];
	
	// Main menu images
	public static SpriteTemplate title[], smallEyes[], mediumEyes[], bigEyes[], stageLocked, stageLockedSelected, darkWoods,
								 darkWoodsSelected, scoreEmpty, scoreHalf, scoreFull, loadingCircleBackground,
								 loadingCircleFill;
	
	// Glows
	public static SpriteTemplate smallGlow, bigGlow, purpleGlow, redGlow, redLaserGlowBody, redLaserGlowEnd, rectangleRedGlow;
	
	// Layered map objects
	public static SpriteTemplate trees[];
	
	// Units
	public static SpriteTemplate bossWalkLeft[], bossWalkRight[], bossFiringLeft[], bossFiringRight[], bossChannelingLeft[],
								 bossChannelingRight[], bossDeathLeft[], bossDeathRight[], /*waveBossTurret,*/
								 floatingCreepWalkLeft[], floatingCreepWalkRight[], floatingCreepAttackLeft[],
								 floatingCreepAttackRight[], floatingCreepDeathLeft[], floatingCreepDeathRight[],
								 casterWalkLeft[], casterWalkRight[], casterFiringLeft[], casterFiringRight[], casterDeathLeft[],
								 casterDeathRight[], playerWalkLeft[], playerWalkRight[], playerFiringLeft[], playerFiringRight[],
								 spawnPortalSpawn[], spawnPortalOpen[], spawnPortalEnd[];
	
	// Projectiles/AreaEffects
	public static SpriteTemplate explosion[], plasmaBall[], groundFire[], enemyLaserHead, enemyLaserBody, doubleEnemyLaser,
								 sOneSwipe, sOneChargeSwipe, enemyProjectile, purpleProjectile, purpleComet, sOneDoubleTapLaser;
	
	// PickUps
	public static SpriteTemplate pickUpGlow, healthPickUp, healthPickUpAnimation[];
	
	// Buffs
	public static SpriteTemplate shield[];
	
	// UI elements
	public static SpriteTemplate hpGregHealth, hpBarBackground, hpSteveHealth, hpBossBackground, hpBossHealth, hpShieldHealth,
								 swipeArrow, cdArcEmpty, cdArcFull, cdTeleportEmpty, cdTeleportFull, cdShieldEmpty,
								 cdShieldFull, cdInnerBlueArc, cdInnerGrayArc, chargeArrowTail, chargeArrowHead,
								 chargeArrowCooldownTail, chargeArrowCooldownHead, chargeArrowSparks[], uiTextBackground;
	
	// Widgets
	public static SpriteTemplate menuButton, menuButtonClick, menuButtonPieces[], menuButtonClickPieces[], backButton,
								 backButtonClick, tutorialNextButton, tutorialNextButtonClick, levelSelectButtonLocked,
								 levelSelectButtonUnlocked, pauseButton, pauseButtonClick, checkBoxOff, checkBoxOn, sliderEmpty,
								 sliderFull, sliderKnob;
	
	@SuppressWarnings("unchecked")
	protected SpriteManager()
	{
		// TODO: Possibly reduce the amount of work (e.g. adding all the SpriteBundles) that needs to be done here as this all
		//happens right when the app first launches
		
		bufferWaitLock = new Object();
		backgroundTextureHandle = -1;
		backgroundList = new ArrayList<CGLTexturedRect>();
		uiTextureHandle = -1;
		theList = new ArrayList<SpriteBundle>(INITIAL_THE_LIST_SIZE);
		for (int i=0; i<INITIAL_THE_LIST_SIZE; i++)
			theList.add(new SpriteBundle());
		bufferList = new ArrayList<SpriteBundle>(INITIAL_THE_LIST_SIZE);
		for (int i=0; i<INITIAL_THE_LIST_SIZE; i++)
			bufferList.add(new SpriteBundle());
		nulls = new LinkedList<Integer>();
		isBuffering = false;
		bufferSwap = false;
		layerCount = 0;
		layerInsertionSort = new int[INITIAL_LAYER_SIZE];
		bufferLayerInsertionSort = new int[INITIAL_LAYER_SIZE];
		uiLayers = new ArrayList[5];
		uiLayers[0] = new ArrayList<Integer>();
		uiLayers[1] = new ArrayList<Integer>();
		uiLayers[2] = new ArrayList<Integer>();
		uiLayers[3] = new ArrayList<Integer>();
		uiLayers[4] = new ArrayList<Integer>();
		changes = new LinkedList<ListChange>();
		actualListSize = 0;
	}
	
	public static void initTemplates()
	{
		//============
		// Map objects
		//============
		tiles = createTemplates(Assets.backgroundTexture, 3, 1, 5, 80, 102);
		
		wallTops = new SpriteTemplate[3];
		wallTops[0] = new SpriteTemplate(Assets.backgroundTexture, new Rect(0, 204, 30, 223));
		wallTops[1] = new SpriteTemplate(Assets.backgroundTexture, new Rect(30, 204, 60, 223));
		wallTops[2] = new SpriteTemplate(Assets.backgroundTexture, new Rect(90, 204, 120, 223));
		wallMid = new SpriteTemplate(Assets.backgroundTexture, new Rect(60, 204, 90, 223));
		wallBottoms = new SpriteTemplate[3];
		wallBottoms[0] = new SpriteTemplate(Assets.backgroundTexture, new Rect(0, 223, 30, 258));
		wallBottoms[1] = new SpriteTemplate(Assets.backgroundTexture, new Rect(30, 223, 60, 258));
		wallBottoms[2] = new SpriteTemplate(Assets.backgroundTexture, new Rect(60, 223, 90, 258));
		
		portal = createTemplates(Assets.portal, 8, 1, 20, 127, 116);
		
		//=================
		// Main menu images
		//=================
		title = createTemplates(Assets.title, 5, 1, 40, 389, 210);
		
		smallEyes = createTemplates(Assets.menuImages, 0, 316, 3, 1, 8, 18, 9);
		mediumEyes = createTemplates(Assets.menuImages, 54, 316, 2, 1, 8, 22, 11);
		bigEyes = createTemplates(Assets.menuImages, 98, 316, 2, 1, 7, 30, 12);
		
		stageLocked = new SpriteTemplate(Assets.menuImages, new Rect(0, 0, 168, 316));
		stageLockedSelected = new SpriteTemplate(Assets.menuImages, new Rect(168, 0, 567, 445));
		darkWoods = new SpriteTemplate(Assets.menuImages, new Rect(567, 0, 735, 316));
		darkWoodsSelected = new SpriteTemplate(Assets.menuImages, new Rect(735, 0, 1127, 445));
		
		scoreEmpty = new SpriteTemplate(Assets.menuImages, new Rect(0, 360, 26, 386));
		scoreHalf = new SpriteTemplate(Assets.menuImages, new Rect(26, 360, 52, 386));
		scoreFull = new SpriteTemplate(Assets.menuImages, new Rect(52, 360, 78, 386));
		
		loadingCircleBackground = new SpriteTemplate(Assets.menuImages, new Rect(1127, 0, 1316, 189));
		loadingCircleFill = new SpriteTemplate(Assets.menuImages, new Rect(1316, 0, 1572, 256));
		
		//=================
		// Projectile Glows
		//=================
		smallGlow = new SpriteTemplate(Assets.glows, new Rect(0, 0, 28, 13));
		bigGlow = new SpriteTemplate(Assets.glows, new Rect(0, 13, 87, 27));
		purpleGlow = new SpriteTemplate(Assets.glows, new Rect(28, 0, 56, 13));
		redGlow = new SpriteTemplate(Assets.glows, new Rect(56, 0, 81, 11));
		redLaserGlowBody = new SpriteTemplate(Assets.glows, new Rect(81, 0, 84, 9));
		redLaserGlowEnd = new SpriteTemplate(Assets.glows, new Rect(84, 0, 100, 9));
		rectangleRedGlow = new SpriteTemplate(Assets.glows, new Rect(81, 0, 116, 9));
		
		//======
		// Trees
		//======
		trees = new SpriteTemplate[6];
		trees[0] = new SpriteTemplate(Assets.trees, new Rect(0, 0, 160, 142));
		trees[1] = new SpriteTemplate(Assets.trees, new Rect(160, 0, 320, 142));
		trees[2] = new SpriteTemplate(Assets.trees, new Rect(320, 0, 460, 146));
		trees[3] = new SpriteTemplate(Assets.trees, new Rect(0, 142, 140, 288));
		trees[4] = new SpriteTemplate(Assets.trees, new Rect(140, 142, 260, 276));
		trees[5] = new SpriteTemplate(Assets.trees, new Rect(260, 146, 380, 280));
		
		//======
		// Units
		//======
		bossWalkLeft = createTemplates(Assets.boss, 9, 1, 7, 215, 184);
		bossWalkRight = createTemplates(Assets.boss, 9, 8, 14, 215, 184);
		bossFiringLeft = createTemplates(Assets.boss, 9, 15, 31, 215, 184);
		bossFiringRight = createTemplates(Assets.boss, 9, 32, 48, 215, 184);
		bossChannelingLeft = createTemplates(Assets.boss, 9, 49, 62, 215, 184);
		bossChannelingRight = createTemplates(Assets.boss, 9, 63, 76, 215, 184);
		
		bossDeathLeft = createTemplates(Assets.bossDeath, 8, 1, 30, 232, 184);
		bossDeathRight = createTemplates(Assets.bossDeath, 8, 31, 60, 232, 184);
		
		floatingCreepWalkLeft = createTemplates(Assets.floatingCreep, 7, 1, 14, 72, 62);
		floatingCreepWalkRight = createTemplates(Assets.floatingCreep, 7, 15, 28, 72, 62);
		floatingCreepAttackLeft = createTemplates(Assets.floatingCreep, 7, 29, 32, 72, 62);
		floatingCreepAttackRight = createTemplates(Assets.floatingCreep, 7, 33, 36, 72, 62);
		
		floatingCreepDeathLeft = createTemplates(Assets.floatingCreepDeath, 11, 1, 10, 90, 90);
		floatingCreepDeathRight = createTemplates(Assets.floatingCreepDeath, 11, 11, 20, 90, 90);
		
		casterWalkLeft = createTemplates(Assets.caster, 7, 1, 9, 132, 91);
		casterWalkRight = createTemplates(Assets.caster, 7, 10, 18, 132, 91);
		casterFiringLeft = createTemplates(Assets.caster, 7, 19, 25, 132, 91);
		casterFiringRight = createTemplates(Assets.caster, 7, 26, 32, 132, 91);
		
		casterDeathLeft = createTemplates(Assets.casterDeath, 8, 1, 12, 124, 126);
		casterDeathRight = createTemplates(Assets.casterDeath, 8, 13, 24, 124, 126);
		
		playerWalkLeft = createTemplates(Assets.player, 9, 1, 8, 56, 85);
		playerWalkRight = createTemplates(Assets.player, 9, 9, 16, 56, 85);
		playerFiringLeft = createTemplates(Assets.player, 9, 17, 22, 56, 85);
		playerFiringRight = createTemplates(Assets.player, 9, 23, 28, 56, 85);
		
		spawnPortalSpawn = createTemplates(Assets.spawnPortal, 7, 1, 14, 70, 101);
		spawnPortalOpen = createTemplates(Assets.spawnPortal, 7, 15, 26, 70, 101);
		
		spawnPortalEnd = createTemplates(Assets.spawnPortalEnd, 4, 1, 9, 125, 125);
		
		//========================
		// Projectiles/AreaEffects
		//========================
		explosion = createTemplates(Assets.explosion, 8, 1, 13, 128, 128);
		
		plasmaBall = createTemplates(Assets.plasmaBall, 3, 1, 3, 41, 41);
		
		groundFire = createTemplates(Assets.groundFire, 11, 1, 11, 46, 64);
		
		//enemyLaserBody = new SpriteTemplate(Assets.enemyLaser, new Rect(0, 1, 3, 25));
		//enemyLaserHead = new SpriteTemplate(Assets.enemyLaser, new Rect(3, 0, 25, 26));
		enemyLaserBody = new SpriteTemplate(Assets.doubleEnemyLaser, new Rect(24, 0, 27, 26));
		enemyLaserHead = new SpriteTemplate(Assets.doubleEnemyLaser, new Rect(27, 0, 49, 26));
		doubleEnemyLaser = new SpriteTemplate(Assets.doubleEnemyLaser, new Rect(0, 0, 47, 22));
		
		sOneSwipe = new SpriteTemplate(Assets.stageOneProjectiles, new Rect(0, 0, 67, 34));
		sOneChargeSwipe = new SpriteTemplate(Assets.stageOneProjectiles, new Rect(67, 0, 121, 83));
		
		sOneDoubleTapLaser = new SpriteTemplate(Assets.sOneDoubleTapLaser, new Rect(0, 0, 435, 22));
		
		enemyProjectile = new SpriteTemplate(Assets.enemyProjectile, new Rect(0, 0, 49, 30));
		
		purpleProjectile = new SpriteTemplate(Assets.purpleProjectile, new Rect(0, 0, 64, 47));
		
		purpleComet = new SpriteTemplate(Assets.purpleComet, new Rect(0, 0, 90, 58));
		
		//========
		// PickUps
		//========
		pickUpGlow = new SpriteTemplate(Assets.pickUpGlow, new Rect(0, 0, 38, 12));
		healthPickUp = new SpriteTemplate(Assets.healthPickUp, new Rect(0, 0, 40, 40));
		healthPickUpAnimation = createTemplates(Assets.healthPickUpAnimation, 8, 1, 8, 66, 87);
		
		//======
		// Buffs
		//======
		shield = createTemplates(Assets.shield, 5, 1, 10, 102, 101);
		
		//============
		// UI elements
		//============
		hpGregHealth = new SpriteTemplate(Assets.gameUITexture, new Rect(210, 44, 258, 56));
		hpBarBackground = new SpriteTemplate(Assets.gameUITexture, new Rect(210, 24, 258, 35));
		hpSteveHealth = new SpriteTemplate(Assets.gameUITexture, new Rect(210, 35, 252, 44));
		hpBossBackground = new SpriteTemplate(Assets.gameUITexture, new Rect(258, 24, 354, 38));
		hpBossHealth = new SpriteTemplate(Assets.gameUITexture, new Rect(258, 38, 354, 52));
		hpShieldHealth = new SpriteTemplate(Assets.gameUITexture, new Rect(210, 56, 256,66));
		
		cdArcEmpty = new SpriteTemplate(Assets.gameUITexture, new Rect(0, 27, 35, 62));
		cdArcFull = new SpriteTemplate(Assets.gameUITexture, new Rect(35, 27, 70, 62));
		cdTeleportEmpty = new SpriteTemplate(Assets.gameUITexture, new Rect(70, 27, 105, 62));
		cdTeleportFull = new SpriteTemplate(Assets.gameUITexture, new Rect(105, 27, 140, 62));
		cdShieldEmpty = new SpriteTemplate(Assets.gameUITexture, new Rect(140, 27, 175, 62));
		cdShieldFull = new SpriteTemplate(Assets.gameUITexture, new Rect(175, 27, 210, 62));
		cdInnerBlueArc = new SpriteTemplate(Assets.gameUITexture, new Rect(0, 0, 13, 25));
		cdInnerGrayArc = new SpriteTemplate(Assets.gameUITexture, new Rect(52, 0, 65, 27));
		
		swipeArrow = new SpriteTemplate(Assets.gameUITexture, new Rect(0, 174, 337, 199));
		
		chargeArrowTail = new SpriteTemplate(Assets.gameUITexture, new Rect(0, 199, 315, 232));
		chargeArrowHead = new SpriteTemplate(Assets.gameUITexture, new Rect(315, 199, 337, 232));
		
		chargeArrowCooldownTail = new SpriteTemplate(Assets.gameUITexture, new Rect(0, 232, 316, 256));
		chargeArrowCooldownHead = new SpriteTemplate(Assets.gameUITexture, new Rect(316, 232, 337, 256));
		
		chargeArrowSparks = createTemplates(Assets.gameUITexture, 0, 256, 4, 1, 12, 103, 43);
		
		uiTextBackground = new SpriteTemplate(Assets.gameUITexture, new Rect(278, 61, 394, 97));
		
		//========
		// Widgets
		//========
		menuButton = new SpriteTemplate(Assets.gameUITexture, new Rect(0, 62, 200, 116));
		menuButtonClick = new SpriteTemplate(Assets.gameUITexture, new Rect(0, 116, 200, 170));
		menuButtonPieces = new SpriteTemplate[3];
		menuButtonPieces[1] = new SpriteTemplate(Assets.gameUITexture, new Rect(0, 62, 19, 116));
		menuButtonPieces[0] = new SpriteTemplate(Assets.gameUITexture, new Rect(19, 62, 181, 116));
		menuButtonPieces[2] = new SpriteTemplate(Assets.gameUITexture, new Rect(181, 62, 200, 116));
		menuButtonClickPieces = new SpriteTemplate[3];
		menuButtonClickPieces[1] = new SpriteTemplate(Assets.gameUITexture, new Rect(0, 116, 19, 170));
		menuButtonClickPieces[0] = new SpriteTemplate(Assets.gameUITexture, new Rect(19, 116, 181, 170));
		menuButtonClickPieces[2] = new SpriteTemplate(Assets.gameUITexture, new Rect(181, 116, 200, 170));
		
		backButton = new SpriteTemplate(Assets.gameUITexture, new Rect(200, 140, 262, 202));
		backButtonClick = new SpriteTemplate(Assets.gameUITexture, new Rect(262, 140, 324, 202));
		
		tutorialNextButton = new SpriteTemplate(Assets.gameUITexture, new Rect(200, 66, 239, 105));
		tutorialNextButtonClick = new SpriteTemplate(Assets.gameUITexture, new Rect(239, 66, 278, 105));
		
		levelSelectButtonLocked = new SpriteTemplate(Assets.gameUITexture, new Rect(200, 62, 296, 140));
		levelSelectButtonUnlocked = new SpriteTemplate(Assets.gameUITexture, new Rect(296, 62, 392, 140));
		
		pauseButton = new SpriteTemplate(Assets.gameUITexture, new Rect(354, 24, 391, 61));
		pauseButtonClick = new SpriteTemplate(Assets.gameUITexture, new Rect(391, 24, 428, 61));
		
		checkBoxOff = new SpriteTemplate(Assets.gameUITexture, new Rect(337, 217, 376, 256));
        checkBoxOn = new SpriteTemplate(Assets.gameUITexture, new Rect(376, 217, 415, 256));
		
		sliderEmpty = new SpriteTemplate(Assets.gameUITexture, new Rect(79, 0, 448, 12));
		sliderFull = new SpriteTemplate(Assets.gameUITexture, new Rect(79, 12, 448, 24));
		sliderKnob = new SpriteTemplate(Assets.gameUITexture, new Rect(448, 0, 478, 30));
	}
	
	private static SpriteTemplate[] createTemplates(CGLTexture texture, int startX, int startY, int imagesPerRow, int startNum,
												 int endNum, int width, int height)
	{
		final int totalImages = endNum - startNum + 1;
		final SpriteTemplate[] templates = new SpriteTemplate[totalImages];
		
		int curImageNum = startNum;
		int x = startX + (((curImageNum - 1) % imagesPerRow) * width);
		int y = startY + (((int)Math.floor(curImageNum / imagesPerRow)) * height);
		if (curImageNum % imagesPerRow == 0)
			y -= height;
		
		for (int i=0; i<totalImages; i++)
		{
			templates[i] = new SpriteTemplate(texture, new Rect(x, y, x + width, y + height));
			
			if (curImageNum % imagesPerRow == 0)
			{
				x = startX;
				y += height;
			}
			else
				x += width;
			
			curImageNum++;
		}
		
		return templates;
	}
	
	private static SpriteTemplate[] createTemplates(CGLTexture texture, int imagesPerRow, int startNum, int endNum, int width,
												 int height)
	{
		return createTemplates(texture, 0, 0, imagesPerRow, startNum, endNum, width, height);
	}
	
	public static SpriteManager getInstance()
	{
		if (instance == null)
			instance = new SpriteManager();
		
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public void startBuffer()
	{
		isBuffering = true;
		
		bufferListSize = 0;
		
		bufferBackgroundTextureHandle = -1;
		bufferBackgroundList = new ArrayList<CGLTexturedRect>();
		
		bufferUITextureHandle = -1;
		
		bufferUILayers = new ArrayList[5];
		bufferUILayers[0] = new ArrayList<Integer>();
		bufferUILayers[1] = new ArrayList<Integer>();
		bufferUILayers[2] = new ArrayList<Integer>();
		bufferUILayers[3] = new ArrayList<Integer>();
		bufferUILayers[4] = new ArrayList<Integer>();
		
		bufferLayerCount = 0;
	}
	
	private void finishBuffer()
	{
		backgroundTextureHandle = bufferBackgroundTextureHandle;
		backgroundList = bufferBackgroundList;
		
		uiTextureHandle = bufferUITextureHandle;
		
		// Swap theList and bufferList references
		final List<SpriteBundle> tempHandle = theList;
		theList = bufferList;
		bufferList = tempHandle;
		actualListSize = bufferListSize;
		
		// Swap (buffer)layerInsertionSort array references
		final int[] tempArray = layerInsertionSort;
		layerInsertionSort = bufferLayerInsertionSort;
		bufferLayerInsertionSort = tempArray;
		layerCount = bufferLayerCount;
		
		for (int i=0; i<5; i++)
			uiLayers[i] = bufferUILayers[i];
		
		changes.clear();
		nulls.clear();
		
		bufferSwap = false;
		
		// Inform game logic thread that the swap has been completed (if it was waiting)
		synchronized (bufferWaitLock)
		{
			isBuffering = false;
			bufferWaitLock.notifyAll();
		}
	}
	
	public void swapBuffer()
	{
		bufferSwap = true;
		
		// Wait for the buffer swap to be performed by the renderer thread (after its next draw() is finished) before proceeding
		synchronized (bufferWaitLock)
		{
			while (isBuffering)
			{
				try { bufferWaitLock.wait(); } catch (final InterruptedException e) { e.printStackTrace(); }
			}
		}
	}
	
	public void clearBackgroundSprites()
	{
		if (isBuffering)
			bufferBackgroundList.clear();
		else
		{
			synchronized (backgroundList)
			{
				backgroundList.clear();
			}
		}
	}
	
	public void addBackgroundSprite(SpriteTemplate template, float x, float y)
	{
		final CGLTexturedRect newSprite = new CGLTexturedRect(template.texture.getTextureHandle(), x, y, template.texture.getWidth(),
				template.texture.getHeight(), template.subTexRect);
		
		if (isBuffering)
		{
			bufferBackgroundList.add(newSprite);
		}
		else
		{
			synchronized (backgroundList)
			{
				backgroundList.add(newSprite);
			}
		}
	}
	
	public void setBackground(CGLTexture texture)
	{
		final CGLTexturedRect newSprite = new CGLTexturedRect(texture.getTextureHandle(), Screen.MIDX, Screen.MIDY, texture.getWidth(),
				texture.getHeight(), new Rect(0, 0, Screen.WIDTH, Screen.HEIGHT));
		
		if (isBuffering)
		{
			bufferBackgroundTextureHandle = texture.getTextureHandle();
			bufferBackgroundList.add(newSprite);
		}
		else	// Note: probably should never be reached
		{
			backgroundTextureHandle = texture.getTextureHandle();
			synchronized (backgroundList)
			{
				backgroundList.add(newSprite);
			}
		}
	}
	
	public void setBackgroundTextureHandle(int textureHandle)
	{
		if (isBuffering)
			bufferBackgroundTextureHandle = textureHandle;
		else
			backgroundTextureHandle = textureHandle;	// Note: probably should never be reached
	}
	
	public void setUITextureHandle(int textureHandle)
	{
		if (isBuffering)
			bufferUITextureHandle = textureHandle;
		else
			uiTextureHandle = textureHandle;
	}
	
	public void drawGameLayer()
	{
		processChangeQueue();
		
		sortLayerList();
		
		// Bind and draw background images. They must all be on the texture specified by backgroundTextureHandle
		if (!backgroundList.isEmpty())
		{
			synchronized (backgroundList)
			{
				// Bind the background texture
				GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
				GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, backgroundTextureHandle);
				
				// Draw background sprites
				for (final CGLTexturedRect ctr : backgroundList)
					ctr.draw();
			}
		}
		
		// Draw game sprites in the order specified by layeringList. They binded and drawn one at a time (for now...)
		SpriteBundle gsb;
		for (int i=0; i<layerCount; i++)
		{
			gsb = theList.get(layerInsertionSort[i]);
			synchronized (gsb)
			{
				if (gsb.texturedRect != null)
					gsb.texturedRect.bindDraw();
			}
		}
		
		// Bind the UI Texture
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, uiTextureHandle);
		
		// Draw "under blankets" UI layers. Elements within each layer are drawn in the order they were added
		for (int i=0; i<2; i++)
		{
			for (final Integer j : uiLayers[i])
			{
				gsb = theList.get(j);
				synchronized (gsb)
				{
					if (gsb.texturedRect != null)
						gsb.texturedRect.draw();
				}
			}
		}
	}
	
	// Draw "over blankets" UI layers
	public void drawUILayer()
	{
		// Bind the UI Texture
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, uiTextureHandle);
		
		// Draw UI sprites. Elements within each layer are drawn in the order they were added
		SpriteBundle gsb;
		for (int i=2; i<5; i++)
		{
			for (final Integer j : uiLayers[i])
			{
				gsb = theList.get(j);
				synchronized (gsb)
				{
					if (gsb.texturedRect != null)
						gsb.texturedRect.draw();
				}
			}
		}
		
		// Case: a buffer swap is pending. Since we have just completed a draw, let's perform the swap now
		if (bufferSwap && isBuffering)
			finishBuffer();
	}
	
	private void processChangeQueue()
	{
		ListChange change;
		synchronized (changes)
		{
			while (!changes.isEmpty())
			{
				change = changes.removeFirst();
				
				if (change.command == ListChange.NEW_SPRITE)
				{
					if (theList.get(change.index).layerHeight < UI_LAYER_UNDER_ONE)
					{
						layerInsertionSort[layerCount] = change.index;
						layerCount++;
						
						// Dynamically resize layerInsertionSort array if it is about to be exceeded on the next add.
						//INITIAL_LAYER_SIZE should be chosen to be large enough that this never actually happens for performance
						//reasons. Still, if it comes down to it, dynamically resizing is obviously better than a crash
						if (layerCount >= layerInsertionSort.length)
						{
							final int newArray[] = new int[layerInsertionSort.length + INITIAL_LAYER_SIZE];
							System.arraycopy(layerInsertionSort, 0, newArray, 0, layerInsertionSort.length);
							layerInsertionSort = newArray;
						}
					}
					else
						uiLayers[theList.get(change.index).layerHeight - UI_LAYER_UNDER_ONE].add(change.index);
				}
				else	// command == REMOVE_SPRITE
				{
					for (int i=0; i<layerCount; i++)
					{
						if (layerInsertionSort[i] == change.index)
						{
							System.arraycopy(layerInsertionSort, i + 1, layerInsertionSort, i, layerCount - i);
							
							layerCount--;
							break;
						}
					}
				}
			}
		}
	}
	
	private void sortLayerList()
	{
		// Perform insertion sort
		int curHeight;
		int curTheListIndex;
		int backwardsLayerIndex;
		int moveAmount;
		for (int i=1; i<layerCount; i++)
		{
			if (theList.get(layerInsertionSort[i]).layerHeight < theList.get(layerInsertionSort[i-1]).layerHeight)
			{
				curHeight = theList.get(layerInsertionSort[i]).layerHeight;
				curTheListIndex = layerInsertionSort[i];
				backwardsLayerIndex = i - 1;
				
				while (backwardsLayerIndex > 0 && curHeight < theList.get(layerInsertionSort[backwardsLayerIndex-1]).layerHeight)
				{
					backwardsLayerIndex--;
				}
				
				moveAmount = i - backwardsLayerIndex;
				switch (moveAmount)
				{
				case 2:
					layerInsertionSort[backwardsLayerIndex + 2] = layerInsertionSort[backwardsLayerIndex + 1];
				case 1:
					layerInsertionSort[backwardsLayerIndex + 1] = layerInsertionSort[backwardsLayerIndex];
					break;
				default:
					System.arraycopy(layerInsertionSort, backwardsLayerIndex, layerInsertionSort, backwardsLayerIndex + 1, moveAmount);
				}
				layerInsertionSort[backwardsLayerIndex] = curTheListIndex;
			}
		}
	}
	
	private int newSprite(CGLTexturedRect newSprite, SpriteInfo info)
	{
		// Retrieve index value to place newSprite in
		int index;
		if (isBuffering)
		{
			index = bufferListSize;
			bufferListSize++;
			
			// Case: we are about to exceed the amount of SpriteBundles we have allocated
			if (index == bufferList.size())
				bufferList.add(new SpriteBundle());
			
			if (info.layerHeight < UI_LAYER_UNDER_ONE)
			{
				bufferLayerInsertionSort[bufferLayerCount] = index;
				bufferLayerCount++;
				
				// Dynamically resize bufferLayerInsertionSort array if it is about to be exceeded on the next add.
				//INITIAL_LAYER_SIZE should be chosen to be large enough that this never actually happens for performance
				//reasons. Still, if it comes down to it, dynamically resizing is obviously better than a crash
				if (bufferLayerCount >= bufferLayerInsertionSort.length)
				{
					final int newArray[] = new int[bufferLayerInsertionSort.length + INITIAL_LAYER_SIZE];
					System.arraycopy(bufferLayerInsertionSort, 0, newArray, 0, bufferLayerInsertionSort.length);
					bufferLayerInsertionSort = newArray;
				}
				
				// Sort newly added sprite into its correct position in buffereLayerInsertionSort
				int backwardsLayerIndex = bufferLayerCount - 1;
				
				while (backwardsLayerIndex > 0 &&
						info.layerHeight < bufferList.get(bufferLayerInsertionSort[backwardsLayerIndex-1]).layerHeight)
				{
					backwardsLayerIndex--;
				}
				
				System.arraycopy(bufferLayerInsertionSort, backwardsLayerIndex, bufferLayerInsertionSort, backwardsLayerIndex + 1,
						(bufferLayerCount - 1) - backwardsLayerIndex);
				bufferLayerInsertionSort[backwardsLayerIndex] = index;
			}
			else
				bufferUILayers[info.layerHeight - UI_LAYER_UNDER_ONE].add(index);
		}
		else if (!nulls.isEmpty())
			index = nulls.removeFirst();
		else
		{
			index = actualListSize;
			actualListSize++;
			
			// Case: we are about to exceed the amount of SpriteBundles we have allocated
			if (index == theList.size())
				theList.add(new SpriteBundle());
		}
		
		// Add the new sprite to theList or bufferList
		if (isBuffering)
		{
			bufferList.get(index).texturedRect = newSprite;
			bufferList.get(index).layerHeight = info.layerHeight;
		}
		else
		{
			synchronized (theList.get(index))
			{
				theList.get(index).texturedRect = newSprite;
				theList.get(index).layerHeight = info.layerHeight;
			}
			
			synchronized (changes)
			{
				changes.add(new ListChange(ListChange.NEW_SPRITE, index));
			}
		}
		
		return index;
	}
	
	public int newSprite(SpriteInfo info)
	{
		final CGLTexture texture = info.template.texture;
		final CGLTexturedRect newSprite = new CGLTexturedRect(texture.getTextureHandle(), info.x, info.y,
				texture.getWidth(), texture.getHeight(), info.template.subTexRect);
		
		// Apply info's specifications
		if (info.direction != 0)
			newSprite.rotate(info.direction);
		if (info.widthScale != 1 || info.heightScale != 1)
			newSprite.scale(info.widthScale, info.heightScale);
		if (info.alpha != 1)
			newSprite.setAlpha(info.alpha);
		if (info.texRect != null)
			newSprite.setTexRect(info.texRect[0], info.texRect[1], info.texRect[2], info.texRect[3]);
		
		return newSprite(newSprite, info);
	}
	
	public int newThreePatchSprite(SpriteInfo info, SpriteTemplate left, SpriteTemplate right)
	{
		CGLTexture texture = info.template.texture;
		final CGLThreePatchTexturedRect newSprite = new CGLThreePatchTexturedRect(texture.getTextureHandle(), info.x,
				info.y, texture.getWidth(), texture.getHeight(), info.template.subTexRect);
		
		if (info.widthScale != 1)
			newSprite.scale(info.widthScale, 1);
		
		if (left != null)
		{
			texture = left.texture;
			newSprite.setLeft(texture.getTextureHandle(), texture.getWidth(), texture.getHeight(), left.subTexRect);
		}
		if (right != null)
		{
			texture = right.texture;
			newSprite.setRight(texture.getTextureHandle(), texture.getWidth(), texture.getHeight(), right.subTexRect);
		}
		
		// Apply info's specifications
		if (info.direction != 0)
			newSprite.rotate(info.direction);
		if (info.heightScale != 1)
			newSprite.scale(1, info.heightScale);
		if (info.alpha != 1)
			newSprite.setAlpha(info.alpha);
		if (info.texRect != null)
			newSprite.setTexRect(info.texRect[0], info.texRect[1], info.texRect[2], info.texRect[3]);
		
		return newSprite(newSprite, info);
	}
	
	public void removeSprite(int index)
	{
		if (index < 0)
			return;
		
		synchronized (theList.get(index))
		{
			theList.get(index).texturedRect = null;
		}
		
		synchronized (changes)
		{
			changes.add(new ListChange(ListChange.REMOVE_SPRITE, index));
		}
	}
	
	private void replaceSprite(int removeIndex, CGLTexturedRect newSprite, SpriteInfo addInfo)
	{
		synchronized (theList.get(removeIndex))
		{
			theList.get(removeIndex).texturedRect = newSprite;
			theList.get(removeIndex).layerHeight = addInfo.layerHeight;
		}
	}
	
	public void replaceSprite(int removeIndex, SpriteInfo addInfo)
	{
		final CGLTexture texture = addInfo.template.texture;
		final CGLTexturedRect newSprite = new CGLTexturedRect(texture.getTextureHandle(), addInfo.x, addInfo.y,
				texture.getWidth(), texture.getHeight(), addInfo.template.subTexRect);
		
		if (addInfo.direction != 0)
			newSprite.rotate(addInfo.direction);
		if (addInfo.widthScale != 1 || addInfo.heightScale != 1)
			newSprite.scale(addInfo.widthScale, addInfo.heightScale);
		if (addInfo.alpha != 1)
			newSprite.setAlpha(addInfo.alpha);
		if (addInfo.texRect != null)
			newSprite.setTexRect(addInfo.texRect[0], addInfo.texRect[1], addInfo.texRect[2], addInfo.texRect[3]);
		
		replaceSprite(removeIndex, newSprite, addInfo);
	}
	
	public void replaceSprite(int removeIndex, SpriteInfo addInfo, SpriteTemplate left, SpriteTemplate right)
	{
		CGLTexture texture = addInfo.template.texture;
		final CGLThreePatchTexturedRect newSprite = new CGLThreePatchTexturedRect(texture.getTextureHandle(), addInfo.x,
				addInfo.y, texture.getWidth(), texture.getHeight(), addInfo.template.subTexRect);
		
		if (addInfo.widthScale != 1)
			newSprite.scale(addInfo.widthScale, 1);
		
		if (left != null)
		{
			texture = left.texture;
			newSprite.setLeft(texture.getTextureHandle(), texture.getWidth(), texture.getHeight(), left.subTexRect);
		}
		if (right != null)
		{
			texture = right.texture;
			newSprite.setRight(texture.getTextureHandle(), texture.getWidth(), texture.getHeight(), right.subTexRect);
		}
		
		if (addInfo.direction != 0)
			newSprite.rotate(addInfo.direction);
		if (addInfo.heightScale != 1)
			newSprite.scale(1, addInfo.heightScale);
		if (addInfo.alpha != 1)
			newSprite.setAlpha(addInfo.alpha);
		if (addInfo.texRect != null)
			newSprite.setTexRect(addInfo.texRect[0], addInfo.texRect[1], addInfo.texRect[2], addInfo.texRect[3]);
		
		replaceSprite(removeIndex, newSprite, addInfo);
	}
	
	public void offset(int index, float dx, float dy)
	{
		synchronized (theList.get(index))
		{
			theList.get(index).texturedRect.offset(dx, dy);
		}
	}
	
	public void rotate(int index, float radians)
	{
		synchronized (theList.get(index))
		{
			theList.get(index).texturedRect.rotate(radians);
		}
	}
	
	public void scale(int index, float widthRatio, float heightRatio)
	{
		synchronized (theList.get(index))
		{
			theList.get(index).texturedRect.scale(widthRatio, heightRatio);
		}
	}
	
	public void setAlpha(int index, float alpha)
	{
		synchronized (theList.get(index))
		{
			theList.get(index).texturedRect.setAlpha(alpha);
		}
	}
	
	public void setLayerHeight(int index, int layerHeight)
	{
		synchronized (theList.get(index))
		{
			theList.get(index).layerHeight = layerHeight;
		}
	}
	
	public void setTexRect(int index, float[] texRect, float dx, float wScale)
	{
		SpriteBundle gsb;
		synchronized (gsb = theList.get(index))
		{
			gsb.texturedRect.setTexRect(texRect[0], texRect[1], texRect[2], texRect[3]);
			gsb.texturedRect.scale(wScale, 1);
			gsb.texturedRect.offset(dx, 0);
		}
	}
	
	public static class SpriteTemplate
	{
		private final CGLTexture texture;
		private final Rect subTexRect;
		private final int width;
		private final int height;
		
		public SpriteTemplate(CGLTexture texture, Rect subTexRect)
		{
			this.texture = texture;
			
			if (subTexRect != null)
			{
				this.subTexRect = subTexRect;
				width = subTexRect.right - subTexRect.left;
				height = subTexRect.bottom - subTexRect.top;
			}
			else
			{
				width = texture.getWidth();
				height = texture.getHeight();
				this.subTexRect = new Rect(0, 0, width, height);
			}
		}
		
		public int getWidth() { return width; }
		public int getHeight() { return height; }
		public Rect getSubTexRect() { return subTexRect; }
		public int getTexWidth() { return texture.getWidth(); }
		public int getTexHeight() { return texture.getHeight(); }
	}
	
	public static class SpriteInfo
	{
		public SpriteTemplate template;
		public float[] texRect;
		public float x = 0;
		public float y = 0;
		public float direction = 0;
		public float widthScale = 1;
		public float heightScale = 1;
		public float alpha = 1;
		public int layerHeight = 0;
		
		public SpriteInfo(SpriteTemplate template, float x, float y)
		{
			this.template = template;
			texRect = null;
			this.x = x;
			this.y = y;
			direction = 0;
			widthScale = 1;
			heightScale = 1;
			alpha = 1;
			layerHeight = 0;
		}
		
		public SpriteInfo(SpriteInfo copyMe)
		{
			template = copyMe.template;
			texRect = null;
			if (copyMe.texRect != null)
			{
				texRect = new float[4];
				for (int i=0; i<4; i++)
					texRect[i] = copyMe.texRect[i];
			}
			x = copyMe.x;
			y = copyMe.y;
			direction = copyMe.direction;
			widthScale = copyMe.widthScale;
			heightScale = copyMe.heightScale;
			alpha = copyMe.alpha;
			layerHeight = copyMe.layerHeight;
		}
	}
	
	private class ListChange
	{
		private static final int NEW_SPRITE = 0;
		private static final int REMOVE_SPRITE = 1;
		
		private final int index;
		private final int command;
		
		private ListChange(int command, int index)
		{
			this.index = index;
			this.command = command;
		}
	}
	
	private class SpriteBundle
	{
		private CGLTexturedRect texturedRect;
		private int layerHeight;
		
		private SpriteBundle()
		{
			texturedRect = null;
			layerHeight = 0;
		}
	}
}