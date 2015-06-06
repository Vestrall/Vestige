package com.lescomber.vestige.framework;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.SoundPool;

import com.lescomber.vestige.Options;

import java.io.IOException;
import java.util.ArrayList;

public class AudioManager {
	private static final String audioDir = "Audio/";

	private static AssetManager assets;
	private static SoundPool soundPool;

	private static Music music;
	private static Music queuedMusic;
	private static boolean queueing;

	private static ArrayList<SoundEffect> allSoundEffects;
	private static ArrayList<MusicEffect> allMusicEffects;

	private static final String AUDIO_EXTENSION = ".ogg";

	public static final String MENU_MUSIC = "TemptationMarch";
	public static final String BOSS_MUSIC = "PendulumWaltz";
	public static final String GAME_MUSIC = "SneakySnooper";

	// Menu sound effects
	public static SoundEffect buttonClick;

	// Universal sound effects
	public static SoundEffect shieldAbsorb, healPickUp;

	// Player sound effects
	public static SoundEffect playerAttack, sOneSwipeHit, sOneChargeSwipeHit, sOneDoubleTapHit;

	// Enemy sound effects
	public static SoundEffect floatingCreepHit, enemyProjectileHit, purpleExplosion, enemyLaserHit, oneSixGroundSlam;

	// Music effects (looping sound effects)
	public static MusicEffect fireLoop, movingBeamLoop;

	public static void init(AndroidGameActivity activity) {
		activity.setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC);
		assets = activity.getAssets();
		soundPool = new SoundPool(20, android.media.AudioManager.STREAM_MUSIC, 0);
		music = null;
		queuedMusic = null;
		allSoundEffects = new ArrayList<SoundEffect>();
		allMusicEffects = new ArrayList<MusicEffect>();
		queueing = false;
		fireLoop = null;
		movingBeamLoop = null;
	}

	public static void initSoundEffects() {
		//===================
		// Menu sound effects
		//===================
		buttonClick = createSound("multimedia_button_click_007", 0.6);
		allSoundEffects.add(buttonClick);

		//========================
		// Universal sound effects
		//========================
		shieldAbsorb = createSound("alien_squeaks_mp3_4pitch2", 0.6);
		allSoundEffects.add(shieldAbsorb);

		healPickUp = createSound("science_fiction_laser_006_treble", 0.65);
		allSoundEffects.add(healPickUp);

		//=====================
		// Player sound effects
		//=====================
		playerAttack = createSound("female_exert_grunt", 0.5);
		allSoundEffects.add(playerAttack);

		sOneSwipeHit = createSound("Photon, photons 2_speedy", 1);
		allSoundEffects.add(sOneSwipeHit);

		sOneChargeSwipeHit = createSound("Photon, photons 2", 1);
		allSoundEffects.add(sOneChargeSwipeHit);

		sOneDoubleTapHit = createSound("science_fiction_laser_002_speed", 0.3);
		allSoundEffects.add(sOneDoubleTapHit);

		//====================
		// Enemy sound effects
		//====================
		floatingCreepHit = createSound("Shuffleboard disk, fast throw 1", 0.5);
		allSoundEffects.add(floatingCreepHit);

		enemyProjectileHit = createSound("science_fiction_laser_005_half_speed", 0.4);
		allSoundEffects.add(enemyProjectileHit);

		purpleExplosion = createSound("explosion_punchy_impact_03_tempo", 0.08);
		allSoundEffects.add(purpleExplosion);

		enemyLaserHit = createSound("science_fiction_laser_005_half_speed_speed", 0.5);
		allSoundEffects.add(enemyLaserHit);

		oneSixGroundSlam = createSound("Single cannon shot", 0.5);
		allSoundEffects.add(oneSixGroundSlam);

		//======================================-
		// Music effects (looping sound effects)
		//======================================
		fireLoop = new MusicEffect("Frying sizzle_fire1", 0.15f);
		allMusicEffects.add(fireLoop);

		movingBeamLoop = new MusicEffect("science_fiction_laser_005_half_speed_speed_section", 0.5f);
		allMusicEffects.add(movingBeamLoop);
	}

	public static void pause(boolean isFinishing) {
		if (music != null) {
			if (isFinishing)
				music.dispose();
			else
				music.pause();
		}

		if (isFinishing) {
			for (final MusicEffect me : allMusicEffects)
				me.dispose();
		} else {
			for (final MusicEffect me : allMusicEffects)
				me.pause();
		}
	}

	public static void resume() {
		if (music != null)
			music.resume();

		for (final MusicEffect me : allMusicEffects)
			me.resume();
	}

	public static void musicVolumeChanged() {
		if (music != null)
			music.setVolume(Options.getMusicVolume());
	}

	public static void sfxVolumeChanged() {
		for (final SoundEffect se : allSoundEffects)
			se.updateVolume();

		for (final MusicEffect me : allMusicEffects)
			me.updateVolume();
	}

	public static void playMusic(String filename) {
		if (!queueing) {
			// Case: song is already playing
			if (music != null && music.isPlaying(filename))
				return;

			if (music != null)
				music.dispose();

			if (filename == null) {
				music = null;
				return;
			}

			music = createMusic(filename);
			if (music != null)
				music.setVolume(Options.getMusicVolume());

			music.play();
		} else {
			if (queuedMusic != null)
				queuedMusic.dispose();

			// Case: filename is null or this song is already playing
			if (filename == null || (music != null && music.isPlaying(filename))) {
				queuedMusic = null;
				return;
			}

			queuedMusic = createMusic(filename);
		}
	}

	public static void queueMode() {
		queueing = true;
	}

	public static void activateQueue() {
		if (queuedMusic != null) {
			if (music != null)
				music.dispose();

			music = queuedMusic;
			if (music != null)
				music.setVolume(Options.getMusicVolume());
			music.play();

			queuedMusic = null;
			queueing = false;
		}

		for (final MusicEffect me : allMusicEffects)
			me.stop();
	}

	public static void clearQueue() {
		queueing = false;

		if (queuedMusic != null) {
			queuedMusic.dispose();
			queuedMusic = null;
		}
	}

	public static void pauseMusicEffects() {
		for (final MusicEffect me : allMusicEffects)
			me.pause();
	}

	public static void resumeMusicEffects() {
		for (final MusicEffect me : allMusicEffects)
			me.resume();
	}

	protected static Music createMusic(String filename) {
		try {
			final AssetFileDescriptor assetDescriptor = assets.openFd(audioDir + filename + AUDIO_EXTENSION);
			return new Music(assetDescriptor, filename);
		} catch (final IOException e) {
			throw new RuntimeException("Couldn't load music '" + filename + "'");
		}
	}

	private static SoundEffect createSound(String filename, double volume) {
		try {
			final AssetFileDescriptor assetDescriptor = assets.openFd(audioDir + filename + AUDIO_EXTENSION);
			final int id = soundPool.load(assetDescriptor, 0);
			return new SoundEffect(id, volume);
		} catch (final IOException e) {
			throw new RuntimeException("Couldn't load sound '" + filename + "'");
		}
	}

	public static class SoundEffect {
		private final int id;
		private final float baseVolume;
		private float volume;

		private SoundEffect(int id, double baseVolume) {
			this.id = id;
			this.baseVolume = (float) baseVolume;
			updateVolume();
		}

		private void updateVolume() {
			volume = Options.getSfxVolume() * baseVolume;
		}

		public void play() {
			soundPool.play(id, volume, volume, 0, 0, 1);
		}
	}
}