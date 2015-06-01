package com.lescomber.vestige;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Typeface;

import com.lescomber.vestige.cgl.CGLTexture;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class Assets {
	private static AssetManager assets;

	// Background images
	public static CGLTexture genericBackground, mainMenuScreen;

	// Menu images
	public static CGLTexture title[], smallEyes[], mediumEyes[], bigEyes[], loadingCircleBackground, loadingCircleFill, scoreEmpty, scoreHalf,
			scoreFull;

	// Stages
	public static CGLTexture stageLocked, stageLockedSelected, stage01, stage01Selected;

	// Map objects
	public static CGLTexture backgroundTexture, portal[];

	// Glows
	public static CGLTexture smallGlow, bigGlow, purpleGlow, redGlow, rectangleRedGlow;

	// Buffs
	public static CGLTexture shield[];

	// Layered map objects
	public static CGLTexture trees[];

	// Units
	public static CGLTexture playerWalkLeft[], playerWalkRight[], playerFiringLeft[], playerFiringRight[], bossWalkLeft[], bossWalkRight[],
			bossAttackLeft[], bossAttackRight[], bossChannelLeft[], bossChannelRight[], bossDeathLeft[], bossDeathRight[], floatingCreepWalkLeft[],
			floatingCreepWalkRight[], floatingCreepAttackLeft[], floatingCreepAttackRight[], floatingCreepDeathLeft[], floatingCreepDeathRight[],
			casterWalkLeft[], casterWalkRight[], casterAttackLeft[], casterAttackRight[], casterDeathLeft[], casterDeathRight[], spawnPortalSpawn[],
			spawnPortalOpen[], spawnPortalEnd[];

	// Projectiles/AreaEffects
	public static CGLTexture sOneSwipe, sOneChargeSwipe, sOneDoubleTapLaser, explosion[], groundFire[], doubleEnemyLaser, enemyProjectile,
			purpleProjectile, purpleComet, plasmaBall[], pewBall[];

	// PickUps
	public static CGLTexture pickUpGlow, healthPickUp, healthPickUpAnimation[];

	// UI textures
	public static CGLTexture gameUITexture, menuUITexture;

	public static Bitmap newBitmap(String filename, Config format) {
		final Options options = new Options();
		options.inPreferredConfig = format;
		options.inScaled = false;    // No pre-scaling

		InputStream in = null;
		Bitmap bitmap = null;
		try {
			in = assets.open(filename);
			bitmap = BitmapFactory.decodeStream(in, null, options);
			if (bitmap == null)
				throw new RuntimeException("Couldn't load bitmap from asset '" + filename + "'");
		} catch (final IOException e) {
			throw new RuntimeException("Couldn't load bitmap from asset '" + filename + "'");
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (final IOException e) {
				}
		}

		return bitmap;
	}

	/**
	 * Create a new Typeface from font file
	 */
	public static Typeface newTypeface(String filename) {
		return Typeface.createFromAsset(assets, filename);
	}

	public static void initAssetManager(AssetManager assets) {
		Assets.assets = assets;
	}

	public static void initTextures() {
		// Background images
		genericBackground = new CGLTexture();
		mainMenuScreen = new CGLTexture();

		// Menu images
		title = initCGLTextureArray(40);
		smallEyes = initCGLTextureArray(8);
		mediumEyes = initCGLTextureArray(8);
		bigEyes = initCGLTextureArray(7);
		scoreEmpty = new CGLTexture();
		scoreHalf = new CGLTexture();
		scoreFull = new CGLTexture();
		loadingCircleBackground = new CGLTexture();
		loadingCircleFill = new CGLTexture();

		// Stages
		stageLocked = new CGLTexture();
		stageLockedSelected = new CGLTexture();
		stage01 = new CGLTexture();
		stage01Selected = new CGLTexture();

		// Map objects
		backgroundTexture = new CGLTexture();
		portal = initCGLTextureArray(20);

		// Glows
		smallGlow = new CGLTexture();
		bigGlow = new CGLTexture();
		purpleGlow = new CGLTexture();
		redGlow = new CGLTexture();
		rectangleRedGlow = new CGLTexture();

		// Buffs
		shield = initCGLTextureArray(10);

		// Layered map objects
		trees = initCGLTextureArray(6);

		// Units
		bossWalkLeft = initCGLTextureArray(7);
		bossWalkRight = initCGLTextureArray(7);
		bossAttackLeft = initCGLTextureArray(17);
		bossAttackRight = initCGLTextureArray(17);
		bossChannelLeft = initCGLTextureArray(14);
		bossChannelRight = initCGLTextureArray(14);
		bossDeathLeft = initCGLTextureArray(30);
		bossDeathRight = initCGLTextureArray(30);
		floatingCreepWalkLeft = initCGLTextureArray(14);
		floatingCreepWalkRight = initCGLTextureArray(14);
		floatingCreepAttackLeft = initCGLTextureArray(4);
		floatingCreepAttackRight = initCGLTextureArray(4);
		floatingCreepDeathLeft = initCGLTextureArray(10);
		floatingCreepDeathRight = initCGLTextureArray(10);
		casterWalkLeft = initCGLTextureArray(9);
		casterWalkRight = initCGLTextureArray(9);
		casterAttackLeft = initCGLTextureArray(7);
		casterAttackRight = initCGLTextureArray(7);
		casterDeathLeft = initCGLTextureArray(12);
		casterDeathRight = initCGLTextureArray(12);
		spawnPortalSpawn = initCGLTextureArray(14);
		spawnPortalOpen = initCGLTextureArray(12);
		spawnPortalEnd = initCGLTextureArray(9);
		playerWalkLeft = initCGLTextureArray(8);
		playerWalkRight = initCGLTextureArray(8);
		playerFiringLeft = initCGLTextureArray(6);
		playerFiringRight = initCGLTextureArray(6);

		// Projectiles/Explosions
		explosion = initCGLTextureArray(13);
		groundFire = initCGLTextureArray(11);
		doubleEnemyLaser = new CGLTexture();
		sOneSwipe = new CGLTexture();
		sOneChargeSwipe = new CGLTexture();
		sOneDoubleTapLaser = new CGLTexture();
		enemyProjectile = new CGLTexture();
		purpleProjectile = new CGLTexture();
		purpleComet = new CGLTexture();
		plasmaBall = initCGLTextureArray(3);
		pewBall = initCGLTextureArray(3);

		// PickUps
		pickUpGlow = new CGLTexture();
		healthPickUp = new CGLTexture();
		healthPickUpAnimation = initCGLTextureArray(8);

		// UI Texture
		gameUITexture = new CGLTexture();
		menuUITexture = new CGLTexture();
	}

	private static CGLTexture[] initCGLTextureArray(int size) {
		final CGLTexture[] textureArray = new CGLTexture[size];
		for (int i = 0; i < size; i++) {
			textureArray[i] = new CGLTexture();
		}
		return textureArray;
	}

	public static void createTextures() {
		// Background images
		genericBackground.createImageTexture("GenericBackground.png");
		mainMenuScreen.createImageTexture("MainMenuScreen.png");

		// Main menu images
		createCGLTextureArray(title, "Title");
		createCGLTextureArray(smallEyes, "SmallEyes");
		createCGLTextureArray(mediumEyes, "MediumEyes");
		createCGLTextureArray(bigEyes, "BigEyes");
		scoreEmpty.createImageTexture("ScoreEmpty.png");
		scoreHalf.createImageTexture("ScoreHalf.png");
		scoreFull.createImageTexture("ScoreFull.png");
		loadingCircleBackground.createImageTexture("LoadingCircleBackground.png");
		loadingCircleFill.createImageTexture("LoadingCircleFill.png");

		// Stages
		stageLocked.createImageTexture("StageLocked.png");
		stageLockedSelected.createImageTexture("StageLockedSelected.png");
		stage01.createImageTexture("Stage01.png");
		stage01Selected.createImageTexture("Stage01Selected.png");

		// Map objects
		backgroundTexture.createImageTexture("BackgroundTexture.png");
		createCGLTextureArray(portal, "Portal");

		// Glows
		smallGlow.createImageTexture("SmallGlow.png");
		bigGlow.createImageTexture("BigGlow.png");
		purpleGlow.createImageTexture("PurpleGlow.png");
		redGlow.createImageTexture("RedGlow.png");
		rectangleRedGlow.createImageTexture("RectangleRedGlow.png");

		// Buffs
		createCGLTextureArray(shield, "Shield");

		// Trees
		createCGLTextureArray(trees, "Tree");

		// Units
		createCGLTextureArray(bossWalkLeft, "BossWalkLeft");
		createCGLTextureArray(bossWalkRight, "BossWalkRight");
		createCGLTextureArray(bossAttackLeft, "BossAttackLeft");
		createCGLTextureArray(bossAttackRight, "BossAttackRight");
		createCGLTextureArray(bossChannelLeft, "BossChannelLeft");
		createCGLTextureArray(bossChannelRight, "BossChannelRight");
		createCGLTextureArray(bossDeathLeft, "BossDeathLeft");
		createCGLTextureArray(bossDeathRight, "BossDeathRight");
		createCGLTextureArray(floatingCreepWalkLeft, "FloatingCreepWalkLeft");
		createCGLTextureArray(floatingCreepWalkRight, "FloatingCreepWalkRight");
		createCGLTextureArray(floatingCreepAttackLeft, "FloatingCreepAttackLeft");
		createCGLTextureArray(floatingCreepAttackRight, "FloatingCreepAttackRight");
		createCGLTextureArray(floatingCreepDeathLeft, "FloatingCreepDeathLeft");
		createCGLTextureArray(floatingCreepDeathRight, "FloatingCreepDeathRight");
		createCGLTextureArray(casterWalkLeft, "CasterWalkLeft");
		createCGLTextureArray(casterWalkRight, "CasterWalkRight");
		createCGLTextureArray(casterAttackLeft, "CasterAttackLeft");
		createCGLTextureArray(casterAttackRight, "CasterAttackRight");
		createCGLTextureArray(casterDeathLeft, "CasterDeathLeft");
		createCGLTextureArray(casterDeathRight, "CasterDeathRight");
		createCGLTextureArray(spawnPortalSpawn, "SpawnPortalSpawn");
		createCGLTextureArray(spawnPortalOpen, "SpawnPortalOpen");
		createCGLTextureArray(spawnPortalEnd, "SpawnPortalEnd");
		createCGLTextureArray(playerWalkLeft, "PlayerWalkLeft");
		createCGLTextureArray(playerWalkRight, "PlayerWalkRight");
		createCGLTextureArray(playerFiringLeft, "PlayerFiringLeft");
		createCGLTextureArray(playerFiringRight, "PlayerFiringRight");

		// Projectiles/AreaEffects
		createCGLTextureArray(explosion, "Explosion");
		createCGLTextureArray(groundFire, "GroundFire");
		doubleEnemyLaser.createImageTexture("DoubleEnemyLaser.png");
		sOneSwipe.createImageTexture("SOneSwipe.png");
		sOneChargeSwipe.createImageTexture("SOneChargeSwipe.png");
		sOneDoubleTapLaser.createImageTexture("SOneDoubleTapLaser.png");
		enemyProjectile.createImageTexture("EnemyProjectile.png");
		purpleProjectile.createImageTexture("PurpleProjectile.png");
		purpleComet.createImageTexture("PurpleComet.png");
		createCGLTextureArray(plasmaBall, "PlasmaBall");
		createCGLTextureArray(pewBall, "PewBall");

		// PickUps
		pickUpGlow.createImageTexture("PickUpGlow.png");
		healthPickUp.createImageTexture("HealthPickUp.png");
		createCGLTextureArray(healthPickUpAnimation, "HealthPickUpAnimation");

		// UI Texture
		gameUITexture.createImageTexture("GameUITexture.png");
		menuUITexture.createImageTexture("MenuUITexture.png");
	}

	private static void createCGLTextureArray(CGLTexture[] textureArray, String baseFilename) {
		for (int i = 0; i < textureArray.length; i++) {
			textureArray[i].createImageTexture(baseFilename + String.format(Locale.US, "%02d", i + 1) + ".png");
		}
	}
}