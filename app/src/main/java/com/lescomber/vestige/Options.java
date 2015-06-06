package com.lescomber.vestige;

import com.lescomber.vestige.framework.AudioManager;
import com.lescomber.vestige.framework.PersistentData;

public class Options {
	// Global option fields
	private static float musicVolume = PersistentData.getMusicVolume();
	private static float sfxVolume = PersistentData.getSfxVolume();
	public static boolean displayFps = PersistentData.isFpsDisplayed();
	public static boolean displayWave = PersistentData.isWaveDisplayed();

	public static int difficulty = 0;
	public static final int EASY = 0;
	public static final int MEDIUM = 1;
	public static final int HARD = 2;

	public static float getMusicVolume() {
		return musicVolume;
	}

	public static void setMusicVolume(float volume) {
		musicVolume = volume;
		PersistentData.setMusicVolume(musicVolume);

		// Inform AudioManager that master volume has changed so it can update the volume of all music / sound effects
		AudioManager.musicVolumeChanged();
	}

	public static float getSfxVolume() {
		return sfxVolume;
	}

	public static void setSfxVolume(float volume) {
		sfxVolume = volume;
		PersistentData.setSfxVolume(sfxVolume);

		// Inform AudioManager that sfx volume has changed so it can update the volume of all sound effects
		AudioManager.sfxVolumeChanged();
	}
}