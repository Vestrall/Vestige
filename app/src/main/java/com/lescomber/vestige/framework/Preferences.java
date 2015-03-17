package com.lescomber.vestige.framework;

import android.content.SharedPreferences;

public class Preferences
{
	private static SharedPreferences prefs;

	public static void initPrefs(SharedPreferences prefs)
	{
		Preferences.prefs = prefs;
	}

	public static boolean getBoolean(String key, boolean defValue)
	{
		return prefs.getBoolean(key, defValue);
	}

	public static float getFloat(String key, float defValue)
	{
		return prefs.getFloat(key, defValue);
	}

	public static int getInt(String key, int defValue)
	{
		return prefs.getInt(key, defValue);
	}

	public static long getLong(String key, long defValue)
	{
		return prefs.getLong(key, defValue);
	}

	public static String getString(String key, String defValue)
	{
		return prefs.getString(key, defValue);
	}

	public static void setBoolean(String key, boolean value)
	{
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		editor.apply();
	}

	public static void setFloat(String key, float value)
	{
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putFloat(key, value);
		editor.apply();
	}

	public static void setInt(String key, int value)
	{
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(key, value);
		editor.apply();
	}

	public static void setLong(String key, long value)
	{
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(key, value);
		editor.apply();
	}

	public static void setString(String key, String value)
	{
		final SharedPreferences.Editor editor = prefs.edit();
		editor.putString(key, value);
		editor.apply();
	}

	public static void clear()
	{
		final SharedPreferences.Editor editor = prefs.edit();
		editor.clear();
		editor.apply();
	}
}