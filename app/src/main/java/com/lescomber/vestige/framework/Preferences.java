package com.lescomber.vestige.framework;

import android.content.SharedPreferences;

import com.lescomber.vestige.screens.OptionsScreen;

public class Preferences {
	private static SharedPreferences prefs;

	public static void initPrefs(SharedPreferences prefs) {
		Preferences.prefs = prefs;
	}

	public static int getLastStage() {
		return prefs.getInt("lastStage", 1);
	}

	public static void setLastStage(int stage) {
		setInt("lastStage", stage);
	}

	public static int getStageProgress(int difficulty) {
		final String key = parseDifficulty(difficulty) + "StageProgress";
		return prefs.getInt(key, 0);
	}

	public static void setStageProgress(int difficulty, int progress) {
		final String key = parseDifficulty(difficulty) + "StageProgress";
		setInt(key, progress);
	}

	public static int getLevelProgress(int difficulty) {
		final String key = parseDifficulty(difficulty) + "LevelProgress";
		return prefs.getInt(key, 0);
	}

	public static void setLevelProgress(int difficulty, int progress) {
		final String key = parseDifficulty(difficulty) + "LevelProgress";
		setInt(key, progress);
	}

	public static float getMusicVolume() {
		return prefs.getFloat("musicVolume", 1);
	}

	public static void setMusicVolume(float volume) {
		setFloat("musicVolume", volume);
	}

	public static float getSfxVolume() {
		return prefs.getFloat("sfxVolume", 1);
	}

	public static void setSfxVolume(float volume) {
		setFloat("sfxVolume", volume);
	}

	public static boolean isFpsDisplayed() {
		return prefs.getBoolean("displayFPS", true);
	}

	public static void setFpsDisplayed(boolean displayed) {
		setBoolean("displayFPS", displayed);
	}

	public static boolean isWaveDisplayed() {
		return prefs.getBoolean("displayWave", true);
	}

	public static void setWaveDisplayed(boolean displayed) {
		setBoolean("displayWave", displayed);
	}

	public static int getPewBallCurrentLevel() {
		return prefs.getInt("pewBallCurrentLevel", 1);
	}

	public static void setPewBallCurrentLevel(int level) {
		setInt("pewBallCurrentLevel", level);
	}

	public static int getPewBallHighScore() {
		return prefs.getInt("pewBallHighScore", 0);
	}

	public static void setPewBallHighScore(int level) {
		setInt("pewBallHighScore", level);
	}

	public static boolean isPewBallInProgress() {
		return prefs.getBoolean("pewBallInProgress", false);
	}

	public static void setPewBallInProgress(boolean inProgress) {
		setBoolean("pewBallInProgress", inProgress);
	}

	private static String parseDifficulty(int difficulty) {
		switch (difficulty) {
			case OptionsScreen.EASY:
				return "easy";
			case OptionsScreen.MEDIUM:
				return "medium";
			case OptionsScreen.HARD:
				return "hard";
			default:
				return "";
		}
	}

	private static void setBoolean(String key, boolean value) {
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		editor.apply();
	}

	private static void setFloat(String key, float value) {
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putFloat(key, value);
		editor.apply();
	}

	private static void setInt(String key, int value) {
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(key, value);
		editor.apply();
	}

	private static void setLong(String key, long value) {
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(key, value);
		editor.apply();
	}

	private static void setString(String key, String value) {
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putString(key, value);
		editor.apply();
	}

	public static void clear() {
		final SharedPreferences.Editor editor = prefs.edit();
		editor.clear();
		editor.apply();
	}
}