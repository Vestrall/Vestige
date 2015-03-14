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

public class Assets
{
	private static AssetManager assets;
	
	// Background images
	public static CGLTexture genericBackground, mainMenuScreen;
	
	// Menu images
	public static CGLTexture menuImages, title;
	
	// Map objects
	public static CGLTexture backgroundTexture, portal;
	
	// Glows
	public static CGLTexture glows;
	
	// Buffs
	public static CGLTexture shield;
	
	// Layered map objects
	public static CGLTexture trees;
	
	// Units
	public static CGLTexture boss, bossDeath, floatingCreep, floatingCreepDeath, caster, casterDeath, player, spawnPortal,
							 spawnPortalEnd;
	
	// Projectiles/AreaEffects
	public static CGLTexture explosion, groundFire, /*enemyLaser, */doubleEnemyLaser, stageOneProjectiles, enemyProjectile,
							 purpleProjectile, purpleComet, sOneDoubleTapLaser, plasmaBall;
	
	// PickUps
	public static CGLTexture pickUpGlow, healthPickUp, healthPickUpAnimation;
	
	// UI textures
	public static CGLTexture gameUITexture, menuUITexture;
	
	public static Bitmap newBitmap(String filename, Config format)
	{
		final Options options = new Options();
		options.inPreferredConfig = format;
		options.inScaled = false;	// No pre-scaling
		
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
				} catch (final IOException e) { }
		}
		
		return bitmap;
	}
	
	// Create a new Typeface from font file
	public static Typeface newTypeface(String filename)
	{
		return Typeface.createFromAsset(assets, filename);
	}
	
	public static void initAssetManager(AssetManager assets) { Assets.assets = assets; }
	
	public static void initTextures()
	{
		// Background images
		genericBackground = new CGLTexture();
		mainMenuScreen = new CGLTexture();
		
		// Menu images
		menuImages = new CGLTexture();
		title = new CGLTexture();
		
		// Map objects
		backgroundTexture = new CGLTexture();
		portal = new CGLTexture();
		
		// Glows
		glows = new CGLTexture();
		
		// Buffs
		shield = new CGLTexture();
		
		// Layered map objects
		trees = new CGLTexture();
		
		// Units
		boss = new CGLTexture();
		bossDeath = new CGLTexture();
		floatingCreep = new CGLTexture();
		floatingCreepDeath = new CGLTexture();
		caster = new CGLTexture();
		casterDeath = new CGLTexture();
		player = new CGLTexture();
		spawnPortal = new CGLTexture();
		spawnPortalEnd = new CGLTexture();
		
		// Projectiles/Explosions
		explosion = new CGLTexture();
		groundFire = new CGLTexture();
		//enemyLaser = new CGLTexture();
		doubleEnemyLaser = new CGLTexture();
		stageOneProjectiles = new CGLTexture();
		enemyProjectile = new CGLTexture();
		purpleProjectile = new CGLTexture();
		purpleComet = new CGLTexture();
		sOneDoubleTapLaser = new CGLTexture();
		plasmaBall = new CGLTexture();
		
		// PickUps
		pickUpGlow = new CGLTexture();
		healthPickUp = new CGLTexture();
		healthPickUpAnimation = new CGLTexture();
		
		// UI Texture
		gameUITexture = new CGLTexture();
		menuUITexture = new CGLTexture();
	}
	
	public static void createTextures()
	{
		// Background images
		genericBackground.createImageTexture("GenericBackground.png");
		mainMenuScreen.createImageTexture("MainMenuScreen.png");
		
		// Main menu images
		menuImages.createImageTexture("MenuImages.png");
		title.createImageTexture("Title.png");
		
		// Map objects
		backgroundTexture.createImageTexture("BackgroundTexture.png");
		portal.createImageTexture("Portal.png");
		
		// Glows
		glows.createImageTexture("Glows.png");
		
		// Buffs
		shield.createImageTexture("Shield.png");
		
		// Trees
		trees.createImageTexture("Trees.png");
		
		// Units
		boss.createImageTexture("Boss.png");
		bossDeath.createImageTexture("BossDeath.png");
		floatingCreep.createImageTexture("FloatingCreep.png");
		floatingCreepDeath.createImageTexture("FloatingCreepDeath.png");
		caster.createImageTexture("Caster.png");
		casterDeath.createImageTexture("CasterDeath.png");
		player.createImageTexture("Player.png");
		spawnPortal.createImageTexture("SpawnPortal.png");
		spawnPortalEnd.createImageTexture("SpawnPortalEnd.png");
		
		// Projectiles/AreaEffects
		explosion.createImageTexture("Explosion.png");
		groundFire.createImageTexture("GroundFire.png");
		//enemyLaser.createImageTexture("EnemyLaser.png");
		doubleEnemyLaser.createImageTexture("DoubleEnemyLaser.png");
		stageOneProjectiles.createImageTexture("SOneProjectiles.png");
		enemyProjectile.createImageTexture("EnemyProjectile.png");
		purpleProjectile.createImageTexture("PurpleProjectile.png");
		purpleComet.createImageTexture("PurpleComet.png");
		sOneDoubleTapLaser.createImageTexture("SOneDoubleTapLaser.png");
		plasmaBall.createImageTexture("PlasmaBall.png");
		
		// PickUps
		pickUpGlow.createImageTexture("PickUpGlow.png");
		healthPickUp.createImageTexture("HealthPickUp.png");
		healthPickUpAnimation.createImageTexture("HealthPickUpAnimation.png");
		
		// UI Texture
		gameUITexture.createImageTexture("GameUITexture.png");
		menuUITexture.createImageTexture("MenuUITexture.png");
	}
}